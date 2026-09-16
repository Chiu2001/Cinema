// Centralized management of the backend API URL.
// When no environment variable is set for local development, it defaults to http://localhost:8443/movie.
// When deploying to production, just set the REACT_APP_API_BASE_URL environment variable —
// there's no need to change any component code.
export const API_BASE_URL =
    process.env.REACT_APP_API_BASE_URL || 'http://localhost:8443/movie';