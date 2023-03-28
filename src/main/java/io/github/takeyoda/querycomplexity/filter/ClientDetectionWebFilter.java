package io.github.takeyoda.querycomplexity.filter;

import io.github.takeyoda.querycomplexity.model.Client;
import io.github.takeyoda.querycomplexity.repository.ClientRepository;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
@Order(0)
@RequiredArgsConstructor
public class ClientDetectionWebFilter implements WebFilter {
  private final ClientRepository clientRepository;

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
    final String apiKey = exchange.getRequest().getHeaders().getFirst("X-Api-Key");
    return Mono.justOrEmpty(apiKey)
        .filter(StringUtils::hasLength)
        .flatMap(clientRepository::findByApiKey)
        .flatMap(
            client ->
                chain
                    .filter(exchange)
                    .contextWrite(context -> context.putNonNull(Client.class, client)))
        .switchIfEmpty(writeErrorResponse(exchange));
  }

  private Mono<Void> writeErrorResponse(ServerWebExchange exchange) {
    final ServerHttpResponse response = exchange.getResponse();
    response.setStatusCode(HttpStatus.BAD_REQUEST);
    response.getHeaders().add(HttpHeaders.CONTENT_TYPE, "application/json; charset=UTF-8");
    String body = "{\"message\": \"No valid API-Key specified\"}";
    DataBufferFactory dbf = response.bufferFactory();
    return response.writeWith(Mono.just(dbf.wrap(body.getBytes(StandardCharsets.UTF_8))));
  }
}
