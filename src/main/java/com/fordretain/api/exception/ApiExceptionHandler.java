package com.fordretain.api.exception;

import java.net.URI;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.TypeMismatchException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

	private static final Map<Integer, String> TITLES = Map.of(
			400, "Requisição inválida",
			401, "Não autenticado",
			403, "Acesso negado",
			404, "Não encontrado",
			405, "Método não permitido",
			409, "Conflito",
			415, "Tipo de mídia não suportado",
			422, "Regra de negócio violada",
			500, "Erro interno");

	private final Clock clock;

	public ApiExceptionHandler(Clock clock) {
		this.clock = clock;
	}

	@ExceptionHandler(ApiException.class)
	public ResponseEntity<Object> handleApiException(ApiException ex, WebRequest request) {
		ProblemDetail problem = problem(ex.getStatus(), ex.getCode(), ex.getMessage());
		return handleExceptionInternal(ex, problem, new HttpHeaders(), ex.getStatus(), request);
	}

	@ExceptionHandler(AuthenticationException.class)
	public ResponseEntity<Object> handleAuthentication(AuthenticationException ex, WebRequest request) {
		HttpHeaders headers = new HttpHeaders();
		ProblemDetail problem;

		if (ex instanceof InvalidBearerTokenException) {
			headers.set(HttpHeaders.WWW_AUTHENTICATE, "Bearer error=\"invalid_token\"");
			problem = problem(HttpStatus.UNAUTHORIZED, "INVALID_TOKEN", "Token inválido ou expirado.");
		} else {
			headers.set(HttpHeaders.WWW_AUTHENTICATE, "Bearer");
			problem = problem(HttpStatus.UNAUTHORIZED, "AUTHENTICATION_REQUIRED",
					"Envie um token de acesso no cabeçalho Authorization: Bearer <token>.");
		}
		return handleExceptionInternal(ex, problem, headers, HttpStatus.UNAUTHORIZED, request);
	}

	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<Object> handleAccessDenied(AccessDeniedException ex, WebRequest request) {
		ProblemDetail problem = problem(HttpStatus.FORBIDDEN, "ACCESS_DENIED",
				"Seu perfil não tem permissão para esta operação.");
		return handleExceptionInternal(ex, problem, new HttpHeaders(), HttpStatus.FORBIDDEN, request);
	}

	@ExceptionHandler(ObjectOptimisticLockingFailureException.class)
	public ResponseEntity<Object> handleConcurrentUpdate(ObjectOptimisticLockingFailureException ex,
			WebRequest request) {
		ProblemDetail problem = problem(HttpStatus.CONFLICT, "CONCURRENT_UPDATE",
				"O recurso foi alterado por outra requisição. Consulte de novo e repita a operação.");
		return handleExceptionInternal(ex, problem, new HttpHeaders(), HttpStatus.CONFLICT, request);
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<Object> handleDataIntegrity(DataIntegrityViolationException ex, WebRequest request) {
		ProblemDetail problem = problem(HttpStatus.CONFLICT, "DATA_INTEGRITY_VIOLATION",
				"A operação viola uma restrição de integridade dos dados.");
		return handleExceptionInternal(ex, problem, new HttpHeaders(), HttpStatus.CONFLICT, request);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<Object> handleUnexpected(Exception ex, WebRequest request) {
		log.error("Erro não tratado", ex);
		ProblemDetail problem = problem(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR",
				"Erro inesperado. Tente novamente em instantes.");
		return handleExceptionInternal(ex, problem, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request);
	}

	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
			HttpHeaders headers, HttpStatusCode status, WebRequest request) {
		List<FieldViolation> errors = new ArrayList<>();
		ex.getBindingResult().getFieldErrors()
				.forEach(error -> errors.add(new FieldViolation(error.getField(), message(error))));
		ex.getBindingResult().getGlobalErrors()
				.forEach(error -> errors.add(new FieldViolation(error.getObjectName(), error.getDefaultMessage())));

		ProblemDetail problem = problem(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", "Um ou mais campos são inválidos.");
		problem.setProperty("errors", errors);
		return handleExceptionInternal(ex, problem, headers, HttpStatus.BAD_REQUEST, request);
	}

	@Override
	protected ResponseEntity<Object> handleHandlerMethodValidationException(HandlerMethodValidationException ex,
			HttpHeaders headers, HttpStatusCode status, WebRequest request) {
		List<FieldViolation> errors = ex.getParameterValidationResults().stream()
				.flatMap(result -> result.getResolvableErrors().stream()
						.map(error -> new FieldViolation(result.getMethodParameter().getParameterName(),
								error.getDefaultMessage())))
				.toList();

		ProblemDetail problem = problem(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", "Um ou mais parâmetros são inválidos.");
		problem.setProperty("errors", errors);
		return handleExceptionInternal(ex, problem, headers, HttpStatus.BAD_REQUEST, request);
	}

	@Override
	protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers,
			HttpStatusCode statusCode, WebRequest request) {
		ProblemDetail problem = body instanceof ProblemDetail detail ? detail : ProblemDetail.forStatus(statusCode);

		if (problem.getProperties() == null || !problem.getProperties().containsKey("code")) {
			describeFrameworkError(ex, problem);
		}
		problem.setTitle(TITLES.getOrDefault(statusCode.value(), problem.getTitle()));
		problem.setProperty("timestamp", Instant.now(clock));
		if (problem.getInstance() == null && request instanceof ServletWebRequest servlet) {
			problem.setInstance(URI.create(servlet.getRequest().getRequestURI()));
		}
		return super.handleExceptionInternal(ex, problem, headers, statusCode, request);
	}

	private static void describeFrameworkError(Exception ex, ProblemDetail problem) {
		if (ex instanceof HttpMessageNotReadableException) {
			fill(problem, "MALFORMED_REQUEST", "Corpo da requisição ausente ou com JSON inválido.");
		} else if (ex instanceof HttpRequestMethodNotSupportedException notSupported) {
			fill(problem, "METHOD_NOT_ALLOWED", "O método " + notSupported.getMethod() + " não é suportado neste recurso.");
		} else if (ex instanceof NoResourceFoundException) {
			fill(problem, "RESOURCE_NOT_FOUND", "Recurso não encontrado.");
		} else if (ex instanceof TypeMismatchException mismatch) {
			fill(problem, "INVALID_PARAMETER", "Valor inválido para o parâmetro '" + mismatch.getPropertyName() + "'.");
		} else if (ex instanceof MissingServletRequestParameterException missing) {
			fill(problem, "MISSING_PARAMETER", "Parâmetro obrigatório '" + missing.getParameterName() + "' ausente.");
		} else if (ex instanceof HttpMediaTypeNotSupportedException) {
			fill(problem, "UNSUPPORTED_MEDIA_TYPE", "Tipo de conteúdo não suportado. Envie application/json.");
		} else {
			problem.setProperty("code", "REQUEST_ERROR");
		}
	}

	private static void fill(ProblemDetail problem, String code, String detail) {
		problem.setProperty("code", code);
		problem.setDetail(detail);
	}

	private static ProblemDetail problem(HttpStatus status, String code, String detail) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
		problem.setProperty("code", code);
		return problem;
	}

	private static String message(FieldError error) {
		return error.getDefaultMessage() != null ? error.getDefaultMessage() : "valor inválido";
	}

	public record FieldViolation(String field, String message) {
	}
}
