package com.himly.api.runner;


import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.himly.api.model.Continent;
import com.himly.api.model.Country;
import com.himly.api.model.University;
import com.himly.api.repository.CountryRepository;
import com.himly.api.repository.UniversityRepository;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
public class DataSeeder implements ApplicationRunner {

    private CountryRepository countryRepository;
    private UniversityRepository universityRepository;

    @Autowired
    public DataSeeder(CountryRepository countryRepository, UniversityRepository universityRepository) {
        this.countryRepository = countryRepository;
        this.universityRepository = universityRepository;
    }

    public void run(ApplicationArguments args) {
//        importCountries();
//        importUniversities();
    }

    private void importCountries() {
        ClassLoader classLoader = getClass().getClassLoader();
        try {
            Reader in = new FileReader(classLoader.getResource("seed/country_continent.csv").getFile());
            Iterable<CSVRecord> records = CSVFormat.DEFAULT.withHeader().parse(in);
            List<Country> countries = new ArrayList<>();
            for (CSVRecord record: records) {
                String countryCode = record.get("iso 3166 country");
                String continentCode = record.get("continent code");
                try {
                    Country country = new Country();
                    country.setCode(countryCode);
                    country.setContinent(Continent.valueOf(continentCode));

                    Locale locale = new Locale("", countryCode);
                    country.setName(locale.getDisplayCountry(Locale.CHINA));
                    countries.add(country);
                } catch (IllegalArgumentException e) {

                }
            };
            countryRepository.saveAll(countries);
            in.close();
        } catch (IOException e) {

        }
    }

    private void importUniversities() {
        ClassLoader classLoader = getClass().getClassLoader();
        try {
            Reader in = new FileReader(classLoader.getResource("seed/universities.json").getFile());
            JsonElement root = new JsonParser().parse(in);
            JsonArray countries = root.getAsJsonArray();
            countries.forEach(countryElement -> {
                JsonObject countryObj = countryElement.getAsJsonObject();
                String countryName = countryObj.get("name").getAsString();
                Country country = countryRepository.findByName(countryName);

                JsonElement universitiesElement = countryObj.get("univs");
                try {
                    JsonArray universityList = universitiesElement.getAsJsonArray();
                    importUniversities(universityList, country);
                } catch (IllegalStateException e) {
                    JsonElement provincesElement = countryObj.get("provs");
                    provincesElement.getAsJsonArray().forEach(province -> {
                        JsonElement innerUniversitiesElement = province.getAsJsonObject().get("univs");
                        importUniversities(innerUniversitiesElement.getAsJsonArray(), country);
                    });
                }
            });
            in.close();
        } catch (IOException e) {

        }
    }

    private void importUniversities(JsonArray universitiesArray, Country country) {
        List<University> universities = new ArrayList<>();
        universitiesArray.forEach(universityElement -> {
            String name = universityElement.getAsJsonObject().get("name").getAsString();
            University university = new University();
            university.setName(name);
            university.setCountry(country);
            universities.add(university);
        });

        
        universityRepository.saveAll(universities);
    }
}
