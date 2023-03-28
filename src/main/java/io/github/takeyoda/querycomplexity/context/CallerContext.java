package io.github.takeyoda.querycomplexity.context;

import io.github.takeyoda.querycomplexity.model.Client;
import org.springframework.core.NamedThreadLocal;

public final class CallerContext {
  private static final ThreadLocal<Client> STATE = new NamedThreadLocal<>("Caller context");

  private CallerContext() {}

  public static Client getCaller() {
    return STATE.get();
  }

  public static void setCaller(Client client) {
    STATE.set(client);
  }

  public static void clear() {
    STATE.remove();
  }
}
