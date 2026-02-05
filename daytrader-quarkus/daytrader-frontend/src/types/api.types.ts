// Common API types
export interface ApiError {
  error: string;
  message: string;
  timestamp: string;
  path?: string;
  traceId?: string;
}

export interface ValidationError {
  field: string;
  message: string;
}

export interface ValidationErrorResponse extends ApiError {
  violations?: ValidationError[];
}

export interface PageMetadata {
  number: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface PagedResponse<T> {
  content: T[];
  page: PageMetadata;
}

