package com.devconnect.api.factories;

import java.time.LocalDateTime;

import com.devconnect.api.post.controller.request.UpdatePostVisibilityRequest;
import com.devconnect.api.post.controller.request.UpdatePostContentRequest;
import com.devconnect.api.post.controller.request.PostRequest;
import com.devconnect.api.post.domain.Post;
import com.devconnect.api.post.domain.Visibility;
import com.devconnect.api.user.domain.User;

public class PostFactory {

	public static final Long ID_POST = 100L;
	public static final Long PRIVATE_POST_ID = 101L;
	public static final String CONTENT = "First production deploy without breaking anything.";
	public static final LocalDateTime CREATED_AT = LocalDateTime.of(2026, 2, 5, 18, 45);

	public static Post.PostBuilder getBuilder() {
		return Post.builder()
			.id(ID_POST)
			.author(FriendshipFactory.getRequester())
			.content(CONTENT)
			.createdAt(CREATED_AT)
			.visibility(Visibility.PUBLIC);
	}

	public static Post getPublic() {
		return getBuilder().build();
	}

	public static Post getPrivate() {
		return getBuilder()
			.id(PRIVATE_POST_ID)
			.visibility(Visibility.PRIVATE)
			.build();
	}

	public static Post getPublicBy(User author) {
		return getBuilder().author(author).build();
	}

	public static Post getPrivateBy(User author) {
		return getBuilder()
			.id(PRIVATE_POST_ID)
			.author(author)
			.visibility(Visibility.PRIVATE)
			.build();
	}

	public static Post getNew() {
		return getBuilder().id(null).build();
	}

	public static UpdatePostContentRequest getUpdateContentRequest(String content) {
		UpdatePostContentRequest request = new UpdatePostContentRequest();
		request.setContent(content);
		return request;
	}

	public static PostRequest getRequest() {
		return getRequest(Visibility.PUBLIC);
	}

	public static PostRequest getRequest(Visibility visibility) {
		PostRequest request = new PostRequest();
		request.setContent(CONTENT);
		request.setVisibility(visibility);
		return request;
	}

	public static UpdatePostVisibilityRequest getUpdateVisibilityRequest(Visibility visibility) {
		UpdatePostVisibilityRequest request = new UpdatePostVisibilityRequest();
		request.setVisibility(visibility);
		return request;
	}
}
