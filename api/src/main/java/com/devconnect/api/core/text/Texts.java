package com.devconnect.api.core.text;

import java.util.Locale;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Texts {

	public String required(String value) {
		return value == null ? null : value.trim();
	}

	public String optional(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}

		return value.trim();
	}

	public String email(String value) {
		return value == null ? null : value.trim().toLowerCase(Locale.ROOT);
	}

}
