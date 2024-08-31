package com.takehome.stayease.service;

import java.util.List;

import com.takehome.stayease.dto.HotelResponse;
import com.takehome.stayease.dto.HotelUpdateRequest;
import com.takehome.stayease.entity.Hotel;


public interface HotelService {

    List<HotelResponse> getAllHotels();

    HotelResponse getHotelById(Long id);

    HotelResponse createHotel(Hotel hotelCreateRequest);

    HotelResponse updateHotel(HotelUpdateRequest hotelUpdateRequest);

    void deleteHotel(Long id);
}
