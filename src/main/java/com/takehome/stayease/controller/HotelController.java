package com.takehome.stayease.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.takehome.stayease.dto.HotelCreateRequest;
import com.takehome.stayease.dto.HotelResponse;
import com.takehome.stayease.dto.HotelUpdateRequest;
import com.takehome.stayease.entity.Hotel;
import com.takehome.stayease.security.JwtTokenProvider;
import com.takehome.stayease.service.HotelService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/hotels")
public class HotelController {

    private static final Logger logger = LoggerFactory.getLogger(HotelController.class);

    private final HotelService hotelService;
    private final JwtTokenProvider jwtTokenProvider;

    public HotelController(HotelService hotelService, JwtTokenProvider jwtTokenProvider) {
        this.hotelService = hotelService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @GetMapping
    public ResponseEntity<List<HotelResponse>> getAllHotels() {
        logger.info("Fetching all hotels");
        List<HotelResponse> hotels = hotelService.getAllHotels();
        logger.info("Found {} hotels", hotels.size());
        return ResponseEntity.ok(hotels);
    }

    @GetMapping("/{id}")
    public ResponseEntity<HotelResponse> getHotelById(@PathVariable Long id) {
        logger.info("Fetching hotel with ID: {}", id);
        HotelResponse hotel = hotelService.getHotelById(id);
        logger.info("Fetched hotel: {}", hotel);
        return ResponseEntity.ok(hotel);
    }

    @PostMapping
    public ResponseEntity<?> createHotel(
        @Valid @RequestBody HotelCreateRequest hotelCreateRequest,
        HttpServletRequest request) {

        logger.info("Creating new hotel with request: {}", hotelCreateRequest);

        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            logger.warn("Authorization token is missing or invalid");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authorization token is missing or invalid");
        }

        String token = authorizationHeader.substring(7);

        // Check if the token is valid
        if (!jwtTokenProvider.validateToken(token)) {
            logger.warn("Invalid or expired token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired token");
        }

        String userRole = jwtTokenProvider.getRoleFromToken(token);

        if (!"ADMIN".equals(userRole)) {
            logger.warn("Access denied: Only ADMIN can create hotels");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Access denied: Only ADMIN can update hotels");
        }

        Hotel hotelRequest = new Hotel();
        hotelRequest.setName(hotelCreateRequest.getName());
        hotelRequest.setLocation(hotelCreateRequest.getLocation());
        hotelRequest.setDescription(hotelCreateRequest.getDescription());
        hotelRequest.setTotalRooms(hotelCreateRequest.getAvailableRooms());

        HotelResponse hotel = hotelService.createHotel(hotelRequest);
        logger.info("Hotel created successfully: {}", hotel);
        return ResponseEntity.ok(hotel);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateHotel(@PathVariable Long id,@Valid @RequestBody HotelUpdateRequest hotelUpdateRequest,
            HttpServletRequest request) {

        logger.info("Updating hotel with ID: {} with request: {}", id, hotelUpdateRequest);

        // Extract JWT token from Authorization header
        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            logger.warn("Authorization token is missing or invalid");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authorization token is missing or invalid");
        }

        String token = authorizationHeader.substring(7);

        // Check if the token is valid
        if (!jwtTokenProvider.validateToken(token)) {
            logger.warn("Invalid or expired token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired token");
        }

        // Extract user role from token
        String userRole = jwtTokenProvider.getRoleFromToken(token);
        logger.debug("User role extracted from token: {}", userRole);

        if (!"HOTEL_MANAGER".equals(userRole)) {
            logger.warn("Access denied: Only HOTEL_MANAGER can update hotels");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Access denied: Only HOTEL_MANAGER can update hotels");
        }

        // Proceed with updating the hotel
        hotelUpdateRequest.setId(id);
        HotelResponse updatedHotel = hotelService.updateHotel(hotelUpdateRequest);
        logger.info("Hotel updated successfully: {}", updatedHotel);
        return ResponseEntity.ok(updatedHotel);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteHotel(@PathVariable Long id, HttpServletRequest request) {
        logger.info("Deleting hotel with ID: {}", id);

        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            logger.warn("Authorization token is missing or invalid");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authorization token is missing or invalid");
        }

        String token = authorizationHeader.substring(7);

        // Check if the token is valid
        if (!jwtTokenProvider.validateToken(token)) {
            logger.warn("Invalid or expired token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired token");
        }

        String userRole = jwtTokenProvider.getRoleFromToken(token);
        logger.debug("User role extracted from token: {}", userRole);

        if (!"ADMIN".equals(userRole)) {
            logger.warn("Access denied: Only ADMIN can delete hotels");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Access denied: Only ADMIN can delete hotels");
        }

        hotelService.deleteHotel(id);
        logger.info("Hotel with ID {} deleted successfully", id);
        return ResponseEntity.noContent().build();
    }

}
