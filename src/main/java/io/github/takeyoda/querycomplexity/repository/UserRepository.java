package io.github.takeyoda.querycomplexity.repository;

import io.github.takeyoda.querycomplexity.model.Favorite;
import io.github.takeyoda.querycomplexity.model.User;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class UserRepository {

  private final List<User> users;

  public UserRepository() {
    users =
        List.of(
            User.builder().id("u1").name("taro").build(),
            User.builder().id("u2").name("jiro").build(),
            User.builder().id("u3").name("saburo").build());
  }

  public Mono<Page<User>> findByCursor(int first, String afterCursor) {
    return Mono.just(new PageImpl<>(users));
  }

  public Flux<Favorite> findFavoriteByUserId(String userId) {
    return Flux.fromIterable(
        List.of(
            Favorite.builder().name(userId + "-fav1").description("fav1 desc").build(),
            Favorite.builder().name(userId + "-fav2").description("fav2 desc").build()));
  }
}
