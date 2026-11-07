package com.your.summoner.infrastructure.adapter.in.rest.admin;

import com.your.summoner.domain.exception.InvalidInputException;
import com.your.summoner.infrastructure.security.InvalidCredentialsException;
import com.your.summoner.infrastructure.security.TooManyLoginAttemptsException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Messages are fixed strings: nothing from the request or from a secret reaches the body. */
// Antes do ProblemDetailsHandler (catch-all Exception): sem ordem explícita, a ordem de varredura
// muda entre Windows e Linux e o login inválido virava 500 no CI.
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(basePackageClasses = AdminExceptionHandler.class)
class AdminExceptionHandler {

    @ExceptionHandler(InvalidCredentialsException.class)
    ProblemDetail invalidCredentials() {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, "Invalid credentials");
    }

    @ExceptionHandler(TooManyLoginAttemptsException.class)
    ResponseEntity<ProblemDetail> tooMany(TooManyLoginAttemptsException e) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .header(HttpHeaders.RETRY_AFTER, Long.toString(e.retryAfterSeconds()))
                .body(ProblemDetail.forStatusAndDetail(HttpStatus.TOO_MANY_REQUESTS, "Too many failed login attempts"));
    }

    @ExceptionHandler(InvalidInputException.class)
    ProblemDetail invalidInput(InvalidInputException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
    }
}
