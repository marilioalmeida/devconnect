package com.devconnect.api.friendship.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.devconnect.api.friendship.controller.request.FriendshipRequest;
import com.devconnect.api.friendship.controller.response.FriendResponse;
import com.devconnect.api.friendship.controller.response.FriendshipResponse;
import com.devconnect.api.friendship.controller.response.FriendRequestResponse;
import com.devconnect.api.friendship.service.AcceptFriendshipService;
import com.devconnect.api.friendship.service.ListFriendsService;
import com.devconnect.api.friendship.service.ListReceivedFriendRequestsService;
import com.devconnect.api.friendship.service.DeleteFriendshipService;
import com.devconnect.api.friendship.service.SendFriendRequestService;
import com.devconnect.api.core.config.OpenApiConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/friendships")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('SCOPE_USER')")
@SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME)
@Tag(name = "Friendships")
public class FriendshipController {

	private final SendFriendRequestService sendFriendRequestService;
	private final AcceptFriendshipService acceptFriendshipService;
	private final DeleteFriendshipService deleteFriendshipService;
	private final ListReceivedFriendRequestsService listReceivedFriendRequestsService;
	private final ListFriendsService listFriendsService;

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "Send a friend request")
	public FriendshipResponse send(@RequestBody @Valid FriendshipRequest request) {
		return sendFriendRequestService.send(request);
	}

	@PatchMapping("/{id}/accept")
	@Operation(summary = "Accept a received friend request")
	public FriendshipResponse accept(@PathVariable Long id) {
		return acceptFriendshipService.accept(id);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "Decline a request or remove a friendship")
	public void delete(@PathVariable Long id) {
		deleteFriendshipService.delete(id);
	}

	@GetMapping("/requests")
	@Operation(summary = "List received friend requests")
	public List<FriendRequestResponse> listReceivedRequests() {
		return listReceivedFriendRequestsService.list();
	}

	@GetMapping
	@Operation(summary = "List friends, filtering by name or email")
	public Page<FriendResponse> listFriends(
			@RequestParam(required = false) String search,
			Pageable pageable) {
		return listFriendsService.list(search, pageable);
	}

}
