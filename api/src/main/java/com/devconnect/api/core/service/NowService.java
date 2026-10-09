package com.devconnect.api.core.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

@Service
public class NowService {

	public LocalDateTime getDateTime() {
		return LocalDateTime.now();
	}

}
