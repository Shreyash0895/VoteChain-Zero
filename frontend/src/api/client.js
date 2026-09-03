import axios from 'axios'

// Points at the Spring Boot backend from earlier phases.
// Change this if your backend runs on a different port/host.
const BASE_URL = 'http://localhost:8080'

const client = axios.create({
  baseURL: BASE_URL,
  headers: { 'Content-Type': 'application/json' },
})

// Attach the JWT (if we have one) to every outgoing request automatically —
// mirrors exactly how JwtAuthFilter expects "Authorization: Bearer <token>"
// on the backend.
client.interceptors.request.use((config) => {
  const token = localStorage.getItem('votechain_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// Extracts a readable message from the backend's error responses.
export function extractErrorMessage(error) {
  return (
    error?.response?.data?.message ||
    error?.response?.data?.error ||
    error?.message ||
    'Something went wrong. Please try again.'
  )
}

export default client