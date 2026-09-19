package com.example.backend.Service.Impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.example.backend.DTO.UserDTO;
import com.example.backend.Entity.Role;
import com.example.backend.Entity.User;
import com.example.backend.Repo.UserRepo;

// Unit tests: UserRepo is mocked, nothing here touches a real database.
// Covers the password-strength rule added to both signup and profile update
// (8+ characters, at least one letter and one digit), plus that the raw
// password is never what actually gets persisted.
@ExtendWith(MockitoExtension.class)
class UserImplTest {

    @Mock
    private UserRepo userRepo;

    @InjectMocks
    private UserImpl userImpl;

    private UserDTO validSignupDTO() {
        UserDTO dto = new UserDTO();
        dto.setUsername("newUser");
        dto.setEmail("newuser@example.com");
        dto.setPassword("abcd1234"); // 8 chars, has both a letter and a digit
        dto.setBirthDate("2000-01-01");
        dto.setGender("Male");
        return dto;
    }

    // ---- saveOrUpdateUser (signup) ----

    @Test
    void saveOrUpdateUser_passwordTooShort_throwsException() {
        UserDTO dto = validSignupDTO();
        dto.setPassword("abc123"); // only 6 characters
        when(userRepo.existsByUsernameOrEmail(dto.getUsername(), dto.getEmail())).thenReturn(false);

        Exception ex = assertThrows(Exception.class, () -> userImpl.saveOrUpdateUser(dto));
        assertTrue(ex.getMessage().contains("Password must be at least 8 characters"));
        verify(userRepo, never()).save(any());
    }

    @Test
    void saveOrUpdateUser_passwordWithoutDigit_throwsException() {
        UserDTO dto = validSignupDTO();
        dto.setPassword("onlylettershere"); // long enough, but no digit
        when(userRepo.existsByUsernameOrEmail(dto.getUsername(), dto.getEmail())).thenReturn(false);

        assertThrows(Exception.class, () -> userImpl.saveOrUpdateUser(dto));
        verify(userRepo, never()).save(any());
    }

    @Test
    void saveOrUpdateUser_usernameOrEmailAlreadyExists_throwsException() {
        UserDTO dto = validSignupDTO();
        when(userRepo.existsByUsernameOrEmail(dto.getUsername(), dto.getEmail())).thenReturn(true);

        Exception ex = assertThrows(Exception.class, () -> userImpl.saveOrUpdateUser(dto));
        assertTrue(ex.getMessage().contains("already exists"));
        verify(userRepo, never()).save(any());
    }

    @Test
    void saveOrUpdateUser_validInput_encodesPasswordBeforeSaving() throws Exception {
        UserDTO dto = validSignupDTO();
        when(userRepo.existsByUsernameOrEmail(dto.getUsername(), dto.getEmail())).thenReturn(false);
        when(userRepo.save(any(User.class))).thenAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            saved.setUser_id(42);
            return saved;
        });

        int savedId = userImpl.saveOrUpdateUser(dto);
        assertEquals(42, savedId);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepo).save(captor.capture());
        String storedPassword = captor.getValue().getPassword();

        // The DB should never see the raw password, only its BCrypt hash.
        assertNotEquals(dto.getPassword(), storedPassword);
        assertTrue(new BCryptPasswordEncoder().matches(dto.getPassword(), storedPassword));
    }

    // ---- updateUser (profile update) ----

    private User existingUser() {
        User user = new User();
        user.setUser_id(1);
        user.setUsername("oldName");
        user.setEmail("old@example.com");
        user.setPassword(new BCryptPasswordEncoder().encode("originalPass1"));
        user.setRole(Role.USER);
        return user;
    }

    @Test
    void updateUser_userNotFound_returnsEmptyOptional() {
        when(userRepo.findById(99)).thenReturn(Optional.empty());

        Optional<User> result = userImpl.updateUser(99, new User());

        assertTrue(result.isEmpty());
        verify(userRepo, never()).save(any());
    }

    @Test
    void updateUser_weakNewPassword_throwsIllegalArgumentException() {
        when(userRepo.findById(1)).thenReturn(Optional.of(existingUser()));

        User updates = new User();
        updates.setUsername("oldName");
        updates.setEmail("old@example.com");
        updates.setPassword("short"); // fails the strength rule

        assertThrows(IllegalArgumentException.class, () -> userImpl.updateUser(1, updates));
        verify(userRepo, never()).save(any());
    }

    @Test
    void updateUser_blankPassword_keepsExistingPasswordUnchanged() {
        User existing = existingUser();
        String originalHash = existing.getPassword();
        when(userRepo.findById(1)).thenReturn(Optional.of(existing));
        when(userRepo.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User updates = new User();
        updates.setUsername("newName");
        updates.setEmail("old@example.com");
        updates.setPassword(""); // left blank -> should not touch the password

        Optional<User> result = userImpl.updateUser(1, updates);

        assertTrue(result.isPresent());
        assertEquals(originalHash, result.get().getPassword());
        assertEquals("newName", result.get().getUsername());
    }

    @Test
    void updateUser_validNewPassword_getsEncodedBeforeSaving() {
        when(userRepo.findById(1)).thenReturn(Optional.of(existingUser()));
        when(userRepo.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User updates = new User();
        updates.setUsername("oldName");
        updates.setEmail("old@example.com");
        updates.setPassword("newPass123");

        Optional<User> result = userImpl.updateUser(1, updates);

        assertTrue(result.isPresent());
        String storedPassword = result.get().getPassword();
        assertNotEquals("newPass123", storedPassword);
        assertTrue(new BCryptPasswordEncoder().matches("newPass123", storedPassword));
    }
}
