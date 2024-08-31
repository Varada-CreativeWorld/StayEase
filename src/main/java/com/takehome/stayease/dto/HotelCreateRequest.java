package com.takehome.stayease.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class HotelCreateRequest {

    @NotBlank(message = "Hotel name is mandatory")
    private String name;

    @NotBlank(message = "Location is mandatory")
    private String location;

    private String description;

    @Positive(message = "Number of available rooms must be positive")
    private int availableRooms;
}

