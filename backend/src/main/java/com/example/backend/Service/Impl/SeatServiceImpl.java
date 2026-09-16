package com.example.backend.Service.Impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.backend.DTO.SeatDTO;
import com.example.backend.Entity.Cinema;
import com.example.backend.Entity.Hall;
import com.example.backend.Entity.Seat;
import com.example.backend.Entity.Showtime;
import com.example.backend.Repo.CinemaRepo;
import com.example.backend.Repo.HallRepo;
import com.example.backend.Repo.SeatRepo;
import com.example.backend.Repo.ShowtimeRepo;
import com.example.backend.Service.SeatService;

@Service
public class SeatServiceImpl implements SeatService{

	@Autowired
	private SeatRepo seatRepo;

	@Autowired
	private ShowtimeRepo showtimeRepo;

	@Autowired
	private CinemaRepo cinemaRepo;

	@Autowired
	private HallRepo hallRepo;
	
	// Convert the DTO to an entity and save it to the database
	public void addSeat(SeatDTO seatDTO) {
		// Look up the Showtime record by showtimeId
		Showtime showtime = showtimeRepo.findById(seatDTO.getShowtimeId())
				.orElseThrow(() -> new RuntimeException("Showtime not found"));

		// Automatically resolve the related Cinema and Hall from the showtime
		Cinema cinema = showtime.getCinema();
		Hall hall = showtime.getHall();

		// Check whether this seat already exists
		Seat seat = seatRepo.findByShowtimeIdAndCinemaIdAndHallIdAndSeatNumber(showtime,
				cinema, hall, seatDTO.getSeatNumber());

		if (seat == null) {
			// Create a new Seat entity if it does not exist yet
			seat = new Seat();
			seat.setSeatNumber(seatDTO.getSeatNumber());
			seat.setSeatAvailability(seatDTO.getSeatAvailability());
			seat.setShowtimeId(showtime);
			seat.setCinemaId(cinema);
			seat.setHallId(hall);
		} else {
			// Update the seat status if it already exists
			seat.setSeatAvailability(false);
		}

		seatRepo.save(seat); // Save to the database
	}

	// Get seat information for a specific showtime, cinema, date, and hall
	public List<SeatDTO> getSeatsByShowtimeCinemaAndHallAndDate(Integer showtimeId, Integer cinemaId, Integer hallId,
			String showDate) {

		List<Seat> seatEntities = seatRepo.findByShowtimeCinemaHallAndDate(showtimeId, cinemaId,
				hallId, showDate);

		// Convert the query results to SeatDto
		List<SeatDTO> seatDtos = seatEntities.stream()
				.map(entity -> new SeatDTO(entity.getSeatNumber(), entity.getSeatAvailability(),
						entity.getShowtimeId().getShowtime_id(), entity.getCinemaId().getCinema_id(),
						entity.getHallId().getHall_id()))
				.collect(Collectors.toList());
		seatDtos.forEach(seatDto -> System.out.println("SeatDto: " + seatDto));

		return seatDtos;
	}
	
    // 根據 showtimeId 查詢相關信息
    public List<SeatDTO> getSeatsByShowtimeId(Integer showtimeId) {
        // 查詢 showtimeId 對應的 Showtimes 實體
        Showtime showtime = showtimeRepo.findById(showtimeId)
            .orElseThrow(() -> new RuntimeException("Showtime not found"));

        // 從 Showtimes 實體中提取外鍵資訊
        Integer cinemaId = showtime.getCinema().getCinema_id();  // 獲取 cinemaId
        Integer hallId = showtime.getHall().getHall_id();  // 獲取 hallId
        String showDate = showtime.getShowDate().getShow_date();  // 獲取 showDate

        // 根據提取的資訊查詢座位
        List<Seat> seatEntities = seatRepo.findSeatsByCinemaIdAndHallIdAndShowDate(cinemaId, hallId, showDate);
        return seatEntities.stream()
        .map(seatEntity -> new SeatDTO(
            seatEntity.getSeatNumber(), 
            seatEntity.getSeatAvailability(), 
            showtimeId, 
            cinemaId, 
            hallId))
        .collect(Collectors.toList());
    }
}
