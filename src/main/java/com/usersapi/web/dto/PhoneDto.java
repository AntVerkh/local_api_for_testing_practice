package com.usersapi.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "DTO телефона")
public class PhoneDto {

    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    @Pattern(regexp = "^[\\d\\+\\-\\(\\)\\s]*$", message = "Phone number can only contain digits, +, -, (, ) and spaces")
    @Schema(description = "Номер телефона", example = "+7-999-123-45-67")
    private String number;

    @Size(max = 50, message = "Brand must not exceed 50 characters")
    @Schema(description = "Бренд телефона", example = "Samsung")
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