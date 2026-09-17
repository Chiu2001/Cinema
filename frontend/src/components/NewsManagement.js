import React, { useState, useEffect } from 'react';
import axios from 'axios';
import styles from '../styles/NewsManagement.module.css';
import { Link, useNavigate } from 'react-router-dom';
import { API_BASE_URL } from '../apiConfig';

const NewsManagement = () => {
    const [news, setNews] = useState([]);
    const [showModal, setShowModal] = useState(false);
    const [selectedNews, setSelectedNews] = useState(null);
    const [newNews, setNewNews] = useState({
        img: '',
        text: '',
        created_time: '',
    });
    const [file, setFile] = useState(null);
    const navigate = useNavigate();

    // Fetch news data from the backend
    useEffect(() => {
        const token = localStorage.getItem('token');
        if (!token) {
            alert('Please log in first');
            navigate('/login');
            return;
        }

        axios.get(`${API_BASE_URL}/api/movie/news`, {
            headers: {
                'Authorization': `Bearer ${token}`,
            }
        })
        .then(response => {
            setNews(response.data);
        })
        .catch(error => {
            handleErrorResponse(error);
        });
    }, []);

    // Handle API errors
    const handleErrorResponse = (error) => {
        console.error('Error:', error);
        if (error.response && error.response.status === 401) {
            alert('Token has expired or is invalid, please log in again');
            localStorage.removeItem('token');
            navigate('/login');
        }
    };

    // Show the modal and populate it with the selected news item's info
    const handleCardClick = (newsItem) => {
        setSelectedNews(newsItem);
        setNewNews({
            img: newsItem.img,
            text: newsItem.text,
            created_time: newsItem.created_time,
        });
        setShowModal(true);
    };

    // Handle input field changes
    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setNewNews({ ...newNews, [name]: value });
    };

    // Handle image file uploads
    const handleImageUpload = (e) => {
        const selectedFile = e.target.files[0];
        setFile(selectedFile);
    };

    // Save the updated news info
    const saveUpdatedNews = () => {
        if (!newNews.text) {
            alert('Please make sure all fields are filled in');
            return;
        }

        const formData = new FormData();
        if (file) {
            formData.append('file', file);
        }
        formData.append('text', newNews.text);
        formData.append('created_time', newNews.created_time);
        formData.append('img', newNews.img);

        const token = localStorage.getItem('token');
        if (!token) {
            alert('Please log in first');
            navigate('/login');
            return;
        }

        const url = selectedNews
            ? `${API_BASE_URL}/api/admin/news/${selectedNews.id}/update` 
            : `${API_BASE_URL}/api/admin/add-news`;
        const method = selectedNews ? 'put' : 'post';

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
            if (selectedNews) {
                setNews(news.map(newsItem =>
                    newsItem.id === selectedNews.id ? response.data : newsItem
                ));
            } else {
                setNews([...news, response.data]);
            }
            setShowModal(false);
        })
        .catch(error => {
            handleErrorResponse(error);
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
                    <li><Link to="/order">Order Lookup</Link></li>
                </ul>
            </div>

            {/* Main content area */}
            <div className={styles.container}>
                <h1>News Management</h1>
                <div className={styles.cardGrid}>
                    {/* Display news cards */}
                    {news.map(newsItem => (
                        <div key={newsItem.id} className={styles.card} onClick={() => handleCardClick(newsItem)}>
                            <img src={newsItem.img} alt={newsItem.text} />
                            <p>{newsItem.text}</p>
                        </div>
                    ))}
                </div>

                {/* Add news button */}
                <button onClick={() => setSelectedNews(null) || setShowModal(true)} className={styles.addButton}>Add</button>

                {/* Modal display area */}
                {showModal && (
                    <div className={styles.modalOverlay}>
                        <div className={styles.modal}>
                            <h2>{selectedNews ? 'Edit News' : 'Add News'}</h2>
                            <input
                                type="file"
                                onChange={handleImageUpload}
                            />
                            <textarea
                                name="text"
                                value={newNews.text}
                                onChange={handleInputChange}
                                placeholder="News content"
                            ></textarea>
                            <div className={styles.modalButtons}>
                                <button onClick={saveUpdatedNews} className={styles.confirmButton}>Save</button>
                                <button onClick={() => setShowModal(false)} className={styles.cancelButton}>Cancel</button>
                            </div>
                        </div>
                    </div>
                )}
            </div>
        </div>
    );
};

export default NewsManagement;
