// GlobalExceptionHandler.java
package com.cotato.backend14th.global.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.cotato.backend14th.global.common.response.ErrorResponse;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	// 우리가 직접 던진 Backend14thException을 처리해요
	@ExceptionHandler(Backend14thException.class)
	public ResponseEntity<ErrorResponse> handleBackend14thException(
		Backend14thException e, HttpServletRequest request
	) {
		log.warn("[Backend14thException] {} {} | code={} | message={}",
			request.getMethod(), request.getRequestURI(),
			e.getErrorCode().getCode(), e.getMessage());

		return ResponseEntity
			.status(e.getErrorCode().getHttpStatus())
			.body(ErrorResponse.of(e.getErrorCode(), request));
	}

	// @Valid 검증에 실패하면 첫 번째 필드의 안내 문구를 그대로 내려줘요
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(
		MethodArgumentNotValidException e, HttpServletRequest request
	) {
		String message = e.getBindingResult().getFieldErrors().stream()
			.findFirst()
			.map(FieldError::getDefaultMessage)
			.orElse(ErrorCode.INVALID_INPUT.getMessage());
		log.warn("[ValidationFailed] {} {} | message={}", request.getMethod(), request.getRequestURI(), message);

		return ResponseEntity
			.status(ErrorCode.INVALID_INPUT.getHttpStatus())
			.body(ErrorResponse.of(ErrorCode.INVALID_INPUT, message, request));
	}

	// 본문을 읽을 수 없거나(JSON 오류, 없는 enum 값 등) 파라미터 타입이 안 맞는 경우도 400이에요
	@ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
	public ResponseEntity<ErrorResponse> handleUnreadableRequest(
		Exception e, HttpServletRequest request
	) {
		log.warn("[UnreadableRequest] {} {} | {}", request.getMethod(), request.getRequestURI(), e.getMessage());

		return ResponseEntity
			.status(ErrorCode.INVALID_INPUT.getHttpStatus())
			.body(ErrorResponse.of(ErrorCode.INVALID_INPUT, request));
	}

	// 그 외 예상치 못한 모든 예외는 500으로 처리해요
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleException(
		Exception e, HttpServletRequest request
	) {
		log.error("[UnhandledException] {} {} | {}",
			request.getMethod(), request.getRequestURI(), e.getMessage(), e);

		return ResponseEntity
			.status(ErrorCode.COMMON_INTERNAL_SERVER_ERROR.getHttpStatus())
			.body(ErrorResponse.of(ErrorCode.COMMON_INTERNAL_SERVER_ERROR, request));
	}
}