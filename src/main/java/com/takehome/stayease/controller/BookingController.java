package com.takehome.stayease.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.takehome.stayease.dto.BookingRequest;
import com.takehome.stayease.dto.BookingResponse;
import com.takehome.stayease.security.JwtTokenProvider;
import com.takehome.stayease.service.BookingService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private static final Logger logger = LoggerFactory.getLogger(BookingController.class);

    private final BookingService bookingService;
    private final JwtTokenProvider jwtTokenProvider;

    public BookingController(BookingService bookingService, JwtTokenProvider jwtTokenProvider) {
        this.bookingService = bookingService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostMapping("/{hotelId}")
    public ResponseEntity<?> bookRoom(@PathVariable Long hotelId, @Valid @RequestBody BookingRequest bookingRequest, HttpServletRequest request) {
        logger.info("Received booking request for hotelId: {}", hotelId);

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

        String userEmail = jwtTokenProvider.getUsername(token);
        logger.debug("Extracted userEmail from token: {}", userEmail);
        bookingRequest.setUserEmail(userEmail);

        BookingResponse bookingResponse = bookingService.bookRoom(hotelId, bookingRequest);
        logger.info("Successfully booked room for hotelId: {} by user: {}", hotelId, userEmail);
        return ResponseEntity.ok(bookingResponse);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getBookingById(@PathVariable Long id, HttpServletRequest request) {
        logger.info("Fetching booking details for bookingId: {}", id);

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

        BookingResponse bookingResponse = bookingService.findById(id);
        logger.info("Successfully fetched booking details for bookingId: {}", id);
        return ResponseEntity.ok(bookingResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBooking(@PathVariable Long id, HttpServletRequest request) {
        logger.info("Received request to delete booking with bookingId: {}", id);

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
        logger.debug("Extracted userRole from token: {}", userRole);
        if (!"HOTEL_MANAGER".equals(userRole)) {
            logger.warn("Access denied: Only HOTEL_MANAGER can delete bookings");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Access denied: Only HOTEL_MANAGER can delete bookings");
        }

        bookingService.deleteBooking(id);
        logger.info("Successfully deleted booking with bookingId: {}", id);
        return ResponseEntity.noContent().build(); // Return HTTP 204 No Content
    }
}
