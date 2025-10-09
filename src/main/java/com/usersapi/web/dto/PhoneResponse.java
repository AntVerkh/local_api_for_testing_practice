package com.usersapi.web.dto;

public class PhoneResponse {
    private Integer id;
    private String number;
    private String brand;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getNumber() { return number; }
    public void setNumber(String number) { this.number = number; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
}
