package com.himly.api.model;

public enum Continent {
    AS("亚洲"),
    EU("欧洲"),
    AF("非洲"),
    NA("北美洲"),
    SA("南美洲"),
    OC("大洋洲"),
    AN("南极洲");

    private String name;

    Continent(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
