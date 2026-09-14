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
 * 座位保留與查詢。
 * 從原本的 CinemaController 拆出來，讓職責單一。
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
			// 檢查 SeatDto 是否非空
			if (seatDTO == null) {
				return ResponseEntity.badRequest().body("SeatDto cannot be null");
			}

			// 設置 DTO 的相關資訊
			seatDTO.setShowtimeId(showtimeId);
			seatDTO.setSeatNumber(seatNumber);

			// 調用 SeatService 的 saveSeatInfo 函數來保存座位資訊，其他外鍵由後端查詢
			seatService.addSeat(seatDTO);

			// 返回成功響應
			return ResponseEntity.ok("Seat information saved successfully");

		} catch (Exception e) {
			// 捕獲並處理異常，返回 500 狀態碼和錯誤訊息
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
			// 調用 service 層的方法來獲取座位信息
			List<SeatDTO> seats = seatService.getSeatsByShowtimeCinemaAndHallAndDate(showtimeId, cinemaId, hallId,
					showDate);

			// 返回成功響應和座位信息
			return ResponseEntity.ok(seats);

		} catch (Exception e) {
			// 捕獲並處理異常，返回 500 狀態碼和錯誤訊息
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Collections.emptyList()); // 返回空的列表，表示沒有數據
		}
	}

	@GetMapping("/seatsResearch/{showtimeId}")
	public ResponseEntity<List<SeatDTO>> getSeatsByShowtimeId(@PathVariable Integer showtimeId) {
		try {
			// 根據 showtimeId 查詢相關的 cinemaId, hallId 和 showDate
			List<SeatDTO> seats = seatService.getSeatsByShowtimeId(showtimeId);

			// 返回座位信息
			return ResponseEntity.ok(seats);

		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Collections.emptyList()); // 返回空列表表示沒有數據
		}
	}
}