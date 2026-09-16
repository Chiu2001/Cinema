import React, { useState, useEffect } from 'react';
import { Link, useParams } from "react-router-dom";
import ShowMovieAndTime from './showMovieAndTime';
import movieDetailStyles from '../styles/MovieDetail.module.css'; // Import the MovieDetail module styles
import { API_BASE_URL } from '../apiConfig';

export default function MovieDetailIn() {

    let params = useParams();
    let [movieDetail, setMovieDetail] = useState(null);

    useEffect(() => {
        console.log("Fetching movie data with id:", params.id);

        // Correct API path, passing params.id dynamically
        fetch(`${API_BASE_URL}/api/movie/movies/${params.id}`)
            .then(response => {
                // Check the API response status code
                if (!response.ok) {
                    throw new Error(`HTTP error! status: ${response.status}`);
                }
                return response.json();
            })
            .then(data => {
                // If the response is an object rather than an array, set the data directly
                setMovieDetail(data);
                console.log("movieInfo", data);
            })
            .catch(error => console.error('Error fetching movie data:', error));
    }, [params.id]);

    return (
        <div className={movieDetailStyles.movieDetailWrapper}>
            {
                movieDetail &&
                <div className={movieDetailStyles.movieDetailContainer}>
                    <h1><p className={movieDetailStyles.title}>Movie Info</p></h1>
                    <div className={movieDetailStyles.movieDetailContent}>
                        <img className={movieDetailStyles.imgMovieDetail} src={movieDetail.img} alt={movieDetail.title} width={200} />
                        <div className={movieDetailStyles.movieInfo}>
                            <div className={movieDetailStyles.movieDetails}>
                                <p className={movieDetailStyles.movieDetailItem}>Duration: {movieDetail.duration} minutes</p>
                                <p className={movieDetailStyles.movieDetailItem}>Release Date: {movieDetail.date}</p>
                                <p className={movieDetailStyles.movieDetailItem}>Genre: {movieDetail.genre}</p>
                                <p className={movieDetailStyles.movieDetailItem}>Cast: {movieDetail.actor}</p>
                                <p className={movieDetailStyles.movieDetailItem}>Director: {movieDetail.director}</p>
                            </div>
                        </div>
                    </div>
                    {/* Pass movieId to the ShowMovieAndTime component */}
                    <ShowMovieAndTime movieId={movieDetail.id} />
                    <Link to="/MovieListIn" className={movieDetailStyles.backLink}>Back to Movie List</Link>
                </div>
            }
        </div>
    );
}



