package com.example.backend.Controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.Entity.Cinema;
import com.example.backend.Entity.Hall;
import com.example.backend.Entity.Showdate;
import com.example.backend.Entity.Showtime;
import com.example.backend.Repo.CinemaRepo;
import com.example.backend.Repo.HallRepo;
import com.example.backend.Repo.ShowtimeRepo;
import com.example.backend.Service.ShowtimeService;

/**
 * Cinema, hall, show date, and showtime queries.
 * Split out of the original CinemaController to keep responsibilities focused.
 */
@RestController
@RequestMapping("/api/movie")
public class CinemaScheduleController {

	@Autowired
	private CinemaRepo cinemaRepo;

	@Autowired
	private HallRepo hallRepo;

	@Autowired
	private ShowtimeRepo showtimeRepo;

	@Autowired
	private ShowtimeService showtimeService;

	@GetMapping("/cinemas")
	public ResponseEntity<List<Cinema>> getAllCinemas() {
		try {
			List<Cinema> cinemas = cinemaRepo.findAll();
			return new ResponseEntity<>(cinemas, HttpStatus.OK);
		} catch (Exception e) {
			System.err.println("Error fetching cinemas: " + e.getMessage());
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping("/halls")
	public List<Hall> getAllHalls() {
		return hallRepo.findAll();
	}

	@GetMapping("/halls/{hallId}")
	public Hall getHallById(@PathVariable("hallId") int hallId) {
		return hallRepo.findById(hallId).orElse(null);
	}

	@GetMapping("/cinemas/{cinemaId}/showdates")
	public ResponseEntity<List<Showdate>> getShowdates(@PathVariable Integer cinemaId) {
		try {
			Cinema cinema = new Cinema();
			cinema.setCinema_id(cinemaId);

			List<Showtime> showtimes = showtimeRepo.findDistinctBycinema(cinema);
			List<Showdate> showdates = showtimes.stream()
					.map(Showtime::getShowDate)
					.distinct()
					.collect(Collectors.toList());
			return ResponseEntity.ok(showdates);
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(500).build();
		}
	}

	@GetMapping("/cinemas/{cinemaId}/showtimes/{showDateId}")
	public ResponseEntity<List<Showtime>> getShowtimes(@PathVariable Integer cinemaId,
			@PathVariable Integer showDateId) {
		try {
			Cinema cinema = new Cinema();
			cinema.setCinema_id(cinemaId);

			Showdate showDate = new Showdate();
			showDate.setShowdate_id(showDateId);

			System.out.println(showDateId);
			List<Showtime> showtimes = showtimeRepo.findByCinemaAndShowDate(cinema, showDate);
			return ResponseEntity.ok(showtimes);
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(500).build();
		}
	}

	// Push data to the frontend based on showtime_id
	@GetMapping("/area/{showtimeId}")
	public ResponseEntity<Showtime> getShowtimeById(@PathVariable Integer showtimeId) {
		Showtime showtime = showtimeService.getShowtimeById(showtimeId);
		if (showtime != null) {
			return ResponseEntity.ok(showtime);
		} else {
			return ResponseEntity.notFound().build();
		}
	}
}
