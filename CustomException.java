package com.topicallocation.topic.exceptionHandler;

import java.util.Date;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.topicallocation.topic.exception.CommonExceptionAll;
import com.topicallocation.topic.exception.IsTopicException;
import com.topicallocation.topic.exception.IsUserException;



@ControllerAdvice
public class CustomException extends ResponseEntityExceptionHandler {

	@ExceptionHandler(IsUserException.class)
	public final ResponseEntity<ExceptionResponse> handleUserNotFoundException(IsUserException ex,
			WebRequest request) {
		ExceptionResponse exceptionResponse = new ExceptionResponse(new Date(), ex.getMessage(),
				request.getDescription(false));
		return new ResponseEntity<>(exceptionResponse, HttpStatus.NOT_FOUND);
	}

	@ExceptionHandler(CommonExceptionAll.class)
	public final ResponseEntity<ExceptionResponse> HandlePasswordNotMatchException(CommonExceptionAll ex,
			WebRequest request) {
		ExceptionResponse exceptionResponse = new ExceptionResponse(new Date(), ex.getMessage(),
				request.getDescription(false));
		return new ResponseEntity<>(exceptionResponse, HttpStatus.NOT_FOUND);
	}
	
	@ExceptionHandler(IsTopicException.class)
	public final ResponseEntity<ExceptionResponse> HandleTopicNotFoundException(IsTopicException ex,
			WebRequest request) {
		ExceptionResponse exceptionResponse = new ExceptionResponse(new Date(), ex.getMessage(),
				request.getDescription(false));
		return new ResponseEntity<>(exceptionResponse, HttpStatus.NOT_FOUND);
	}


	
	@SuppressWarnings("deprecation")
	@ExceptionHandler(MultipartException.class)
	public final ResponseEntity<ExceptionResponse> handleError1(MultipartException e, WebRequest request) {
		ExceptionResponse exceptionResponse = new ExceptionResponse(new Date(), e.getMessage(),
				request.getDescription(false));
		return new ResponseEntity<>(exceptionResponse, HttpStatus.REQUEST_ENTITY_TOO_LARGE);
	}
}
