package com.takehome.stayease.dto;

import java.time.LocalDate;

import com.takehome.stayease.validation.ValidLocalDate;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BookingRequest {

    @NotNull(message = "Check-in date is mandatory")
    @ValidLocalDate
    private LocalDate checkInDate;

    @NotNull(message = "Check-out date is mandatory")
    @ValidLocalDate
    private LocalDate checkOutDate;

    private String userEmail;
}
