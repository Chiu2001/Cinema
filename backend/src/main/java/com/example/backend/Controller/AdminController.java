package com.example.backend.Controller;

import java.sql.Time;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.backend.DTO.MovieDTO;
import com.example.backend.DTO.NewsDTO;
import com.example.backend.DTO.ShowtimeDTO;
import com.example.backend.Entity.Cinema;
import com.example.backend.Entity.Hall;
import com.example.backend.Entity.Movie;
import com.example.backend.Entity.News;
import com.example.backend.Entity.Showdate;
import com.example.backend.Entity.Showtime;
import com.example.backend.Entity.Ticket;
import com.example.backend.Entity.User;
import com.example.backend.Repo.CinemaRepo;
import com.example.backend.Repo.HallRepo;
import com.example.backend.Repo.MovieRepo;
import com.example.backend.Repo.ShowdateRepo;
import com.example.backend.Repo.ShowtimeRepo;
import com.example.backend.Repo.TicketRepo;
import com.example.backend.Repo.UserRepo;
import com.example.backend.Service.MovieService;
import com.example.backend.Service.NewsService;
import com.example.backend.Service.UserService;

import java.io.IOException;

@RestController
@RequestMapping("api/admin")
public class AdminController {

    @Autowired
    private TicketRepo ticketRepo;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private MovieRepo movieRepo;

    @Autowired
    private NewsService newsService;

    @Autowired
    private UserService userService;

    @Autowired
    private  MovieService movieService;

    @Autowired
    private CinemaRepo cinemaRepo;

    @Autowired
    private HallRepo hallRepo;

    @Autowired
    private ShowdateRepo showdateRepo;

    @Autowired
    private ShowtimeRepo showtimeRepo;

    /**
     * Get all tickets
     * @return the list of tickets
     */
    @GetMapping("/tickets")
    public ResponseEntity<List<Ticket>> getTickets() {
        List<Ticket> tickets = ticketRepo.findAll();
        return ResponseEntity.ok(tickets);
    }

    /**
     * Get all users
     * @return the list of users
     */
    @GetMapping("/getusers")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }
    
    @PostMapping("/add-movie")
    public ResponseEntity<String> addMovie (
    		@ModelAttribute MovieDTO movieDTO,
            @RequestParam("file") MultipartFile file) throws java.io.IOException {
        try {
            String response = movieService.addMovie(movieDTO, file);
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Error occurred while saving the movie.");
        }
    }
    
    @PutMapping("/movie/{id}/update")
    public ResponseEntity<Movie> updateMovie(
        @PathVariable Integer id,
        @ModelAttribute MovieDTO movieDTO, 
        @RequestParam(required = false) MultipartFile file) throws IOException {
        Movie updatedMovie = movieService.updateMovie(id, movieDTO, file);
        
        return ResponseEntity.ok(updatedMovie);
    }
    
    @GetMapping("/search-user")
    public Optional<User> findByUsername(String username) {
		return userRepo.findByUsername(username);
	}
    
    @PostMapping("/add-news")
    public ResponseEntity<String> addNews (
    		@ModelAttribute NewsDTO newsDTO,
            @RequestParam("file") MultipartFile file) throws IOException {
        try {
            String response = newsService.addNews(newsDTO, file);
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Error occurred while saving the news.");
        }
    }
	
    @PutMapping("/news/{id}/update")
	public ResponseEntity<News> updateNews(@PathVariable Integer id,
	        @ModelAttribute NewsDTO newsDTO, 
	        @RequestParam(required = false) MultipartFile file) throws IOException{
    	News updatedNews = newsService.updateNews(id, newsDTO, file);
        
        return ResponseEntity.ok(updatedNews);
	}
    @GetMapping("/movies")
    public List<Movie> getAllMovies() {
        return movieRepo.findAll();  // Query and return all movies
    }

    @GetMapping("/showtimes")
    public List<Showtime> getAllShowtimes() {
        return showtimeRepo.findAll();
    }

    @PostMapping("/add-showtime")
    public ResponseEntity<Showtime> addShowtime(@RequestBody ShowtimeDTO showtimeDTO) {
        Movie movie = movieRepo.findById(showtimeDTO.getMovieId())
                .orElseThrow(() -> new IllegalArgumentException("Movie not found"));
        Cinema cinema = cinemaRepo.findById(showtimeDTO.getCinemaId())
                .orElseThrow(() -> new IllegalArgumentException("Cinema not found"));
        Hall hall = hallRepo.findById(showtimeDTO.getHallId())
                .orElseThrow(() -> new IllegalArgumentException("Hall not found"));

        // Reuse the Showdate row for this date if one already exists, instead of
        // creating a duplicate row for the same day.
        Showdate showDate = showdateRepo.findByShow_date(showtimeDTO.getShowDate())
                .orElseGet(() -> {
                    Showdate newShowDate = new Showdate();
                    newShowDate.setShow_date(showtimeDTO.getShowDate());
                    return showdateRepo.save(newShowDate);
                });

        Showtime showtime = new Showtime();
        showtime.setMovie(movie);
        showtime.setCinema(cinema);
        showtime.setHall(hall);
        showtime.setShowDate(showDate);
        showtime.setShow_time(Time.valueOf(showtimeDTO.getShowTime()));

        return ResponseEntity.ok(showtimeRepo.save(showtime));
    }

}

