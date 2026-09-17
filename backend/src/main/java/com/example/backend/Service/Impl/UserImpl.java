package com.example.backend.Service.Impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.backend.DTO.LoginDTO;
import com.example.backend.DTO.TokenDTO;
import com.example.backend.DTO.UserDTO;
import com.example.backend.Entity.Gender;
import com.example.backend.Entity.Role;
import com.example.backend.Entity.User;
import com.example.backend.Repo.UserRepo;
import com.example.backend.Security.CustomUserDetails;
import com.example.backend.Security.JwtService;
import com.example.backend.Service.UserService;

import jakarta.transaction.Transactional;

@Service
public class UserImpl implements UserService {

    @Autowired
    private UserRepo userRepo;
    
    @Autowired
    @Lazy
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private JwtService jwtService;

    @Override
    public List<User> getAllUsers() {
        return userRepo.findAll();
    }

    @Override
    @Transactional
    public int saveOrUpdateUser(UserDTO userDTO) throws Exception {
        
    	// Check whether the username or email already exists
        boolean userExists = userRepo.existsByUsernameOrEmail(userDTO.getUsername(), userDTO.getEmail());
        if (userExists) {
            throw new Exception("Username or email already exists");
        }

        User user = new User();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        
        try {
            Date date = formatter.parse(userDTO.getbirthDate());
            user.setBirthDate(date);
        } catch (ParseException e) {
            throw new Exception("Invalid date format");
        }
        
        BeanUtils.copyProperties(userDTO, user);
        user.setCreatedTime(LocalDateTime.now());

        // Encrypt the password
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encodedPassword = passwordEncoder.encode(userDTO.getPassword());
        user.setPassword(encodedPassword);

        // Set the gender
        if (Gender.Male.toString().equals(userDTO.getGender())) {
            user.setGender(Gender.Male);
        } else if (Gender.Female.toString().equals(userDTO.getGender())) {
            user.setGender(Gender.Female);
        } else if (Gender.Other.toString().equals(userDTO.getGender())) {
            user.setGender(Gender.Other);
        }

        user = userRepo.save(user);
        return user.getUser_id();
    }
    
    @Override
    public Optional<User> updateUser(Integer id, User updatedUser) {
        return userRepo.findById(id).map(user -> {
            user.setUsername(updatedUser.getUsername());
            user.setEmail(updatedUser.getEmail());
            user.setPassword(updatedUser.getPassword());
            user.setGender(updatedUser.getGender());
            // Role changes go through ManagerController/updateUserRole; leave the
            // existing role untouched here instead of forcing it back to USER,
            // which previously reset any ADMIN/MANAGER editing their own profile
            // back down to a plain USER.
            user.setBirthDate(updatedUser.getBirthDate());
            return userRepo.save(user);
        });
    }
    
    @Override
    public TokenDTO login(LoginDTO loginDTO) {
        try {
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginDTO.getEmail(),
                    loginDTO.getPassword()
                )
            );
        } catch (BadCredentialsException e) {
            throw new RuntimeException("Invalid credentials");
        }

        User user = userRepo.findByEmail(loginDTO.getEmail())
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        CustomUserDetails userDetails = new CustomUserDetails(user);
        String jwtToken = jwtService.generateToken(userDetails);

        // Get the user's role information
        List<String> roles = userDetails.getAuthorities()
            .stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.toList());

        return new TokenDTO(jwtToken, roles, user.getUsername(), user.getUser_id()); 

    }


	@Override
	 public User updateUserRole(Integer id, String newRole) {
        User user = userRepo.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        Role role = Role.valueOf(newRole.toUpperCase());
        user.setRole(role);
        return userRepo.save(user);
    }

	@Override
	public Optional<User> findByUsername(String username) {
		return userRepo.findByUsername(username);
	}
}

