package com.devconnect.api.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.devconnect.api.user.controller.request.UserRequest;
import com.devconnect.api.user.domain.Role;
import com.devconnect.api.user.domain.User;
import com.devconnect.api.user.repository.UserRepository;

@DisplayName("Integration: validation and error handling")
class ValidationIntegrationTest extends IntegrationTestBase {

	private static final String EMAIL = "ana.souza@devconnect.com";
	private static final String NO_ROLE_EMAIL = "no.role@devconnect.com";
	private static final String PASSWORD = "securePassword123";

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@BeforeEach
	void prepareDatabase() {
		cleanDatabase();
		store(EMAIL, true);
		store(NO_ROLE_EMAIL, false);
	}

	@Test
	@DisplayName("Should respond with full error contract")
	void shouldRespondWithFullErrorContract() throws Exception {

		UserRequest request = valid();
		request.setEmail("not-an-email");

		mockMvc.perform(post("/users")
				.contentType(MediaType.APPLICATION_JSON)
				.content(json(request)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.timestamp").exists())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.error").value("Bad Request"))
				.andExpect(jsonPath("$.message").exists())
				.andExpect(jsonPath("$.path").value("/users"));
	}

	@Test
	@DisplayName("Should not register with malformed email")
	void shouldNotRegisterWithMalformedEmail() throws Exception {

		UserRequest request = valid();
		request.setEmail("ana.souza.devconnect");

		mockMvc.perform(post("/users")
				.contentType(MediaType.APPLICATION_JSON)
				.content(json(request)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("email: must be a valid email address"));
	}

	@Test
	@DisplayName("Should not register with blank name")
	void shouldNotRegisterWithBlankName() throws Exception {

		UserRequest request = valid();
		request.setFullName("   ");

		mockMvc.perform(post("/users")
				.contentType(MediaType.APPLICATION_JSON)
				.content(json(request)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("fullName: must not be blank"));
	}

	@Test
	@DisplayName("Should not register with short password")
	void shouldNotRegisterWithShortPassword() throws Exception {

		UserRequest request = valid();
		request.setPassword("short");

		mockMvc.perform(post("/users")
				.contentType(MediaType.APPLICATION_JSON)
				.content(json(request)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("password: must be between 8 and 128 characters"));
	}

	@Test
	@DisplayName("Should not register with future birth date")
	void shouldNotRegisterWithFutureBirthDate() throws Exception {

		UserRequest request = valid();
		request.setBirthDate(LocalDate.now().plusDays(1));

		mockMvc.perform(post("/users")
				.contentType(MediaType.APPLICATION_JSON)
				.content(json(request)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("birthDate: must be a date in the past"));
	}

	@Test
	@DisplayName("Should report all invalid fields")
	void shouldReportAllInvalidFields() throws Exception {

		UserRequest request = new UserRequest();
		request.setFullName("  ");
		request.setEmail("not-an-email");
		request.setPassword("short");

		mockMvc.perform(post("/users")
				.contentType(MediaType.APPLICATION_JSON)
				.content(json(request)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value(
						"birthDate: is required; "
								+ "email: must be a valid email address; "
								+ "fullName: must not be blank; "
								+ "password: must be between 8 and 128 characters"));
	}

	@Test
	@DisplayName("Should not leak parser details on malformed JSON")
	void shouldNotLeakParserDetailsOnMalformedJson() throws Exception {

		mockMvc.perform(post("/users")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"fullName\": "))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("Request body is missing or malformed"))
				.andExpect(jsonPath("$.message").value(Matchers.not(Matchers.containsString("JSON"))));
	}

	@Test
	@DisplayName("Should not accept non numeric ID")
	void shouldNotAcceptNonNumericId() throws Exception {

		String token = authenticate(EMAIL, PASSWORD);

		mockMvc.perform(get("/users/{userId}", "abc")
				.header("Authorization", bearer(token)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("Invalid value for parameter userId"));
	}

	@Test
	@DisplayName("Should not access route without required role")
	void shouldNotAccessRouteWithoutRequiredRole() throws Exception {

		String token = authenticate(NO_ROLE_EMAIL, PASSWORD);

		mockMvc.perform(get("/posts/feed")
				.header("Authorization", bearer(token)))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.message").value("You do not have permission to perform this operation"));
	}

	@Test
	@DisplayName("Should respond not found for an unknown route")
	void shouldRespondNotFoundForUnknownRoute() throws Exception {

		String token = authenticate(EMAIL, PASSWORD);

		mockMvc.perform(get("/route-that-does-not-exist")
				.header("Authorization", bearer(token)))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message").value("Resource not found"));
	}

	@Test
	@DisplayName("Should respond method not allowed for the wrong method")
	void shouldRespondMethodNotAllowedForWrongMethod() throws Exception {

		String token = authenticate(EMAIL, PASSWORD);

		mockMvc.perform(post("/posts/feed")
				.header("Authorization", bearer(token)))
				.andExpect(status().isMethodNotAllowed())
				.andExpect(jsonPath("$.message").value("Method POST is not supported by this resource"));
	}

	@Test
	@DisplayName("Should treat email case insensitively")
	void shouldTreatEmailCaseInsensitively() throws Exception {

		UserRequest request = valid();
		request.setEmail("New.Ana@DevConnect.com");

		mockMvc.perform(post("/users")
				.contentType(MediaType.APPLICATION_JSON)
				.content(json(request)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.email").value("new.ana@devconnect.com"));

		UserRequest duplicate = valid();
		duplicate.setEmail("NEW.ANA@devconnect.com");

		mockMvc.perform(post("/users")
				.contentType(MediaType.APPLICATION_JSON)
				.content(json(duplicate)))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.message").value("Email already registered"));

		authenticate("new.ana@devconnect.com", PASSWORD);
	}

	@Test
	@DisplayName("Should respond unsupported media type for the wrong content type")
	void shouldRespondUnsupportedMediaTypeForWrongContent() throws Exception {

		mockMvc.perform(post("/users")
				.contentType(MediaType.TEXT_PLAIN)
				.content("anything"))
				.andExpect(status().isUnsupportedMediaType())
				.andExpect(jsonPath("$.message").value("Unsupported content type"));
	}

	private UserRequest valid() {
		UserRequest request = new UserRequest();
		request.setFullName("Ana Souza");
		request.setEmail("new.ana@devconnect.com");
		request.setBirthDate(LocalDate.of(1995, 3, 15));
		request.setPassword(PASSWORD);
		return request;
	}

	private void store(String email, boolean withRole) {
		User user = User.builder()
				.fullName("Integration User")
				.email(email)
				.birthDate(LocalDate.of(1995, 3, 15))
				.password(passwordEncoder.encode(PASSWORD))
				.active(true)
				.build();

		if (withRole) {
			user.addRole(Role.builder().name("USER").build());
		}

		userRepository.save(user);
	}

}
