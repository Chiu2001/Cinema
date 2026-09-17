import React, { useState, useEffect } from 'react';
import { Link } from "react-router-dom";
import styles from '../styles/MovieList.module.css'; // Import module styles
import { API_BASE_URL } from '../apiConfig';

export default function MovieListIn() {
    let [movieList, setMovieList] = useState([]);

    useEffect(() => {
        fetch(`${API_BASE_URL}/api/movie/movies`)
            .then(response => response.json())
            .then(data => setMovieList(data))
            .catch(error => console.error('Error fetching data:', error));
    }, []);

    return (
        <div className={styles.pageContainer}>
            <h1 className={styles.pageTitle}>Now Showing</h1>
            <div className={styles.movieContainer}>
                {movieList.map(movie => (
                    <div className={styles.movieBorder} key={movie.id}>
                        <Link to={'/MovieDetailIn/' + movie.id}>
                            <img className={styles.imgMovieList} src={movie.img} alt="moviePicture" /><br />
                        </Link>
                        <div className={styles.movieInfo}>
                            <p>{movie.title}</p>
                            <p>Release Date: {movie.date}</p>
                        </div>
                        <Link to={'/MovieDetailIn/' + movie.id}>
                            <button className={styles.btn}>Details</button>
                        </Link>
                    </div>
                ))}
            </div>
        </div>
    );
}
