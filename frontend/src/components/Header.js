import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import styles from '../styles/header.module.css';
import search_icon from '../assets/search.png';
import movie_icon from '../assets/movie.png';
import axios from 'axios';
import { API_BASE_URL } from '../apiConfig';

const Header = () => {
    const [searchQuery, setSearchQuery] = useState('');
    const [filteredMovies, setFilteredMovies] = useState([]);
    const [isSearchActive, setIsSearchActive] = useState(false);

    const handleInputChange = async (query) => {
        setSearchQuery(query);

        if (query) {
            try {
                const response = await axios.get(`${API_BASE_URL}/api/movie/search?keyword=${query}`);
                setFilteredMovies(response.data);
            } catch (error) {
                console.error('Search failed:', error);
            }
        } else {
            setFilteredMovies([]);
        }
    };

    const handleSearch = () => {
        if (searchQuery) {
            setIsSearchActive(true);
        }
    };

    const handleKeyDown = (e) => {
        if (e.key === 'Enter') {
            handleSearch();
        }
    };

    return (
        <div className={styles.headerSTY}>
            <Link to="/home">
                <img src={movie_icon} alt="Logo" className={styles.logo} />
            </Link>
            <ul>
                <li><Link to="/MovieList">Movie Info</Link></li>
                <li><Link to="/about">About Us</Link></li>
                <li><Link to="/OrderList">Order History</Link></li>
                {/* <li><Link to="/CheckOut">Cart</Link></li> */}
            </ul>

            <div className={styles['search-box']}>
                <input
                    type="text"
                    placeholder="Search"
                    value={searchQuery}
                    onChange={(e) => handleInputChange(e.target.value)}
                    onKeyDown={handleKeyDown}
                />
                <img
                    src={search_icon}
                    alt="Search Icon"
                    onClick={handleSearch}
                />
            </div>

            {isSearchActive && (
                <div className={styles['search-results']}>
                    <h3>Search Results:</h3>
                    <ul>
                        {filteredMovies.map((movie, index) => (
                            <li key={index}>
                                <Link to={'/MovieDetail/' + movie.id}>
                                    <strong>{movie.title}</strong> - {movie.director} - {movie.date}
                                </Link>
                            </li>
                        ))}
                    </ul>
                </div>
            )}

            <div className={styles['user-actions']}>
                {/* Show login button when not logged in */}
                <Link to="/login" className="btn btn-outline-secondary">👤 Member Login</Link>
            </div>
        </div>
    );
};

export default Header;

