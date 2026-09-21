# Cinema

[![Java](https://img.shields.io/badge/language-Java-brightgreen)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/backend-Spring%20Boot%203-6DB33F)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/frontend-React-61DAFB)](https://react.dev/)
[![Stripe](https://img.shields.io/badge/payments-Stripe-635BFF)](https://stripe.com/)

A full-stack movie ticket booking site: browse showtimes, pick seats, pay with Stripe, and manage everything (movies, showtimes, users, orders) from an admin panel.

## Features

**Customer**
- Browse movies and showtimes, pick seats on a live seat map
- Cart with per-showtime seat holds (see [Seat reservations](#seat-reservations) below)
- Checkout and payment via Stripe, with a guest-friendly cart preview before requiring login
- Email/password signup and login, plus Google OAuth login
- Order history

**Admin** (role-gated: `ADMIN` / `MANAGER`)
- Movie management (add/edit, poster upload, showing status)
- Showtime scheduling (cinema, hall, date, time)
- News management
- User management, including role changes (`MANAGER` only)
- Order lookup (every ticket sold, by order/seat/showtime)

**Security**
- JWT authentication, Spring Security route-level authorization
- BCrypt-hashed passwords, enforced strength rule (8+ characters, letters and numbers) on both signup and profile updates
- Ownership checks on profile and order endpoints — a user can only read/modify their own data unless they're ADMIN/MANAGER
- Password hashes are never serialized back out over the API

## Seat reservations

Selecting a seat marks it unavailable immediately, so two people can't book the same seat. If a cart is abandoned (closed tab, no payment), a scheduled job releases any hold older than the configured TTL (default 10 minutes) back to available — see `seat.reservation.ttl-minutes` below. A seat's hold is only cleared permanently once payment is actually confirmed.

## Tech Stack

| | |
|---|---|
| Backend | Java 17, Spring Boot 3, Spring Security (JWT), Spring Data JPA, MariaDB |
| Frontend | React (Create React App), React Router |
| Payments | Stripe Checkout + webhooks |
| Auth | JWT, Google OAuth 2.0 |
| Testing | JUnit 5, Mockito, `@DataJpaTest` (H2) |

## Getting Started

### Prerequisites
- Java 17
- Node.js and npm
- MariaDB (or MySQL) running locally
- A [Stripe](https://dashboard.stripe.com/) account (test mode is fine) if you want to exercise checkout
- The [Stripe CLI](https://stripe.com/docs/stripe-cli) for forwarding webhooks to `localhost` in local development

### Backend

```bash
git clone https://github.com/Chiu2001/Cinema
cd Cinema/backend
```

Create the database, then copy the config template and fill in your own values:

```bash
mysql -u root -p -e "CREATE DATABASE cinema"
cp src/main/resources/application.properties.example src/main/resources/application.properties
```

Edit `application.properties` — at minimum set your database credentials and a JWT secret (`openssl rand -base64 64`). Stripe and Google OAuth keys are only needed if you're testing those flows.

Optionally load some demo movies/cinemas/showtimes so the app isn't empty on first run:

```bash
mysql -u root -p cinema < ../cinema-seed-data.sql
```

Run it (the Maven wrapper is included, no need to install Maven separately):

```bash
./mvnw spring-boot:run
```

The API comes up at `http://localhost:8443/movie`.

**First admin account**: sign up normally through the app, then promote yourself directly in the database (there's no bootstrap admin — the role-change endpoint itself requires an existing MANAGER):

```sql
UPDATE users SET role = 'ADMIN' WHERE email = 'you@example.com';
```

### Frontend

```bash
cd ../frontend
npm install
npm start
```

Opens at `http://localhost:3000`. It talks to the backend at `http://localhost:8443/movie` by default; override with the `REACT_APP_API_BASE_URL` environment variable if needed.

### Testing Stripe locally

Stripe can't reach your `localhost` directly, so forward webhook events with the Stripe CLI:

```bash
stripe listen --forward-to localhost:8443/movie/api/stripe/webhook
```

Copy the `whsec_...` secret it prints into `stripe.webhook-secret` in `application.properties`. Use a [Stripe test card](https://stripe.com/docs/testing) (e.g. `4242 4242 4242 4242`) to complete a payment.

## Running Tests

```bash
cd backend
./mvnw test
```

Covers password validation, order-ownership authorization, ticket creation on payment, seat-reservation TTL behavior, and a regression test for a Spring Data query bug — a mix of unit tests (mocked repositories) and `@DataJpaTest` integration tests (in-memory H2, no real database needed).

## Contact

If you have any questions or suggestions, feel free to reach out:
- Name: Chiu Cheng-Yi, Tsai Meng-En
- GitHub Issues: [Submit here](https://github.com/Chiu2001/Cinema/issues)
