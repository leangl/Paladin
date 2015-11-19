package com.nanospark.gard.weather;

import java.io.Serializable;

/**
 * Created by Leandro on 17/11/2015.
 */
public class City implements Serializable {

    private Long id;
    private String name;
    private String country;
    private String zipCode;

    public City() {
    }

    public City(String country, Long id, String name, String zipCode) {
        this.country = country;
        this.id = id;
        this.name = name;
        this.zipCode = zipCode;
    }

    public String getCountry() {
        return country;
    }
    public void setCountry(String country) {
        this.country = country;
    }
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getZipCode() {
        return zipCode;
    }
    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        City city = (City) o;

        return id.equals(city.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return name + ", " + country;
    }
}
