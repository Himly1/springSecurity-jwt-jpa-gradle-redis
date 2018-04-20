package com.himly.api.controller;


import com.himly.api.model.CasServer;
import com.himly.api.model.Role;
import com.himly.api.model.ThirdPartyAccount;
import com.himly.api.repository.CasServerRepository;
import com.himly.api.repository.RoleRepository;
import com.himly.api.repository.ThirdPartyAccountRepository;
import com.himly.api.repository.UserRepository;
import com.himly.api.config.security.config.JwtAuthenticationFilter;
import com.himly.api.config.security.config.SecurityConsole;
import com.himly.api.utils.CasResponseResolveUtil;
import com.himly.api.utils.CodeMapper;
import com.himly.api.utils.HttpReqUtil;
import com.himly.api.utils.JwtUtil;
import io.jsonwebtoken.Jwt;
import io.swagger.annotations.*;
import org.apache.http.HttpEntity;
import org.apache.log4j.Logger;
import org.aspectj.apache.bcel.classfile.Code;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.himly.api.model.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author himly z1399956473@gmail.com
 */
@Api(value = "用户模块")
@RestController
public class UserController {

    private static final Logger log = Logger.getLogger(UserController.class);

    private static final String CAS_CALLBACK_URL = "http://so.yzu.edu.cn/a/cas/callback/";

    private static final String CAS_CALLBACK_PARAM = "service";

    @Autowired
    UserRepository userRepository;

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    BCryptPasswordEncoder bCrypt;

    @Autowired
    CasServerRepository casServerRepository;

    @Autowired
    private ThirdPartyAccountRepository thirdPartyAccountRepository;

    private RedisTemplate<String,String> redisTemplate;

    private ValueOperations<String,String> valueOperations;

    @Autowired
    public void setRedisTemplate(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.valueOperations = redisTemplate.opsForValue();
    }


    @GetMapping("/a/u/user/info")
    @PreAuthorize("hasRole('ROLE_USER')")
    @ApiOperation(value = "获取用户详情", notes = "必须传token")
    @ApiImplicitParams({
            @ApiImplicitParam(name = SecurityConsole.HEADER_STRING, value = "jwt token", required = true, dataType = "string", paramType = "header")
    })
    public LinkedHashMap<String, Object> userInfo(HttpServletRequest request) {

        LinkedHashMap<String, Object> result = new LinkedHashMap<>(4);

        try {
            String token = JwtUtil.getToken(request);
            String account = JwtUtil.getUserAccount(token);


            User user = userRepository.findByAccount(account);
            log.info("user is=="+user);

            if (null==user) {
                log.info("user is null");
                result.put("code",-2007);
                result.put("msg",CodeMapper.codeToMsg(-2007));
                return result;
            }

            result.put("code", 0);
            result.put("msg", CodeMapper.codeToMsg(0));
            //TODO 解决栈溢出
            user.setRoles(null);
            result.put("data", user);
        } catch (Throwable t) {
            log.error("has an error,see==" + t.getMessage(), t);
            result.put("code", -1000);
            result.put("msg", CodeMapper.codeToMsg(-1000));
            return result;
        }

        log.info("result is==" + result);

        return result;
    }


