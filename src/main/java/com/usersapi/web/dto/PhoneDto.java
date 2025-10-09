package com.usersapi.web.dto;

import jakarta.validation.constraints.NotBlank;

public class PhoneDto {
    @NotBlank
    private String number;

    @NotBlank
    private String brand;

    public PhoneDto() {}
    public PhoneDto(String number, String brand) {
        this.number = number;
        this.brand = brand;
    }

    public String getNumber() { return number; }
    public void setNumber(String number) { this.number = number; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
}