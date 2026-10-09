package com.devconnect.api.user.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.devconnect.api.core.config.OpenApiConfig;
import com.devconnect.api.user.controller.request.UpdateProfileRequest;
import com.devconnect.api.user.controller.request.UserRequest;
import com.devconnect.api.user.controller.response.ProfileResponse;
import com.devconnect.api.user.controller.response.UserResponse;
import com.devconnect.api.user.service.UpdateProfileService;
import com.devconnect.api.user.service.GetCurrentUserService;
import com.devconnect.api.user.service.SearchUsersService;
import com.devconnect.api.user.service.GetProfileService;
import com.devconnect.api.user.service.CreateUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "Users")
public class UserController {

	private final CreateUserService createUserService;
	private final GetCurrentUserService getCurrentUserService;
	private final UpdateProfileService updateProfileService;
	private final SearchUsersService searchUsersService;
	private final GetProfileService getProfileService;

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "Register a new user")
	public UserResponse create(@RequestBody @Valid UserRequest request) {
		return createUserService.create(request);
	}

	@GetMapping("/me")
	@PreAuthorize("hasAuthority('SCOPE_USER')")
	@SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME)
	@Operation(summary = "Get the authenticated user")
	public UserResponse getCurrent() {
		return getCurrentUserService.getCurrent();
	}

	@PutMapping("/me")
	@PreAuthorize("hasAuthority('SCOPE_USER')")
	@SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME)
	@Operation(summary = "Update the name, nickname and profile image of the authenticated user")
	public UserResponse updateProfile(@RequestBody @Valid UpdateProfileRequest request) {
		return updateProfileService.update(request);
	}

	@GetMapping
	@PreAuthorize("hasAuthority('SCOPE_USER')")
	@SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME)
	@Operation(summary = "Search users by name or email to send friend requests")
	public Page<ProfileResponse> search(
			@RequestParam(required = false) String search,
			Pageable pageable) {
		return searchUsersService.search(search, pageable);
	}

	@GetMapping("/{userId}")
	@PreAuthorize("hasAuthority('SCOPE_USER')")
	@SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME)
	@Operation(summary = "Get a user profile and the friendship status with them")
	public ProfileResponse getProfile(@PathVariable Long userId) {
		return getProfileService.get(userId);
	}

}
