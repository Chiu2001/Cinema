package com.example.backend.Entity;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "ticket")
public class Ticket {
	@Id
    @Column(name="ticket_id", length = 10)
    @GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@Column(name = "order_id")
	private int order;

	@Column(name = "showtime_id")
	private int showtime;

	@Column(name = "seat_id")
	private String seat;

	@Column(name = "price")
	private int price;

	@Column(name = "purchase_time")
	private LocalDateTime purchasetime;

	// @JsonProperty renames these to the snake_case keys OrderManagement.js
	// expects (order_id, ticket_id, seat_id, showtime_id, purchase_time),
	// since Jackson would otherwise serialize using the plain getter names.
	@JsonProperty("ticket_id")
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@JsonProperty("order_id")
	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	@JsonProperty("showtime_id")
	public int getShowtime() {
		return showtime;
	}

	public void setShowtime(int showtime) {
		this.showtime = showtime;
	}

	@JsonProperty("seat_id")
	public String getSeat() {
		return seat;
	}

	public void setSeat(String seat) {
		this.seat = seat;
	}

	public int getPrice() {
		return price;
	}

	public void setPrice(int price) {
		this.price = price;
	}

	@JsonProperty("purchase_time")
	public LocalDateTime getPurchasetime() {
		return purchasetime;
	}

	public void setPurchasetime(LocalDateTime purchasetime) {
		this.purchasetime = purchasetime;
	}

	

}
