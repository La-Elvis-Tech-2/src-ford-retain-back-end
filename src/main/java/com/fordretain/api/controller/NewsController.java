package com.fordretain.api.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.fordretain.api.dto.request.NewsRequest;
import com.fordretain.api.dto.response.NewsResponse;
import com.fordretain.api.model.News;
import com.fordretain.api.model.enums.NewsCategory;
import com.fordretain.api.service.NewsService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/news")
@Tag(name = "Novidades", description = "Recalls, campanhas e lançamentos da rede")
public class NewsController {

	private final NewsService service;

	public NewsController(NewsService service) {
		this.service = service;
	}

	@GetMapping
	@Operation(summary = "Lista as novidades", description = "Endpoint público, da mais recente para a mais antiga.")
	public List<NewsResponse> list(@Parameter(description = "Filtra por categoria") @RequestParam(required = false) NewsCategory category) {
		return service.list(category).stream().map(NewsResponse::from).toList();
	}

	@GetMapping("/{id}")
	@Operation(summary = "Consulta uma novidade", description = "Endpoint público.")
	public NewsResponse get(@PathVariable Long id) {
		return NewsResponse.from(service.get(id));
	}

	@PostMapping
	@PreAuthorize("hasRole('ADMIN')")
	@SecurityRequirement(name = "bearerAuth")
	@Operation(summary = "Publica uma novidade")
	@ApiResponse(responseCode = "201", description = "Novidade publicada")
	public ResponseEntity<NewsResponse> create(@Valid @RequestBody NewsRequest request) {
		News news = service.create(request);
		URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(news.getId()).toUri();
		return ResponseEntity.created(location).body(NewsResponse.from(news));
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	@SecurityRequirement(name = "bearerAuth")
	@Operation(summary = "Atualiza uma novidade")
	public NewsResponse update(@PathVariable Long id, @Valid @RequestBody NewsRequest request) {
		return NewsResponse.from(service.update(id, request));
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	@SecurityRequirement(name = "bearerAuth")
	@Operation(summary = "Remove uma novidade")
	@ApiResponse(responseCode = "204", description = "Novidade removida")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		service.delete(id);
		return ResponseEntity.noContent().build();
	}
}
