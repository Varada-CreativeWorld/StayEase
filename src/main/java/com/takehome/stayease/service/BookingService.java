package com.takehome.stayease.service;

import com.takehome.stayease.dto.BookingRequest;
import com.takehome.stayease.dto.BookingResponse;
import com.takehome.stayease.exception.BookingNotFoundException;

public interface BookingService {

    BookingResponse bookRoom(Long hotelId, BookingRequest bookingRequest);

    void deleteBooking(Long bookingId);

    BookingResponse findById(Long id) throws BookingNotFoundException;
}

