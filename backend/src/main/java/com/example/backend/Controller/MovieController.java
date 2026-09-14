package com.example.backend.Controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.Entity.Movie;
import com.example.backend.Entity.News;
import com.example.backend.Repo.MovieRepo;
import com.example.backend.Repo.NewsRepo;
import com.example.backend.Service.MovieService;

/**
 * 電影、新聞相關的查詢與搜尋。
 * 從原本的 CinemaController 拆出來，讓職責單一。
 */
@RestController
@RequestMapping("/api/movie")
public class MovieController {

	@Autowired
	private MovieRepo movieRepo;

	@Autowired
	private NewsRepo newsRepo;

	@Autowired
	private MovieService movieService;

	/**
	 * 获取所有电影
	 */
	@GetMapping("/movies")
	public List<Movie> getMovies() {
		return movieRepo.findMoviesByStatus(Movie.Status.TRUE);
	}

	@GetMapping("/movies/{id}")
	public ResponseEntity<Movie> getMovieById(@PathVariable int id) {
		return movieRepo.findById(id)
				.map(movie -> ResponseEntity.ok().body(movie))
				.orElseGet(() -> ResponseEntity.notFound().build());
	}

	@GetMapping("/news")
	public List<News> getNews() {
		return newsRepo.findAll();
	}

	/**
	 * 根据标题或导演搜索电影
	 */
	@GetMapping("/search")
	public List<Movie> searchByTitleOrDirector(@RequestParam String keyword) {
		return movieService.searchByTitleOrDirector(keyword);
	}
}
