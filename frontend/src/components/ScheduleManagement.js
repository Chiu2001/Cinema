import React, { useState, useEffect } from 'react';
import axios from 'axios';
import styles from '../styles/ScheduleManagement.module.css';
import { Link } from 'react-router-dom';

const ScheduleManagement = () => {
    const [movies, setMovies] = useState([]);
    const [showModal, setShowModal] = useState(false);
    const [selectedMovie, setSelectedMovie] = useState(null);
    const [newMovie, setNewMovie] = useState({
        img: '',
        title: '',
        genre: '',
        director: '',
        description: '',
        date: '',
        actor: '',
        duration: '',
        status: 'FALSE',
    });
    const [file, setFile] = useState(null);
    const [tempStatus, setTempStatus] = useState('FALSE');

    // Fetch movie data from the backend
    useEffect(() => {
        const token = localStorage.getItem('token');
        if (!token) {
            alert('Please log in first');
            window.location.href = '/login';
            return;
        }

        axios.get('http://localhost:8443/movie/api/admin/movies', {
            headers: {
                'Authorization': `Bearer ${token}`,
            }
        })
            .then(response => {
                setMovies(response.data);
            })
            .catch(error => {
                console.error('Error fetching movies:', error);
                if (error.response && error.response.status === 401) {
                    alert('Token has expired or is invalid, please log in again');
                    localStorage.removeItem('token');
                    window.location.href = '/login';
                }
            });
    }, []);

    // Show the modal and populate it with the selected movie's data
    const handleCardClick = (movie) => {
        setSelectedMovie(movie);
        setNewMovie({
            img: movie.img,
            title: movie.title,
            genre: movie.genre,
            director: movie.director,
            description: movie.description,
            date: movie.date,
            actor: movie.actor,
            duration: movie.duration,
            status: movie.status ? 'TRUE' : 'FALSE',
        });
        setShowModal(true);
    };

    

    // Handle changes to input fields
    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setNewMovie({ ...newMovie, [name]: value });
    };

    // Handle image file upload
    const handleImageUpload = (e) => {
        const selectedFile = e.target.files[0];
        setFile(selectedFile);
        const reader = new FileReader();
        reader.onloadend = () => {
            setNewMovie({ ...newMovie, img: reader.result });
        };
        reader.readAsDataURL(selectedFile);
    };

    // Save the updated movie info
    const saveUpdatedMovie = () => {
        if (!newMovie.title || !newMovie.director || !newMovie.actor || !newMovie.description || !newMovie.genre || !newMovie.duration || !newMovie.date) {
            alert('Please make sure all fields are filled in');
            return;
        }
    
        const formData = new FormData();
        if (file) {
            formData.append('file', file);
        }
        formData.append('title', newMovie.title);
        formData.append('director', newMovie.director);
        formData.append('actor', newMovie.actor);
        formData.append('description', newMovie.description);
        formData.append('genre', newMovie.genre);
        formData.append('duration', newMovie.duration);
        formData.append('date', newMovie.date);
        formData.append('status', newMovie.status);
    
        const token = localStorage.getItem('token');
        if (!token) {
            alert('Please log in first');
            window.location.href = '/login';
            return;
        }

        const url = selectedMovie ? `http://localhost:8443/movie/api/admin/movie/${selectedMovie.id}/update` : 'http://localhost:8443/movie/api/admin/add-movie';
        const method = selectedMovie ? 'put' : 'post';
    
        axios({
            method: method,
            url: url,
            data: formData,
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'multipart/form-data',
            }
        })
        .then(response => {
            if (selectedMovie) {
                setMovies(movies.map(movie =>
                    movie.id === selectedMovie.id ? { ...response.data } : movie
                ));
            } else {
                setMovies([...movies, response.data]);
            }
            setShowModal(false);
        })
        .catch(error => {
            console.error('Error updating or adding movie:', error);
            if (error.response && error.response.status === 401) {
                alert('Token has expired or is invalid, please log in again');
                localStorage.removeItem('token');
                window.location.href = '/login';
            }
        });
    };

    return (
        <div>
            {/* Top navigation bar */}
            <div className={styles.headerST}>
                <ul>
                    <li><Link to="/schedule">Schedule Management</Link></li>
                    <li><Link to="/news">News Management</Link></li>
                    <li><Link to="/user_management">User Management</Link></li>
                    <li><Link to="/order">Search Orders</Link></li>
                </ul>
            </div>

            {/* Main content area */}
            <div className={styles.container}>
                <h1>Schedule Management</h1>
                <div className={styles.cardGrid}>
                    {/* Display movie cards */}
                    {movies.map(movie => (
                        <div key={movie.id} className={styles.card} onClick={() => handleCardClick(movie)}>
                            <img src={movie.img} alt={movie.title} />
                            <h3>{movie.title}</h3>
                            <p>Release Date: {movie.date}</p>
                            <p>{movie.status ? 'Now Showing' : 'Not Showing'}</p>
                        </div>
                    ))}
                </div>

                {/* Add movie button */}
                <button onClick={() => setSelectedMovie(null) || setShowModal(true)} className={styles.addButton}>Add</button>

                {/* Modal display area */}
                {showModal && (
                    <div className={styles.modalOverlay}>
                        <div className={styles.modal}>
                            <h2>{selectedMovie ? 'Movie Details' : 'Add Movie'}</h2>
                            <input type="text" name="title" placeholder="Title" value={newMovie.title} onChange={handleInputChange} />
                            <input type="text" name="director" placeholder="Director" value={newMovie.director} onChange={handleInputChange} />
                            <input type="text" name="actor" placeholder="Actor" value={newMovie.actor} onChange={handleInputChange} />
                            <textarea name="description" placeholder="Movie description" value={newMovie.description} onChange={handleInputChange} />
                            <input type="text" name="genre" placeholder="Genre" value={newMovie.genre} onChange={handleInputChange} />
                            <input type="text" name="duration" placeholder="Duration" value={newMovie.duration} onChange={handleInputChange} />
                            <input type="date" name="date" value={newMovie.date} onChange={handleInputChange} />
                            <input type="file" onChange={handleImageUpload} />
                            <select name="status" value={newMovie.status} onChange={handleInputChange}>
                                <option value="TRUE">Now Showing</option>
                                <option value="FALSE">Not Showing</option>
                            </select>
                            <button onClick={saveUpdatedMovie}>Save</button>
                            <button onClick={() => setShowModal(false)}>Close</button>
                        </div>
                    </div>
                )}
            </div>
        </div>
    );
};

export default ScheduleManagement;

