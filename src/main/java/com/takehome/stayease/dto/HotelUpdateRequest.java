package com.takehome.stayease.dto;

import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class HotelUpdateRequest {

    private Long id;
    private String name;
    private String location;
    private String description;
    
    @Positive(message = "Number of available rooms must be positive")
    private Integer availableRooms;
}

