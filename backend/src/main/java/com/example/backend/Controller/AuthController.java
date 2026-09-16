package com.example.backend.Controller;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.DTO.LoginDTO;
import com.example.backend.DTO.TokenDTO;
import com.example.backend.DTO.UserDTO;
import com.example.backend.Entity.Role;
import com.example.backend.Entity.User;
import com.example.backend.Repo.UserRepo;
import com.example.backend.Security.CustomUserDetails;
import com.example.backend.Security.CustomUserDetailsService;
import com.example.backend.Security.JwtService;
import com.example.backend.Service.UserService;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.json.JsonFactory;

/**
 * Registration, login (including Google login), and user profile updates.
 * Split out of the original CinemaController to keep responsibilities focused.
 */
@RestController
@RequestMapping("/api/movie")
public class AuthController {

	@Autowired
	private UserRepo userRepo;

	@Autowired
	private UserService userService;

	@Autowired
	private CustomUserDetailsService customUserDetailsService;

	@Autowired
	private JwtService jwtService;

	// This used to be hardcoded as Collections.singletonList(""):
	// GoogleIdTokenVerifier compares this list against the aud claim in the token Google issues,
	// but aud can never be an empty string, so Google login verification always failed (idToken was always null).
	// Now it reads the real Google OAuth Client ID from configuration so Google login can actually be verified.
	@Value("${google.oauth.client-id}")
	private String googleClientId;

	/**
	 * Register a new user
	 */
	@PostMapping("/register")
	public ResponseEntity<Map<String, String>> saveUser(@RequestBody UserDTO user) {
		Map<String, String> response = new HashMap<>();

		try {
			int savedUser = userService.saveOrUpdateUser(user);
			response.put("message", "User saved with ID: " + savedUser);
			return ResponseEntity.status(201).body(response);
		} catch (Exception e) {
			response.put("error", e.getMessage());
			return ResponseEntity.status(400).body(response); // Return 400 to indicate a bad request
		}
	}

	/**
	 * User login
	 */
	@PostMapping("/login")
	public ResponseEntity<TokenDTO> login(@RequestBody LoginDTO loginDTO) {
		return ResponseEntity.ok(customUserDetailsService.authenticate(loginDTO));
	}

	@PostMapping("/google-login")
	public ResponseEntity<?> googleLogin(@RequestBody Map<String, String> request) {
		String googleToken = request.get("token");

		JsonFactory jsonFactory = GsonFactory.getDefaultInstance();
		GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), jsonFactory)
				.setAudience(Collections.singletonList(googleClientId))
				.build();

		try {
			GoogleIdToken idToken = verifier.verify(googleToken);
			if (idToken != null) {
				GoogleIdToken.Payload payload = idToken.getPayload();
				String email = payload.getEmail();
				String name = (String) payload.get("name");

				Optional<User> optionalUser = userRepo.findByEmail(email);
				User user;
				if (optionalUser.isPresent()) {
					user = optionalUser.get();
				} else {
					// Create a new user if not found
					user = new User();
					user.setEmail(email);
					user.setUsername(name);
					user.setRole(Role.USER);
					userRepo.save(user);
				}

				// Assuming userRepo returns a UserDetails object
				UserDetails userDetails = new CustomUserDetails(user);
				String jwtToken = jwtService.generateToken(userDetails);

				return ResponseEntity.ok(Map.of(
						"token", jwtToken,
						"email", email,
						"roles", userDetails.getAuthorities().stream()
								.map(GrantedAuthority::getAuthority)
								.collect(Collectors.toList()),
						"name", name,
						"id", user.getUser_id()));
			} else {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid Google token"));
			}
		} catch (Exception e) {
			// Catch all exceptions and return a detailed error message
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("error", "Google login error: " + e.getMessage()));
		}
	}

	@PutMapping("/update/{id}")
	public ResponseEntity<User> updateUser(@PathVariable Integer id, @RequestBody User updatedUser) {
		Optional<User> updated = userService.updateUser(id, updatedUser);

		return updated.map(user -> ResponseEntity.ok(user))
				.orElseGet(() -> ResponseEntity.notFound().build());
	}
}
