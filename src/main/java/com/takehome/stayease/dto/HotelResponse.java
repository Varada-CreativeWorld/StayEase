package com.takehome.stayease.dto;

import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class HotelResponse {
    private Long id;
    private String name;
    private String location;
    private String description;
    private int availableRooms;
    private List<Long> bookingIDs;
}
