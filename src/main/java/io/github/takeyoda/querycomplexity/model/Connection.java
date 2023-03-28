package io.github.takeyoda.querycomplexity.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import graphql.relay.ConnectionCursor;
import graphql.relay.DefaultConnectionCursor;
import graphql.relay.DefaultEdge;
import graphql.relay.DefaultPageInfo;
import graphql.relay.Edge;
import graphql.relay.PageInfo;
import java.util.List;
import lombok.Getter;
import lombok.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@Value
public class Connection<T> implements graphql.relay.Connection<T> {
  @Getter(onMethod = @__({@Override}))
  ImmutableList<Edge<T>> edges;

  @Getter(onMethod = @__({@Override}))
  PageInfo pageInfo;

  long totalCount;

  public static <E extends Node> Connection<E> create(
      List<E> items, Pageable pageable, long totalCount) {
    Page<E> page = new PageImpl<>(items, pageable, totalCount);
    return create(page);
  }

  public static <E extends Node> Connection<E> create(Page<E> page) {
    return create(page, Node::getId);
  }

  public static <E> Connection<E> create(Page<E> page, CursorFunction<E> cursorFunction) {
    ImmutableList<Edge<E>> edges =
        page.stream()
            .map(
                it -> {
                  ConnectionCursor cursor =
                      new DefaultConnectionCursor(cursorFunction.getCursor(it));
                  return new DefaultEdge<>(it, cursor);
                })
            .collect(ImmutableList.toImmutableList());
    ConnectionCursor startCursor = edges.stream().findFirst().map(Edge::getCursor).orElse(null);
    ConnectionCursor endCursor = edges.isEmpty() ? null : Iterables.getLast(edges).getCursor();
    PageInfo pageInfo =
        new DefaultPageInfo(startCursor, endCursor, page.hasPrevious(), page.hasNext());
    return new Connection<>(edges, pageInfo, page.getTotalElements());
  }

  public interface CursorFunction<T> {
    String getCursor(T item);
  }
}
