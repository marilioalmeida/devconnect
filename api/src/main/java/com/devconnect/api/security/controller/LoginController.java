package com.devconnect.api.security.controller;

import java.time.Instant;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.devconnect.api.core.text.Texts;
import com.devconnect.api.security.controller.request.LoginRequest;
import com.devconnect.api.security.controller.response.LoginResponse;
import com.devconnect.api.user.domain.Role;
import com.devconnect.api.user.domain.User;
import com.devconnect.api.user.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/login")
@RequiredArgsConstructor
@Tag(name = "Authentication")
public class LoginController {

	private static final String ISSUER = "devconnect-api";
	private static final String TOKEN_TYPE = "Bearer";

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtEncoder jwtEncoder;

	@Value("${api.jwt.expiration-seconds}")
	private Long expirationSeconds;

	@PostMapping
	@Operation(summary = "Authenticate and get an access token")
	public LoginResponse login(@RequestBody @Valid LoginRequest request) {
		var user = userRepository.findByEmail(Texts.email(request.getEmail()))
			.filter(found -> passwordEncoder.matches(request.getPassword(), found.getPassword()))
			.filter(User::isActive)
			.orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

		var now = Instant.now();

		var claims = JwtClaimsSet.builder()
			.issuer(ISSUER)
			.subject(user.getFullName())
			.claim("email", user.getEmail())
			.claim("scope", buildScope(user))
			.issuedAt(now)
			.expiresAt(now.plusSeconds(expirationSeconds))
			.build();

		var token = jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();

		return LoginResponse.builder()
			.accessToken(token)
			.tokenType(TOKEN_TYPE)
			.expiresIn(expirationSeconds)
			.build();
	}

	private String buildScope(User user) {
		return user.getRoles().stream()
			.map(Role::getName)
			.collect(Collectors.joining(" "));
	}

}
