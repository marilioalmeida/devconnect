package com.devconnect.api.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import com.devconnect.api.friendship.controller.request.FriendshipRequest;
import com.devconnect.api.comment.controller.request.CommentRequest;
import com.devconnect.api.post.controller.request.PostRequest;
import com.devconnect.api.post.domain.Visibility;
import com.devconnect.api.user.controller.request.UserRequest;

@DisplayName("Integration: full social journey")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SocialJourneyIntegrationTest extends IntegrationTestBase {

	private static final String EMAIL_ANA = "ana.souza@devconnect.com";
	private static final String EMAIL_BRUNO = "bruno.lima@devconnect.com";
	private static final String PASSWORD = "securePassword123";

	private Long idAna;
	private Long idBruno;
	private String tokenAna;
	private String tokenBruno;
	private Long publicPostId;
	private Long privatePostId;
	private Long friendshipId;

	@BeforeAll
	void prepareDatabase() {
		cleanDatabase();
	}

	@Test
	@Order(1)
	@DisplayName("Ana and Bruno sign up and start out active")
	void shouldRegisterBothUsers() throws Exception {

		idAna = register("Ana Souza", EMAIL_ANA);
		idBruno = register("Bruno Lima", EMAIL_BRUNO);
	}

	@Test
	@Order(2)
	@DisplayName("Both log in and receive distinct tokens")
	void shouldAuthenticateBothUsers() throws Exception {

		tokenAna = authenticate(EMAIL_ANA, PASSWORD);
		tokenBruno = authenticate(EMAIL_BRUNO, PASSWORD);
	}

	@Test
	@Order(3)
	@DisplayName("Ana publishes one public and one private post")
	void shouldPublishAnaPosts() throws Exception {

		publicPostId = publish(tokenAna, "Started learning Spring Boot today", Visibility.PUBLIC);
		privatePostId = publish(tokenAna, "Personal study notes", Visibility.PRIVATE);
	}

	@Test
	@Order(4)
	@DisplayName("Without a friendship, Bruno's feed does not show even Ana's public post")
	void shouldNotIncludeStrangerPostsInFeed() throws Exception {

		mockMvc.perform(authenticated(get("/posts/feed"), tokenBruno))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content").isEmpty())
				.andExpect(jsonPath("$.page.totalElements").value(0));
	}

	@Test
	@Order(5)
	@DisplayName("Bruno likes Ana's public post even without a friendship")
	void shouldLikeStrangerPublicPost() throws Exception {

		mockMvc.perform(authenticated(post("/posts/{postId}/likes", publicPostId), tokenBruno))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.postId").value(publicPostId))
				.andExpect(jsonPath("$.likeCount").value(1))
				.andExpect(jsonPath("$.likedByCurrentUser").value(true));
	}

	@Test
	@Order(6)
	@DisplayName("Bruno cannot reach Ana's private post while they are not friends")
	void shouldNotLikeStrangerPrivatePost() throws Exception {

		mockMvc.perform(authenticated(post("/posts/{postId}/likes", privatePostId), tokenBruno))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.message").value("You do not have access to this post"));
	}

	@Test
	@Order(7)
	@DisplayName("Bruno comments on Ana's public post")
	void shouldCommentOnPublicPost() throws Exception {

		CommentRequest request = new CommentRequest();
		request.setContent("Nice, Ana! Ping me if you get stuck");

		mockMvc.perform(authenticated(post("/posts/{postId}/comments", publicPostId), tokenBruno)
				.contentType(MediaType.APPLICATION_JSON)
				.content(json(request)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.content").value(request.getContent()))
				.andExpect(jsonPath("$.author.fullName").value("Bruno Lima"))
				.andExpect(jsonPath("$.author.email").doesNotExist());
	}

	@Test
	@Order(8)
	@DisplayName("Bruno sends a friend request and it starts as pending")
	void shouldSendFriendRequest() throws Exception {

		FriendshipRequest request = new FriendshipRequest();
		request.setRecipientId(idAna);

		String body = mockMvc.perform(authenticated(post("/friendships"), tokenBruno)
				.contentType(MediaType.APPLICATION_JSON)
				.content(json(request)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.status").value("PENDING"))
				.andExpect(jsonPath("$.requester.id").value(idBruno))
				.andExpect(jsonPath("$.recipient.id").value(idAna))
				.andReturn()
				.getResponse()
				.getContentAsString();

		friendshipId = objectMapper.readTree(body).get("id").asLong();
	}

	@Test
	@Order(9)
	@DisplayName("Ana accepts the request")
	void shouldAcceptFriendship() throws Exception {

		mockMvc.perform(authenticated(patch("/friendships/{id}/accept", friendshipId), tokenAna))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("ACCEPTED"))
				.andExpect(jsonPath("$.respondedAt").isNotEmpty());
	}

	@Test
	@Order(10)
	@DisplayName("Once friends, Bruno's feed shows both of Ana's posts with the right counts")
	void shouldIncludeFriendPostsInFeed() throws Exception {

		mockMvc.perform(authenticated(get("/posts/feed"), tokenBruno))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.page.totalElements").value(2))
				.andExpect(jsonPath("$.content[0].id").value(privatePostId))
				.andExpect(jsonPath("$.content[0].visibility").value("PRIVATE"))
				.andExpect(jsonPath("$.content[0].author.fullName").value("Ana Souza"))
				.andExpect(jsonPath("$.content[0].author.email").doesNotExist())
				.andExpect(jsonPath("$.content[1].id").value(publicPostId))
				.andExpect(jsonPath("$.content[1].likeCount").value(1))
				.andExpect(jsonPath("$.content[1].commentCount").value(1))
				.andExpect(jsonPath("$.content[1].likedByCurrentUser").value(true));
	}

	@Test
	@Order(11)
	@DisplayName("Ana's profile now shows the private post to Bruno")
	void shouldShowPrivatePostOnProfileToFriend() throws Exception {

		mockMvc.perform(authenticated(get("/users/{userId}/posts", idAna), tokenBruno))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.page.totalElements").value(2))
				.andExpect(jsonPath("$.content[0].visibility").value("PRIVATE"))
				.andExpect(jsonPath("$.content[1].visibility").value("PUBLIC"));
	}

	private Long register(String fullName, String email) throws Exception {
		UserRequest request = new UserRequest();
		request.setFullName(fullName);
		request.setEmail(email);
		request.setBirthDate(LocalDate.of(1995, 3, 15));
		request.setPassword(PASSWORD);

		String body = mockMvc.perform(post("/users")
				.contentType(MediaType.APPLICATION_JSON)
				.content(json(request)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.email").value(email))
				.andExpect(jsonPath("$.active").value(true))
				.andReturn()
				.getResponse()
				.getContentAsString();

		return objectMapper.readTree(body).get("id").asLong();
	}

	private Long publish(String token, String content, Visibility visibility) throws Exception {
		PostRequest request = new PostRequest();
		request.setContent(content);
		request.setVisibility(visibility);

		String body = mockMvc.perform(authenticated(post("/posts"), token)
				.contentType(MediaType.APPLICATION_JSON)
				.content(json(request)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.visibility").value(visibility.name()))
				.andReturn()
				.getResponse()
				.getContentAsString();

		return objectMapper.readTree(body).get("id").asLong();
	}

	private MockHttpServletRequestBuilder authenticated(MockHttpServletRequestBuilder request, String token) {
		return request.header("Authorization", bearer(token));
	}

}
