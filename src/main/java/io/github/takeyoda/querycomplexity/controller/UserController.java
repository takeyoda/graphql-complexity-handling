package io.github.takeyoda.querycomplexity.controller;

import io.github.takeyoda.querycomplexity.model.Connection;
import io.github.takeyoda.querycomplexity.model.Favorite;
import io.github.takeyoda.querycomplexity.model.User;
import io.github.takeyoda.querycomplexity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Controller
@RequiredArgsConstructor
public class UserController {
  private final UserRepository userRepository;

  @QueryMapping
  public Mono<Connection<User>> users(@Argument Integer first, @Argument String after) {
    return userRepository.findByCursor(first, after).map(Connection::create);
  }

  @SchemaMapping(typeName = "User", field = "favorites")
  public Flux<Favorite> favorites(User user) {
    return userRepository.findFavoriteByUserId(user.getId());
  }
}