    /***
     *
     * @param user
     * @return
     */
    @ApiOperation(value = "系统账号登录", notes = "系统账号登录")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "user", value = "用户实体user,传account+password", required = true, dataType = "User")
    })
    @PostMapping("/a/login")
    public LinkedHashMap<String, Object> login(@RequestBody User user) {

        log.info("user is==" + user);

        LinkedHashMap<String, Object> result = new LinkedHashMap<>(4);


        if (null == user) {
            log.info("user is null");
            result.put("code", -2000);
            result.put("msg", CodeMapper.codeToMsg(-2000));
            return result;
        }

        if (JwtAuthenticationFilter.isPasswordIllegal(user.getPassword())) {
            log.info("pwd is illegal");
            result.put("code", -2001);
            result.put("msg", CodeMapper.codeToMsg(-2001));
            return result;
        }

        if (null == user.getAccount() || user.getAccount().isEmpty()) {
            log.info("account is null or empty");
            result.put("code", -2002);
            result.put("msg", CodeMapper.codeToMsg(-2002));
            return result;
        }

        if (null == user.getPassword() || user.getPassword().isEmpty()) {
            log.info("pwd is null or empty");
            result.put("code", -2003);
            result.put("msg", CodeMapper.codeToMsg(-2003));
            return result;
        }


        try {
            String token = getToken(user.getAccount(),user.getPassword());
            log.info("token is=="+token);

            result.put("code", "0");
            result.put("msg", "success");
            result.put("token", token);
        } catch (BadCredentialsException e) {
            log.error("username or password error,see==" + e.getMessage(), e);
            result.put("code", -2004);
            result.put("msg", CodeMapper.codeToMsg(-2004));
            return result;
        } catch (Throwable t) {
            log.error("has an error,see==" + t.getMessage(), t);
            result.put("code", -1000);
            result.put("msg", CodeMapper.codeToMsg(-1000));
            return result;
        }

        log.info("result is==" + result);
        return result;
    }


    public String getToken(String account,String pwd) throws Throwable{

        log.info("account is=="+account+" pwd is=="+pwd);

        if (null==account) {
            log.info("username is null");
            throw new Exception("username can not be null");
        }


        try {
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(account, pwd);

            Authentication authentication = authenticationManager.authenticate(authenticationToken);
            log.info("authentication is ==" + authentication);

            if (null == authentication) {
                log.info("authentication is null");
                throw new Throwable("unknown exception");
            }

            SecurityContextHolder.getContext().setAuthentication(authentication);
            org.springframework.security.core.userdetails.User userDetails = (org.springframework.security.core.userdetails.User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            return  JwtUtil.createToken(authentication);
        }catch (BadCredentialsException e) {
            throw e;
        }catch (Throwable t) {
            log.error("has an error,see=="+t.getMessage(),t);
            throw t;
        }
    }


    @ApiOperation(value = "注册系统账号", notes = "注册账号")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "user", value = "用户实体user", required = true,
                    dataType = "User")
    })
    @PostMapping("/a/users/sign-up")
    public LinkedHashMap<String, Object> signUp(@RequestBody User user) {

        log.info("user is==" + user);

        LinkedHashMap<String, Object> result = new LinkedHashMap<>(4);

        //TODO 针对入参实体编写统一校验参数方法,并返回对应状态码,根据状态码能够做出相应处理
        if (null == user) {
            log.info("user is null");
            result.put("code", -2000);
            result.put("msg", CodeMapper.codeToMsg(-2000));
            return result;
        }

        if (JwtAuthenticationFilter.isPasswordIllegal(user.getPassword())) {
            log.info("pwd is illegal");
            result.put("code", -2001);
            result.put("msg", CodeMapper.codeToMsg(-2001));
            return result;
        }

        if (null == user.getName() || user.getName().isEmpty()) {
            log.info("username is null or empty");
            result.put("code", -2002);
            result.put("msg", CodeMapper.codeToMsg(-2002));
            return result;
        }

        if (null == user.getPassword() || user.getPassword().isEmpty()) {
            log.info("pwd is null or empty");
            result.put("code", -2003);
            result.put("msg", CodeMapper.codeToMsg(-2003));
            return result;
        }

        if (null==user.getAccount()||user.getAccount().isEmpty()) {
            log.info("account is null or empty");
            result.put("code",-2008);
            result.put("msg",CodeMapper.codeToMsg(-2008));
            return result;
        }



        try {
            if (null!=userRepository.findByAccount(user.getAccount())) {
                log.info("account already existent");
                result.put("code",-2006);
                result.put("msg",CodeMapper.codeToMsg(-2006));
                return result;
            }

            Role role = roleRepository.getByRole("ROLE_USER");
            log.info("role is==" + role);

            if (null == role) {
                log.info("role is null");
                result.put("code", -2005);
                result.put("msg", CodeMapper.codeToMsg(-2005));
                return result;
            }

            user.getRoles().add(role);
            user.setPassword(bCrypt.encode(user.getPassword()));
            user.setType(User.STUDENT);
            user = userRepository.save(user);
            user = userRepository.findByAccount(user.getAccount());

            result.put("code", 0);
            result.put("msg", CodeMapper.codeToMsg(0));
            //TODO 解决栈溢出
            user.setRoles(null);
            result.put("data", user);

            log.info("result is==" + result);


            return result;
        } catch (Throwable t) {
            log.error("has an error,see==" + t.getMessage(), t);
            result.put("code", -1000);
            result.put("msg", CodeMapper.codeToMsg(-1000));
            return result;
        }
    }


    @ApiOperation(value = "获取cas-server授权中心url",notes = "cas-server授权中心登录")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "casServerId",value = "cas-server id",required = true,dataType = "long",paramType = "path")
    })
    @GetMapping("/a/cas/{casServerId}")
    public LinkedHashMap<String,Object> getCasServerLocation(@PathVariable("casServerId") Long casServerId) {

        log.info("casServerId is=="+ casServerId);

        LinkedHashMap<String,Object> result = new LinkedHashMap<>(4);

        if (null==casServerId||0>=casServerId) {
            log.info("casServerId is null or zero");
            result.put("code", -3000);
            result.put("msg", CodeMapper.codeToMsg(-3000));
            return result;
        }

        try {
            Optional<CasServer> optional = casServerRepository.findById(casServerId);
            CasServer casServer = optional.orElse(new CasServer());
            log.info("casServer is=="+casServer);

            if (null == casServer.getId()) {
                log.info("cas server not found");
                result.put("code", -3001);
                result.put("msg", CodeMapper.codeToMsg(-3001));
                return result;
            }

            if (null==casServer.getCas_server_location()||casServer.getCas_server_location().isEmpty()) {

                log.info("cas server location is null or empty");
                result.put("code",-3002);
                result.put("msg",CodeMapper.codeToMsg(-3002));
                return result;
            }

            String url = casServer.getCas_server_location()+
                    "?"+CAS_CALLBACK_PARAM+"="+
                    CAS_CALLBACK_URL+casServerId;

            result.put("code",0);
            result.put("msg",CodeMapper.codeToMsg(0));
            result.put("url",url);

            log.info("result is=="+result);
            return result;
        }catch (Throwable t) {
            log.error("has an error,see=="+t.getMessage(),t);
            result.put("code", -1000);
            result.put("msg", CodeMapper.codeToMsg(-1000));
            return result;
        }
    }



    @ApiOperation(value = "cas 授权回调函数",notes = "请忽略该API")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "ticket",value = "cas ST",required = true,dataType = "string"),
            @ApiImplicitParam(name = "casServerId", value = "cas server id",required = true,dataType = "long",paramType = "path")
    })
    @RequestMapping(value = "/a/cas/callback/{casServerId}")
    public LinkedHashMap<String,Object> casCallback(@PathVariable("casServerId") Long casServerId, String ticket) {

        log.info("cas server id is=="+casServerId+" ticket is=="+ticket);

        LinkedHashMap<String,Object> result = new LinkedHashMap<>(4);

        if (null==casServerId||0>=casServerId) {
            log.info("cas server illegal");
            result.put("code",-3000);
            result.put("msg",CodeMapper.codeToMsg(-3000));
            return result;
        }

        if (null==ticket||ticket.isEmpty()) {
            log.info("ticket illegal");
            result.put("code",-3003);
            result.put("msg",CodeMapper.codeToMsg(-3003));
            return result;
        }

        try {
            CasServer casServer = casServerRepository.findById(casServerId).orElse(null);

            if (null == casServer) {
                log.info("casServer not found");
                result.put("code", -3001);
                result.put("msg", CodeMapper.codeToMsg(-3001));
                return result;
            }

            if (null == casServer.getCas_server_location() || casServer.getCas_server_location().isEmpty()) {
                log.info("got cas server, but cas server location not found");
                result.put("code", -3002);
                result.put("msg", CodeMapper.codeToMsg(-3002));
                return result;
            }


            Map<String,Object> info = verifyTicket(ticket,casServer);


            if (!info.get(CasResponseResolveUtil.STATUS).equals("success")) {
                log.info("verify ST failed,cause=="+info.get(CasResponseResolveUtil.MSG));
                result.put("code",-3004);
                result.put("msg", CodeMapper.codeToMsg(-3004));
                return result;
            }

            User user = getUser(info,casServerId);
            String token = getToken(user.getAccount(),null);

            result.put("code",0);
            result.put("msg",CodeMapper.codeToMsg(0));
            result.put("token",token);

            return result;
        }catch (Throwable t) {
            log.error("has an error,see=="+t.getMessage(),t);
            result.put("code",-1000);
            result.put("msg",CodeMapper.codeToMsg(-1000));
            return result;
        }
    }


    /**
     *
     * @param ticket
     * @param casServer
     * @return
     * @throws Throwable
     */
    private Map<String,Object> verifyTicket(String ticket,CasServer casServer) throws Throwable{

        String realUrl = casServer.getCas_server_location() + "/serviceValidate" + "?ticket="
                + ticket + "&" + CAS_CALLBACK_PARAM + "=" + CAS_CALLBACK_URL + casServer.getId();


        try {
            HttpEntity entity = HttpReqUtil.sendGetRequest(realUrl, null);
            log.info("entity is==" + entity);

            return CasResponseResolveUtil.resolveResponse(entity);
        }catch (Throwable t) {
            throw t;
        }
    }

    /**
     *
     * @param info
     * @return
     */
    private User getUser(Map<String,Object> info,Long casServerId) throws Throwable{

        String username = (String) info.get(CasResponseResolveUtil.USERNAME);
        log.info("username is=="+username);
        String account = (String) info.get(CasResponseResolveUtil.ACCOUNT);
        log.info("account is=="+account);

        if (null==account) {
            log.info("account is null");
            throw new Throwable("account can not be null");
        }

        ThirdPartyAccount thirdPartyAccount = thirdPartyAccountRepository.findByAccountAndAccountType(
                account, ThirdPartyAccount.CAS
        );
        log.info("thirdPartyAccount is=="+thirdPartyAccount);

        User user = null;

        try {
            if (null == thirdPartyAccount) {
                log.info("first use thirty party account to login");
                User userArgs = new User();
                Role role = roleRepository.getByRole("ROLE_USER");
                userArgs.setName(username);
                userArgs.setAccount(account);
                userArgs.setPassword(bCrypt.encode("123456"));
                userArgs.getRoles().add(role);
                userArgs.setType(User.STUDENT);

                user = userRepository.save(userArgs);

                ThirdPartyAccount tpaArgs = new ThirdPartyAccount();
                tpaArgs.setAccount(account);
                tpaArgs.setAccountType(ThirdPartyAccount.CAS);
                tpaArgs.setCasServerTargetId(casServerId);
                tpaArgs.setTargetType(ThirdPartyAccount.STUDENT);
                tpaArgs.setTargetId(user.getId());

                thirdPartyAccountRepository.save(tpaArgs);
            } else {
                log.info("already has system account");
                user = userRepository.findById(thirdPartyAccount.getTargetId()).orElse(null);
            }
        }catch (Throwable t) {
            log.error("has an error,see=="+t.getMessage(),t);
            throw t;
        }


        log.info("user is=="+user);

        return user;
    }



    @ApiOperation(value = "sign out")
    @ApiImplicitParams({
            @ApiImplicitParam(name = SecurityConsole.HEADER_STRING,value = "jwt tokwn", required = true,dataType = "string",paramType = "header")
    })
    @DeleteMapping(value = "/a/u/sign-out")
    @PreAuthorize("hasAnyRole('ROLE_USER','ROLE_ADMIN')")
    public LinkedHashMap<String,Object> signOut(HttpServletRequest request) {

        LinkedHashMap<String,Object> result = new LinkedHashMap<>(4);
        valueOperations = redisTemplate.opsForValue();


        try{

            String token = JwtUtil.getToken(request);
            log.info("token is=="+token);

            Long residueTime = JwtUtil.getResidueTime(token);
            log.info("residueTime is=="+residueTime);



            valueOperations.set(JwtUtil.BLACK_LIST_PREFIX+token,token,residueTime,TimeUnit.MILLISECONDS);
            result.put("code",0);
            result.put("msg",CodeMapper.codeToMsg(0));
        } catch (Throwable t) {
            log.error("has an error,see=="+t.getMessage(),t);
            result.put("code",-1000);
            result.put("msg", CodeMapper.codeToMsg(-1000));
            return result;
        }

        return result;
    }
}
