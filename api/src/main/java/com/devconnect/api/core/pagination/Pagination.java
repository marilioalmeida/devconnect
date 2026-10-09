package com.devconnect.api.core.pagination;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Pagination {

	private static final int MAX_SIZE = 50;

	public Pageable withoutSort(Pageable pageable) {
		int size = Math.min(pageable.getPageSize(), MAX_SIZE);

		return PageRequest.of(pageable.getPageNumber(), size);
	}

}
