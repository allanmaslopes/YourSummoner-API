package com.your.summoner.infrastructure.adapter.in.rest;

import com.your.summoner.application.port.out.CatalogStorageUnavailableException;
import com.your.summoner.domain.exception.ExternalServiceException;
import com.your.summoner.domain.exception.InvalidInputException;
import com.your.summoner.domain.exception.RateLimitExceededException;
import com.your.summoner.domain.exception.ResourceNotFoundException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.net.URI;

/**
 * Exceções conhecidas → RFC 9457 ({@code application/problem+json}). Erros do próprio Spring MVC (validação,
 * tipo de parâmetro, rota inexistente) ficam com a classe base. O resto vira um 500 genérico: nunca stack trace
 * nem a mensagem da exceção.
 */
@RestControllerAdvice
public class ProblemDetailsHandler extends ResponseEntityExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(ProblemDetailsHandler.class);

    @ExceptionHandler(InvalidInputException.class)
    ResponseEntity<ProblemDetail> invalidInput(InvalidInputException e) {
        return respond(problem("invalid-input", "Invalid request", HttpStatus.BAD_REQUEST, e.getMessage()));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    ResponseEntity<ProblemDetail> notFound(ResourceNotFoundException e) {
        var problem = problem("not-found", e.resource() + " not found", HttpStatus.NOT_FOUND, e.getMessage());
        problem.setProperty("resource", e.resource());
        problem.setProperty("identifier", e.identifier());
        return respond(problem);
    }

    @ExceptionHandler(RateLimitExceededException.class)
    ResponseEntity<ProblemDetail> rateLimited(RateLimitExceededException e) {
        var problem = problem("rate-limited", "Riot API rate limit exceeded", HttpStatus.TOO_MANY_REQUESTS,
                "Too many requests to the Riot API, try again later");
        var response = ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS);
        e.retryAfter().ifPresent(d -> response.header("Retry-After", String.valueOf(d.toSeconds())));
        return response.body(problem);
    }

    @ExceptionHandler(ExternalServiceException.class)
    ResponseEntity<ProblemDetail> externalService(ExternalServiceException e) {
        LOG.warn("External service failure: service={}, reason={}, exception={}",
                e.service(), e.reason(), e.getClass().getSimpleName());
        return respond(switch (e.reason()) {
            case UNAVAILABLE -> {
                yield problem("upstream-unavailable", e.service() + " unavailable", HttpStatus.SERVICE_UNAVAILABLE,
                        e.service() + " is temporarily unavailable");
            }
            case AUTHENTICATION -> {
                yield problem("upstream-authentication", e.service() + " rejected the credentials",
                        HttpStatus.BAD_GATEWAY, "Upstream rejected the service credentials");
            }
            case BAD_RESPONSE -> {
                yield problem("upstream-bad-response", "Unexpected response from " + e.service(),
                        HttpStatus.BAD_GATEWAY, "The upstream service returned an unexpected response");
            }
        });
    }

    @ExceptionHandler(CallNotPermittedException.class)
    ResponseEntity<ProblemDetail> circuitOpen(CallNotPermittedException e) {
        return respond(problem("upstream-unavailable", "Upstream temporarily disabled", HttpStatus.SERVICE_UNAVAILABLE,
                "Too many recent failures talking to an external service, try again in a few seconds"));
    }

    @ExceptionHandler(CatalogStorageUnavailableException.class)
    ResponseEntity<ProblemDetail> catalogUnavailable(CatalogStorageUnavailableException e) {
        LOG.warn("Catalog storage unavailable: {}", e.getMessage());
        return respond(problem("upstream-unavailable", "Catalog unavailable", HttpStatus.SERVICE_UNAVAILABLE,
                "The catalog storage is temporarily unavailable"));
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ProblemDetail> unexpected(Exception e) {
        LOG.error("Unhandled exception", e);
        return respond(problem("internal-error", "Internal server error", HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred"));
    }

    private static ResponseEntity<ProblemDetail> respond(ProblemDetail problem) {
        return ResponseEntity.status(problem.getStatus()).body(problem);
    }

    private static ProblemDetail problem(String slug, String title, HttpStatus status, String detail) {
        var problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setType(URI.create("urn:your-summoner:problem:" + slug));
        problem.setTitle(title);
        return problem;
    }
}
