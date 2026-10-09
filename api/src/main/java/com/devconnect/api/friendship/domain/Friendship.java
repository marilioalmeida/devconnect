package com.devconnect.api.friendship.domain;

import java.time.LocalDateTime;

import com.devconnect.api.user.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "friendships", uniqueConstraints = @UniqueConstraint(columnNames = { "requester_id", "recipient_id" }))
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(of = "id")
@ToString(of = "id")
public class Friendship {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "requester_id", nullable = false)
	private User requester;

	@ManyToOne
	@JoinColumn(name = "recipient_id", nullable = false)
	private User recipient;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private FriendshipStatus status;

	@Column(nullable = false)
	private LocalDateTime requestedAt;

	private LocalDateTime respondedAt;

}
