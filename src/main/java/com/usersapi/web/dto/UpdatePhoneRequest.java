package com.usersapi.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Запрос на обновление телефона")
public class UpdatePhoneRequest {

    @Schema(description = "Номер телефона", example = "+7-999-123-45-67")
    private String number;

    @Schema(description = "Бренд телефона", example = "Samsung")
    private String brand;

    public String getNumber() { return number; }
    public void setNumber(String number) { this.number = number; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
}