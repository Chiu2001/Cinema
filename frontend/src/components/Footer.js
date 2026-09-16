import React from "react";
import { Link } from 'react-router-dom';  // Use React Router's Link component
import styles from '../styles/footer.module.css';
import fb from '../assets/fbimg.png';
import twitter from '../assets/twitterimg.png';
import linkedin from '../assets/linkedinimg.png';
import insta from '../assets/instaimg.png';

const Footer = () => {
    return (
        <div className={styles.footer}>
            <div className={styles.sbFooter + " " + styles.sectionPadding}>
                <div className={styles.sbFooterLinks}>
                    <div className={styles.sbFooterLinksDiv}>
                        <h3>About Us</h3>
                        <p>Business name: Washington Cinemas Co., Ltd., Taipei Branch</p>
                        <p>Business registration number: 80412345</p>
                        <p>Address: No. 231, Sec. 2, Jianguo S. Rd., Da'an Dist., Taipei City</p>
                        <p>Phone: 02-2700-5858</p>
                    </div>

                    <div className={styles.sbFooterLinksDiv}>
                        <h3>Related Information</h3>
                        <Link to="/about" className={styles.link}>
                            <p>About the Cinema</p>
                        </Link>
                        <Link to="/press" className={styles.link}>
                            <p>Customer Service Email</p>
                        </Link>
                        <Link to="/career" className={styles.link}>
                            <p>Membership Rules</p>
                        </Link>
                        <Link to="/contact" className={styles.link}>
                            <p>FAQ</p>
                        </Link>
                    </div>

                    <div className={styles.sbFooterLinksDiv}>
                        <h4>Our Community</h4>
                        <div className={styles.socialMedia}>
                            <a href="https://www.facebook.com" target="_blank" rel="noopener noreferrer">
                                <img src={fb} alt="Facebook" />
                            </a>
                            <a href="https://www.twitter.com" target="_blank" rel="noopener noreferrer">
                                <img src={twitter} alt="Twitter" />
                            </a>
                            <a href="https://www.linkedin.com" target="_blank" rel="noopener noreferrer">
                                <img src={linkedin} alt="LinkedIn" />
                            </a>
                            <a href="https://www.instagram.com" target="_blank" rel="noopener noreferrer">
                                <img src={insta} alt="Instagram" />
                            </a>
                        </div>
                    </div>
                </div>

                <hr />
            </div>
        </div>
    );
}

export default Footer;

