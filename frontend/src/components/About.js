import React from 'react';
import styles from '../styles/AboutPage.module.css';

const AboutPage = () => {
    return (
        <div className={styles.aboutContainer}>
            <h3>About Us</h3>
            <p>Business name: Washington Cinemas Co., Ltd., Taipei Branch</p>
            <p>Business registration number: 80412345</p>
            <p>Address: No. 231, Sec. 2, Jianguo S. Rd., Da'an Dist., Taipei City</p>
            <p>Phone: (02)2720-5678</p>
            <p>Number of halls: 1 hall</p>
            <p>Number of seats: 1360 seats</p>
            <p>
                The cinema's 12 halls have been completely renovated, featuring panoramic screens, 4K high-definition projection equipment, a Dolby Atmos sound system, Bose surround-sound speakers, and halls with a 1:2.35 golden-ratio aspect, delivering an unparalleled audiovisual experience for every audience.
            </p>
            <p>
                The seating uses a European-American style dual-armrest design with business-class-level spacious seats, paired with ergonomic wraparound seat backs, greatly enhancing viewing comfort so every guest can enjoy the finest movie-going experience.
            </p>

            <h3>New York Cinema</h3>
            <div className={styles.theaterInfo}>
                <div className={styles.locationInfo}>
                    <h4>Location</h4>
                    <p>Address: No. 231, Sec. 2, Jianguo S. Rd., Da'an Dist., Taipei City</p>
                    <div className={styles.mapContainer}>
                        <iframe
                            title="Google Maps"
                            src="https://www.google.com/maps/embed?pb=!1m18!1m12!1m3!1d3615.238137532968!2d121.53551237566926!3d25.025991277821575!2m3!1f0!2f0!3f0!3m2!1i1024!2i768!4f13.1!3m3!1m2!1s0x3442aa29c2124e41%3A0x4c5bf7354c52fabf!2zMTA25Y-w5YyX5biC5aSn5a6J5Y2A5bu65ZyL5Y2X6Lev5LqM5q61MjMx6Jmf!5e0!3m2!1sen!2stw!4v1724633072603!5m2!1sen!2stw"
                            width="600"
                            height="450"
                            style={{ border: 0 }}
                            allowFullScreen=""
                            loading="lazy"
                            referrerPolicy="no-referrer-when-downgrade"
                        ></iframe>
                    </div>

                </div>
            </div>
        </div>
    );
};

export default AboutPage;