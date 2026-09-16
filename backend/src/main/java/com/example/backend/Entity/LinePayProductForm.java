package com.example.backend.Entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "LinePayProductForm")
public class LinePayProductForm {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	
	@Column(name = "name", nullable = false) //ok
	private String name;

	@Column(name = "imageUrl", nullable = true)
	private String imageUrl;

	@Column(name = "quantity", nullable = false, precision = 38, scale = 0) //ok
	private BigDecimal quantity; 

	@Column(name = "price", nullable = false, precision = 38, scale = 0) //ok
	private BigDecimal price;

	// @ManyToOne
    // @JoinColumn(name = "seat_id", referencedColumnName = "seat_number")
    // private SeatEntity seat;

	// @ManyToOne
    // @JoinColumn(name = "movie_id", referencedColumnName = "movie_id", nullable = false)
    // private Movie movie;

	// @ManyToOne
    // @JoinColumn(name = "hall_id", referencedColumnName = "hall_id", nullable = false)
    // private Halls hall;

	// // Convert Movie's quantity to BigDecimal and set it on LinePayProductForm's quantity
    // public void setQuantityFromMovie() {
    //     if (movie != null) {
    //         this.price = new BigDecimal(movie.getQuantity());
    //     }
    // }

	// // Convert Hall's price to BigDecimal and set it on LinePayProductForm's price
    // public void setPriceFromHall() {
    //     if (hall != null) {
    //         this.price = new BigDecimal(hall.getPrice());
    //     }
    // }
}
