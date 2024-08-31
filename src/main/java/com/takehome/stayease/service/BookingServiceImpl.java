package com.takehome.stayease.service;

import java.time.LocalDate;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.takehome.stayease.dto.BookingRequest;
import com.takehome.stayease.dto.BookingResponse;
import com.takehome.stayease.entity.Booking;
import com.takehome.stayease.entity.Hotel;
import com.takehome.stayease.entity.User;
import com.takehome.stayease.exception.BookingNotFoundException;
import com.takehome.stayease.exception.HotelNotFoundException;
import com.takehome.stayease.exception.RoomUnavailableException;
import com.takehome.stayease.repository.BookingRepository;
import com.takehome.stayease.repository.HotelRepository;
import com.takehome.stayease.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

@Service
public class BookingServiceImpl implements BookingService {

    private static final Logger logger = LoggerFactory.getLogger(BookingServiceImpl.class);

    private final BookingRepository bookingRepository;
    private final HotelRepository hotelRepository;
    private final UserRepository userRepository;

    public BookingServiceImpl(BookingRepository bookingRepository, HotelRepository hotelRepository, UserRepository userRepository) {
        this.bookingRepository = bookingRepository;
        this.hotelRepository = hotelRepository;
        this.userRepository = userRepository;
    }

    public BookingResponse bookRoom(Long hotelId, BookingRequest bookingRequest) {
        logger.info("Attempting to book room in hotel with ID: {}", hotelId);

        Hotel hotel = hotelRepository.findById(hotelId)
            .orElseThrow(() -> {
                logger.error("Hotel not found with ID: {}", hotelId);
                return new HotelNotFoundException("Hotel not found.");
            });

        LocalDate checkInDate = bookingRequest.getCheckInDate();
        LocalDate checkOutDate = bookingRequest.getCheckOutDate();
        List<Booking> existingBookings = bookingRepository.findBookingsByHotelAndDateRange(hotelId, checkInDate, checkOutDate);

        if (existingBookings.size() >= hotel.getTotalRooms()) {
            logger.warn("No rooms available for the specified date range at hotel ID: {}", hotelId);
            throw new RoomUnavailableException("No rooms available for the specified date range.");
        }

        User currentUser = userRepository.findByEmail(bookingRequest.getUserEmail())
            .orElseThrow(() -> {
                logger.error("User not found with email: {}", bookingRequest.getUserEmail());
                return new EntityNotFoundException("User not found with email: " + bookingRequest.getUserEmail());
            });

        // Create new booking
        Booking booking = new Booking();
        booking.setHotel(hotel);
        booking.setUser(currentUser);
        booking.setCheckInDate(checkInDate);
        booking.setCheckOutDate(checkOutDate);

        // Save booking
        Booking savedBooking = bookingRepository.save(booking);

        logger.info("Room successfully booked with booking ID: {} for user email: {}", savedBooking.getId(), bookingRequest.getUserEmail());

        // Prepare response
        return new BookingResponse(
            savedBooking.getId(),
            hotel.getId(),
            hotel.getName(),
            savedBooking.getUser().getEmail(),
            savedBooking.getCheckInDate(),
            savedBooking.getCheckOutDate()
        );
    }

    @Override
    public BookingResponse findById(Long id) throws BookingNotFoundException {
        logger.info("Finding booking with ID: {}", id);

        Booking booking = bookingRepository.findById(id)
            .orElseThrow(() -> {
                logger.error("Booking not found with ID: {}", id);
                return new BookingNotFoundException("Booking not found with id: " + id);
            });

        logger.info("Booking found with ID: {}", id);

        return new BookingResponse(
            booking.getId(),
            booking.getHotel().getId(),
            booking.getHotel().getName(),
            booking.getUser().getEmail(),
            booking.getCheckInDate(),
            booking.getCheckOutDate()
        );
    }

    @Override
    @Transactional
    public void deleteBooking(Long bookingId) {
        logger.info("Attempting to delete booking with ID: {}", bookingId);

        Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> {
                logger.error("Booking not found with ID: {}", bookingId);
                return new EntityNotFoundException("Booking not found");
            });

        // Delete the booking
        bookingRepository.delete(booking);

        logger.info("Successfully deleted booking with ID: {}", bookingId);
    }
}
