package com.devconnect.api.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import com.devconnect.api.security.controller.request.LoginRequest;
import com.devconnect.api.user.domain.Role;
import com.devconnect.api.user.domain.User;
import com.devconnect.api.user.repository.UserRepository;

@DisplayName("Integration: authentication")
class AuthenticationIntegrationTest extends IntegrationTestBase {

	private static final String ACTIVE_EMAIL = "ana.souza@devconnect.com";
	private static final String INACTIVE_EMAIL = "bruno.lima@devconnect.com";
	private static final String PASSWORD = "securePassword123";
	private static final String INVALID_CREDENTIALS = "Invalid email or password";

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private JwtDecoder jwtDecoder;

	@BeforeEach
	void prepareDatabase() {
		cleanDatabase();
		store(ACTIVE_EMAIL, true);
		store(INACTIVE_EMAIL, false);
	}

	@Test
	@DisplayName("Should authenticate active user")
	void shouldAuthenticateActiveUser() throws Exception {

		String body = mockMvc.perform(post("/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(json(request(ACTIVE_EMAIL, PASSWORD))))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.tokenType").value("Bearer"))
				.andExpect(jsonPath("$.expiresIn").value(3600))
				.andReturn()
				.getResponse()
				.getContentAsString();

		String token = objectMapper.readTree(body).get("accessToken").asText();
		var jwt = jwtDecoder.decode(token);

		assertEquals("devconnect-api", jwt.getClaimAsString(JwtClaimNames.ISS));
		assertEquals(ACTIVE_EMAIL, jwt.getClaimAsString("email"));
		assertEquals("USER", jwt.getClaimAsString("scope"));
	}

	@Test
	@DisplayName("Should not authenticate with wrong password")
	void shouldNotAuthenticateWithWrongPassword() throws Exception {

		mockMvc.perform(post("/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(json(request(ACTIVE_EMAIL, "wrongPassword123"))))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.message").value(INVALID_CREDENTIALS));
	}

	@Test
	@DisplayName("Should not authenticate an unknown email")
	void shouldNotAuthenticateUnknownEmail() throws Exception {

		mockMvc.perform(post("/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(json(request("nobody@devconnect.com", PASSWORD))))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.message").value(INVALID_CREDENTIALS));
	}

	@Test
	@DisplayName("Should not authenticate inactive user")
	void shouldNotAuthenticateInactiveUser() throws Exception {

		mockMvc.perform(post("/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(json(request(INACTIVE_EMAIL, PASSWORD))))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.message").value(INVALID_CREDENTIALS));
	}

	@Test
	@DisplayName("Should not access protected route without token")
	void shouldNotAccessProtectedRouteWithoutToken() throws Exception {

		mockMvc.perform(get("/users/me"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	@DisplayName("Should access protected route with login token")
	void shouldAccessProtectedRouteWithLoginToken() throws Exception {

		String token = authenticate(ACTIVE_EMAIL, PASSWORD);

		mockMvc.perform(get("/users/me")
				.header("Authorization", bearer(token)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.email").value(ACTIVE_EMAIL))
				.andExpect(jsonPath("$.active").value(true));
	}

	@Test
	@DisplayName("Should not accept token of a deactivated user")
	void shouldNotAcceptTokenOfDeactivatedUser() throws Exception {

		String token = authenticate(ACTIVE_EMAIL, PASSWORD);

		User user = userRepository.findByEmail(ACTIVE_EMAIL).orElseThrow();
		user.setActive(false);
		userRepository.save(user);

		mockMvc.perform(get("/users/me")
				.header("Authorization", bearer(token)))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.message").value("Authenticated user not found"));
	}

	private LoginRequest request(String email, String password) {
		LoginRequest request = new LoginRequest();
		request.setEmail(email);
		request.setPassword(password);
		return request;
	}

	private void store(String email, boolean active) {
		User user = User.builder()
				.fullName("Integration User")
				.email(email)
				.birthDate(LocalDate.of(1995, 3, 15))
				.password(passwordEncoder.encode(PASSWORD))
				.active(active)
				.build();

		user.addRole(Role.builder().name("USER").build());

		userRepository.save(user);
	}

}
