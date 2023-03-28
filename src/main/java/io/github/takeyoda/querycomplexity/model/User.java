package io.github.takeyoda.querycomplexity.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Value;

@Value
@Builder
public class User implements Node {
  @Getter(onMethod = @__({@Override}))
  String id;

  String name;
}
