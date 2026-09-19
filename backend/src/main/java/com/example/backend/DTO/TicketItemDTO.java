package com.example.backend.DTO;

import java.util.List;

// One line item from checkout: a showtime plus the seats bought for it at
// that showtime's price. Used to (de)serialize the "ticketItems" metadata
// Stripe echoes back on the checkout.session.completed webhook, so tickets
// can be created once payment is actually confirmed.
public class TicketItemDTO {

	private Integer showtimeId;
	private List<String> seatNumbers;
	private Integer unitAmount; // cents, matching Stripe's line item amount

	public Integer getShowtimeId() {
		return showtimeId;
	}

	public void setShowtimeId(Integer showtimeId) {
		this.showtimeId = showtimeId;
	}

	public List<String> getSeatNumbers() {
		return seatNumbers;
	}

	public void setSeatNumbers(List<String> seatNumbers) {
		this.seatNumbers = seatNumbers;
	}

	public Integer getUnitAmount() {
		return unitAmount;
	}

	public void setUnitAmount(Integer unitAmount) {
		this.unitAmount = unitAmount;
	}
}
