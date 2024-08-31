package com.takehome.stayease.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BookingResponse {

    private Long bookingId;
    private Long hotelId;
    private String hotelName;
    private String bookedBy;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
}
