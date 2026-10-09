package com.devconnect.api.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.devconnect.api.security.controller.request.LoginRequest;

@SpringBootTest(properties = {
		"spring.jpa.show-sql=false",
		"logging.level.org.hibernate.orm.jdbc.bind=info",
		"server.servlet.encoding.force-response=true"
})
@AutoConfigureMockMvc
public abstract class IntegrationTestBase {

	private static final String TABLES = "post_likes, comments, posts, friendships, user_roles, users";

	private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
			.withDatabaseName("devconnect")
			.withUsername("devconnect")
			.withPassword("devconnect");

	static {
		POSTGRES.start();
	}

	@DynamicPropertySource
	static void configureDatasource(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
		registry.add("spring.datasource.username", POSTGRES::getUsername);
		registry.add("spring.datasource.password", POSTGRES::getPassword);
	}

	@Autowired
	protected MockMvc mockMvc;

	@Autowired
	protected ObjectMapper objectMapper;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	protected void cleanDatabase() {
		jdbcTemplate.execute("TRUNCATE TABLE " + TABLES + " RESTART IDENTITY CASCADE");
	}

	protected String authenticate(String email, String password) throws Exception {
		LoginRequest request = new LoginRequest();
		request.setEmail(email);
		request.setPassword(password);

		String body = mockMvc.perform(post("/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(json(request)))
				.andExpect(status().isOk())
				.andReturn()
				.getResponse()
				.getContentAsString();

		return objectMapper.readTree(body).get("accessToken").asText();
	}

	protected String json(Object body) throws Exception {
		return objectMapper.writeValueAsString(body);
	}

	protected String bearer(String token) {
		return "Bearer " + token;
	}

}
