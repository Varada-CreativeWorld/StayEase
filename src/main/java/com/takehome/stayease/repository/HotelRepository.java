package com.takehome.stayease.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.takehome.stayease.entity.Hotel;

public interface HotelRepository extends JpaRepository<Hotel, Long> {
}

