package com.devconnect.api.user.domain;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import org.hibernate.annotations.BatchSize;

import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "users")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(of = "id")
@ToString(of = "id")
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 255)
	private String fullName;

	@Column(nullable = false, length = 255, unique = true)
	private String email;

	@Column(length = 50)
	private String nickname;

	@Column(nullable = false)
	private LocalDate birthDate;

	@Column(nullable = false, length = 128)
	private String password;

	@Column(length = 512)
	private String profileImage;

	@Column(nullable = false)
	private boolean active;

	@Builder.Default
	@BatchSize(size = 50)
	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	private List<Role> roles = new ArrayList<>();

	public void addRole(Role role) {
		this.roles.add(role);
		role.setUser(this);
	}

}
