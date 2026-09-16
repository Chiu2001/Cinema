package com.example.backend.Controller;

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.DTO.SeatDTO;
import com.example.backend.Service.SeatService;

/**
 * Seat reservation and lookup.
 * Split out of the original CinemaController to keep responsibilities focused.
 */
@RestController
@RequestMapping("/api/movie")
public class SeatController {

	@Autowired
	private SeatService seatService;

	@PutMapping("/save/{showtimeId}/{seatNumber}")
	public ResponseEntity<String> addSeat(@PathVariable Integer showtimeId,
			@PathVariable String seatNumber,
			@RequestBody SeatDTO seatDTO) {
		try {
			// Check that the SeatDto isn't null
			if (seatDTO == null) {
				return ResponseEntity.badRequest().body("SeatDto cannot be null");
			}

			// Fill in the DTO's remaining details
			seatDTO.setShowtimeId(showtimeId);
			seatDTO.setSeatNumber(seatNumber);

			// Call SeatService to persist the seat info; other foreign keys are resolved on the backend
			seatService.addSeat(seatDTO);

			// Return a success response
			return ResponseEntity.ok("Seat information saved successfully");

		} catch (Exception e) {
			// Catch and handle the exception, returning a 500 status and error message
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Error saving seat information: " + e.getMessage());
		}
	}

	@GetMapping("/seatsResearch/{showtimeId}/{cinemaId}/{hallId}/{showDate}")
	public ResponseEntity<List<SeatDTO>> getSeats(
			@PathVariable Integer showtimeId,
			@PathVariable Integer cinemaId,
			@PathVariable Integer hallId,
			@PathVariable String showDate) {

		try {
			// Call the service layer to fetch seat information
			List<SeatDTO> seats = seatService.getSeatsByShowtimeCinemaAndHallAndDate(showtimeId, cinemaId, hallId,
					showDate);

			// Return a success response with the seat information
			return ResponseEntity.ok(seats);

		} catch (Exception e) {
			// Catch and handle the exception, returning a 500 status and error message
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Collections.emptyList()); // Return an empty list to indicate no data
		}
	}

	@GetMapping("/seatsResearch/{showtimeId}")
	public ResponseEntity<List<SeatDTO>> getSeatsByShowtimeId(@PathVariable Integer showtimeId) {
		try {
			// Look up the cinemaId, hallId, and showDate associated with this showtimeId
			List<SeatDTO> seats = seatService.getSeatsByShowtimeId(showtimeId);

			// Return the seat information
			return ResponseEntity.ok(seats);

		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Collections.emptyList()); // Return an empty list to indicate no data
		}
	}
}
