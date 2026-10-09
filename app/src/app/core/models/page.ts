export interface PageMetadata {
  size: number;
  number: number;
  totalElements: number;
  totalPages: number;
}

export interface Page<T> {
  content: T[];
  page: PageMetadata;
}

export function emptyPage<T>(size: number): Page<T> {
  return {
    content: [],
    page: { size: size, number: 0, totalElements: 0, totalPages: 0 },
  };
}
