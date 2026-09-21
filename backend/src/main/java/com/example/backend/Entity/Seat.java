package com.example.backend.Entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "Seat_Info_Test")
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "showtime_id", nullable = true, referencedColumnName = "showtime_id")
    private Showtime showtimeId;

    @ManyToOne
    @JoinColumn(name = "cinema_id", nullable = true, referencedColumnName = "cinema_id")
    private Cinema cinemaId;

    @ManyToOne
    @JoinColumn(name = "hall_id", nullable = true, referencedColumnName = "hall_id")
    private Hall hallId;

    @Column(name = "seat_number", nullable = false)
    private String seatNumber;

    @Column(name = "seat_availability", nullable = false, columnDefinition = "TINYINT(1)")
    private Boolean seatAvailability;

    // Set when a seat is reserved (seatAvailability = false), cleared back to
    // null once the booking is actually paid for. A scheduled task releases
    // any seat whose hold is older than the TTL, so an abandoned cart doesn't
    // lock a seat forever; a null value here (with seatAvailability still
    // false) means the seat is permanently booked and never gets swept.
    @Column(name = "reserved_at")
    private LocalDateTime reservedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Showtime getShowtimeId() {
        return showtimeId;
    }

    public void setShowtimeId(Showtime showtimeId) {
        this.showtimeId = showtimeId;
    }

    public Cinema getCinemaId() {
        return cinemaId;
    }

    public void setCinemaId(Cinema cinemaId) {
        this.cinemaId = cinemaId;
    }

    public Hall getHallId() {
        return hallId;
    }

    public void setHallId(Hall hallId) {
        this.hallId = hallId;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(String seatNumber) {
        this.seatNumber = seatNumber;
    }

    public Boolean getSeatAvailability() {
        return seatAvailability;
    }

    public void setSeatAvailability(Boolean seatAvailability) {
        this.seatAvailability = seatAvailability;
    }

    public LocalDateTime getReservedAt() {
        return reservedAt;
    }

    public void setReservedAt(LocalDateTime reservedAt) {
        this.reservedAt = reservedAt;
    }
}
