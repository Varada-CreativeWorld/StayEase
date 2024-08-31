package com.takehome.stayease.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.takehome.stayease.dto.HotelResponse;
import com.takehome.stayease.dto.HotelUpdateRequest;
import com.takehome.stayease.entity.Booking;
import com.takehome.stayease.entity.Hotel;
import com.takehome.stayease.entity.User;
import com.takehome.stayease.exception.HotelNotFoundException;
import com.takehome.stayease.repository.BookingRepository;
import com.takehome.stayease.repository.HotelRepository;
import com.takehome.stayease.repository.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class HotelServiceImpl implements HotelService {

    private static final Logger logger = LoggerFactory.getLogger(HotelServiceImpl.class);

    private final HotelRepository hotelRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;

    @Autowired
    public HotelServiceImpl(HotelRepository hotelRepository, UserRepository userRepository, BookingRepository bookingRepository) {
        this.hotelRepository = hotelRepository;
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
    }

    @Override
    public HotelResponse createHotel(Hotel hotelRequest) {
        logger.info("Creating hotel with details: {}", hotelRequest);

        Hotel hotel = hotelRepository.save(hotelRequest);
        HotelResponse response = convertToHotelResponse(hotel);

        logger.info("Hotel created successfully with ID: {}", hotel.getId());
        return response;
    }

    @Override
    public List<HotelResponse> getAllHotels() {
        logger.info("Retrieving all hotels");

        List<Hotel> hotels = hotelRepository.findAll();
        List<HotelResponse> responses = hotels.stream()
            .map(this::convertToHotelResponse)
            .collect(Collectors.toList());

        logger.info("Retrieved {} hotels", responses.size());
        return responses;
    }

    @Override
    public HotelResponse getHotelById(Long id) {
        logger.info("Retrieving hotel with ID: {}", id);

        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Hotel not found with ID: {}", id);
                    return new HotelNotFoundException("Hotel not found with id: " + id);
                });

        HotelResponse response = convertToHotelResponse(hotel);
        logger.info("Hotel retrieved with ID: {}", id);
        return response;
    }

    @Override
    public HotelResponse updateHotel(HotelUpdateRequest hotelUpdateRequest) {
        logger.info("Updating hotel with ID: {}", hotelUpdateRequest.getId());

        Hotel hotel = hotelRepository.findById(hotelUpdateRequest.getId())
                .orElseThrow(() -> {
                    logger.error("Hotel not found with ID: {}", hotelUpdateRequest.getId());
                    return new HotelNotFoundException("Hotel not found with id: " + hotelUpdateRequest.getId());
                });

        if (hotelUpdateRequest.getName() != null) {
            hotel.setName(hotelUpdateRequest.getName());
        }
        if (hotelUpdateRequest.getLocation() != null) {
            hotel.setLocation(hotelUpdateRequest.getLocation());
        }
        if (hotelUpdateRequest.getDescription() != null) {
            hotel.setDescription(hotelUpdateRequest.getDescription());
        }
        if (hotelUpdateRequest.getAvailableRooms() != null) {
            hotel.setTotalRooms(hotelUpdateRequest.getAvailableRooms());
        }

        Hotel updatedHotel = hotelRepository.save(hotel);
        HotelResponse response = convertToHotelResponse(updatedHotel);

        logger.info("Hotel updated successfully with ID: {}", hotel.getId());
        return response;
    }

    @Transactional
    @Override
    public void deleteHotel(Long hotelId) {
        logger.info("Attempting to delete hotel with ID: {}", hotelId);

        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> {
                    logger.error("Hotel not found with ID: {}", hotelId);
                    return new HotelNotFoundException("Hotel not found");
                });

        List<Booking> bookings = hotel.getBookings();

        for (Booking booking : bookings) {
            User user = booking.getUser();
            user.getBookings().remove(booking);
            userRepository.save(user);
        }

        bookingRepository.deleteAll(bookings);
        hotelRepository.delete(hotel);

        logger.info("Hotel and its bookings deleted successfully with ID: {}", hotelId);
    }

    private HotelResponse convertToHotelResponse(Hotel hotel) {
        HotelResponse dto = new HotelResponse();
        dto.setId(hotel.getId());
        dto.setName(hotel.getName());
        dto.setLocation(hotel.getLocation());
        dto.setDescription(hotel.getDescription());
        dto.setAvailableRooms(hotel.getTotalRooms());

        List<Long> bookingIds = (hotel.getBookings() != null) ?
            hotel.getBookings().stream()
                .map(Booking::getId)
                .collect(Collectors.toList()):
            new ArrayList<>();
        dto.setBookingIDs(bookingIds);

        return dto;
    }
}
