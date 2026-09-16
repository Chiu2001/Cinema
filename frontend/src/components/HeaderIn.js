import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import styles from '../styles/header.module.css';
import search_icon from '../assets/search.png';
import movie_icon from '../assets/movie.png';
import axios from 'axios';
import { API_BASE_URL } from '../apiConfig';

const HeaderIn = () => {
    const [searchQuery, setSearchQuery] = useState('');
    const [filteredMovies, setFilteredMovies] = useState([]);
    const [isSearchActive, setIsSearchActive] = useState(false);
    const [roles, setRoles] = useState(JSON.parse(localStorage.getItem('roles') || '[]'));
    const navigate = useNavigate();

    useEffect(() => {
        // Update state whenever roles changes in localStorage
        const storedRoles = JSON.parse(localStorage.getItem('roles') || '[]');
        setRoles(storedRoles);
    }, []);

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

    const handleLogout = () => {
        // Clear localStorage, resetting token and roles to null
        localStorage.clear();
        navigate('/home'); // Redirect to the homepage
    };

    return (
        <div className={styles.headerSTY}>
            <Link to="/HomePageIn">
                <img src={movie_icon} alt="Logo" className={styles.logo} />
            </Link>
            <ul>
                <li><Link to="/MovieListIn">Movie Info</Link></li>
                <li><Link to="/AboutPageIn">About Us</Link></li>
                <li><Link to="/OrderListIn">Order History</Link></li>
                <li><Link to="/CheckOutIn">Cart</Link></li>
                {/* <li><Link to="/UserListIn">Member Center</Link></li> */}
                {/* Show "System Management" based on roles */}
                {roles.includes("ROLE_MANAGER") || roles.includes("ROLE_ADMIN") ? (
                    <li><Link to="/schedule">System Management</Link></li>
                ) : null}
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
                                <Link to={'/MovieDetailIn/' + movie.id}>
                                    <strong>{movie.title}</strong> - {movie.director} - {movie.date}
                                </Link>
                            </li>
                        ))}
                    </ul>
                </div>
            )}

            <div className={styles['user-actions']}>
                {/* Show logout button when logged in */}
                <button onClick={handleLogout} className="btn btn-outline-secondary">👤 Logout</button>
            </div>
        </div>
    );
};

export default HeaderIn;
