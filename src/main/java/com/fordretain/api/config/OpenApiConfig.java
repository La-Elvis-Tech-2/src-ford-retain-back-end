package com.fordretain.api.config;

import java.lang.annotation.Annotation;
import java.util.Map;

import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springdoc.core.utils.SpringDocUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.method.HandlerMethod;

import com.fordretain.api.dto.response.ProblemResponse;
import com.fordretain.api.security.AuthenticatedUser;

import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;

/**
 * Documentação OpenAPI servida em {@code /v3/api-docs} e no Swagger UI.
 *
 * As respostas de erro comuns são acrescentadas a partir da assinatura de cada
 * endpoint, para a documentação não depender de anotação repetida:
 * corpo validado gera 400, id na rota gera 404, token exigido gera 401 e
 * perfil exigido gera 403. Toda resposta 4xx/5xx aponta para o schema
 * {@code Problem}.
 */
@Configuration
@OpenAPIDefinition(info = @Info(
		title = "Ford Retain API",
		version = "1.0.0",
		description = "API do Ford Retain: saúde do veículo, revisão recomendada e agendamento na rede Ford. "
				+ "Autentique em POST /auth/login e use o token no botão Authorize."))
@SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT")
public class OpenApiConfig {

	private static final String PROBLEM_SCHEMA = "Problem";
	private static final String PROBLEM_MEDIA_TYPE = "application/problem+json";

	static {
		// O usuário autenticado vem do token, não de parâmetro da requisição.
		SpringDocUtils.getConfig().addRequestWrapperToIgnore(AuthenticatedUser.class);
	}

	@Bean
	OpenApiCustomizer problemSchemaCustomizer() {
		return openApi -> {
			Map<String, Schema> schemas = ModelConverters.getInstance().read(ProblemResponse.class);
			schemas.forEach((name, schema) -> openApi.getComponents().addSchemas(name, schema));
		};
	}

	@Bean
	OperationCustomizer standardErrorResponses() {
		return (operation, handler) -> {
			ApiResponses responses = operation.getResponses();

			if (hasParameter(handler, RequestBody.class)) {
				responses.putIfAbsent("400", problem("Dados inválidos"));
			}
			if (hasParameter(handler, PathVariable.class)) {
				responses.putIfAbsent("404", problem("Recurso não encontrado"));
			}
			if (isProtected(handler)) {
				responses.putIfAbsent("401", problem("Token ausente, inválido ou expirado"));
			}
			if (isRestricted(handler)) {
				responses.putIfAbsent("403", problem("Perfil sem permissão para a operação"));
			}
			responses.forEach((code, response) -> {
				if (code.startsWith("4") || code.startsWith("5")) {
					response.setContent(problemContent());
				}
			});
			return operation;
		};
	}

	private static boolean hasParameter(HandlerMethod handler, Class<? extends Annotation> type) {
		for (var parameter : handler.getMethodParameters()) {
			if (parameter.hasParameterAnnotation(type)) {
				return true;
			}
		}
		return false;
	}

	private static boolean isProtected(HandlerMethod handler) {
		return handler.hasMethodAnnotation(SecurityRequirement.class)
				|| AnnotatedElementUtils.hasAnnotation(handler.getBeanType(), SecurityRequirement.class);
	}

	private static boolean isRestricted(HandlerMethod handler) {
		return handler.hasMethodAnnotation(PreAuthorize.class)
				|| AnnotatedElementUtils.hasAnnotation(handler.getBeanType(), PreAuthorize.class);
	}

	private static ApiResponse problem(String description) {
		return new ApiResponse().description(description).content(problemContent());
	}

	private static Content problemContent() {
		Schema<?> reference = new Schema<>().$ref("#/components/schemas/" + PROBLEM_SCHEMA);
		return new Content().addMediaType(PROBLEM_MEDIA_TYPE, new MediaType().schema(reference));
	}
}
