import React, { useState, useEffect } from 'react';
import { Link } from "react-router-dom";
import styles from '../styles/MovieList.module.css'; // Import module styles

export default function MovieList() {
    let [movieList, setMovieList] = useState([]);

    useEffect(() => {
        fetch("http://localhost:8443/movie/api/movie/movies")
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
                        <Link to={'/MovieDetail/' + movie.id}>
                            <img className={styles.imgMovieList} src={movie.img} alt="moviePicture" /><br />
                        </Link>
                        <div className={styles.movieInfo}>
                            <p>{movie.title}</p>
                            <p>Release date: {movie.released_date}</p>
                        </div>
                        <Link to={'/MovieDetail/' + movie.id}>
                            <button className={styles.btn}>More Info</button>
                        </Link>
                    </div>
                ))}
            </div>
        </div>
    );
}
