package com.example.backend.Security;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

	@Override
	public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
	    OAuth2User oAuth2User = super.loadUser(userRequest); // Get the default OAuth2User
	    Map<String, Object> attributes = oAuth2User.getAttributes();

	    // Extract roles from the attributes (if the provider returns this data)
	    @SuppressWarnings("unchecked")
	    List<String> roles = (List<String>) attributes.getOrDefault("roles", List.of("USER")); // Default to the USER role

	    // Set the authorities
	    List<GrantedAuthority> authorities = roles.stream()
	        .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
	        .collect(Collectors.toList());

	    return new DefaultOAuth2User(authorities, attributes, "email"); // Use email as the primary identifier
	}

}

