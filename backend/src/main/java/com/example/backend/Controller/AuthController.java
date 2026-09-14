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
 * 註冊、登入（含 Google 登入）、更新使用者資料。
 * 從原本的 CinemaController 拆出來，讓職責單一。
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

	// 原本這裡是寫死的 Collections.singletonList("")：
	// GoogleIdTokenVerifier 會拿 Google 核發的 token 裡的 aud claim 跟這個清單比對，
	// 但 aud 永遠不可能是空字串，代表 Google 登入的驗證步驟實際上永遠會失敗（idToken 永遠是 null）。
	// 改成從設定檔讀取真正的 Google OAuth Client ID，Google 登入才驗證得過。
	@Value("${google.oauth.client-id}")
	private String googleClientId;

	/**
	 * 用户注册
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
			return ResponseEntity.status(400).body(response); // 返回400狀態碼以表示請求錯誤
		}
	}

	/**
	 * 用户登录
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
					"name", name
				));
			} else {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid Google token"));
			}
		} catch (Exception e) {
			// 捕獲所有異常並返回詳細的錯誤訊息
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Google login error: " + e.getMessage()));
		}
	}

	@PutMapping("/update/{id}")
	public ResponseEntity<User> updateUser(@PathVariable Integer id, @RequestBody User updatedUser) {
		Optional<User> updated = userService.updateUser(id, updatedUser);

		return updated.map(user -> ResponseEntity.ok(user))
				.orElseGet(() -> ResponseEntity.notFound().build());
	}
}
