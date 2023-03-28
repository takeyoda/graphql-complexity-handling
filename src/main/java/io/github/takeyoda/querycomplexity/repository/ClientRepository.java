package io.github.takeyoda.querycomplexity.repository;

import io.github.takeyoda.querycomplexity.model.Client;
import java.util.List;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class ClientRepository {
  private final List<Client> clients;

  public ClientRepository() {
    clients =
        List.of(
            Client.builder().apiKey("key1").maxDepth(10).maxComplexity(200).build(),
            Client.builder().apiKey("key2").maxDepth(5).maxComplexity(100).build());
  }

  public Mono<Client> findByApiKey(String apiKey) {
    return Mono.justOrEmpty(clients.stream().filter(c -> c.apiKey().equals(apiKey)).findFirst());
  }
}
