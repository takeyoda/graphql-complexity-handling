package io.github.takeyoda.querycomplexity.context;

import io.github.takeyoda.querycomplexity.model.Client;
import lombok.RequiredArgsConstructor;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.util.context.Context;

// https://github.com/archie-swif/webflux-mdc/blob/master/src/main/java/com/example/webfluxmdc/MdcContextLifterConfiguration.java
@RequiredArgsConstructor
public class CallerContextLifter<T> implements CoreSubscriber<T> {

  private final CoreSubscriber<? super T> wrapped;

  @Override
  public Context currentContext() {
    return wrapped.currentContext();
  }

  private Client getClient() {
    Context context = currentContext();
    if (context.isEmpty() || !context.hasKey(Client.class)) {
      return null;
    }
    return context.get(Client.class);
  }

  @Override
  public void onSubscribe(Subscription subscription) {
    Client client = getClient();
    CallerContext.setCaller(client);
    try {
      wrapped.onSubscribe(subscription);
    } finally {
      CallerContext.clear();
    }
  }

  @Override
  public void onNext(T t) {
    Client client = getClient();
    CallerContext.setCaller(client);
    try {
      wrapped.onNext(t);
    } finally {
      CallerContext.clear();
    }
  }

  @Override
  public void onError(Throwable t) {
    Client client = getClient();
    CallerContext.setCaller(client);
    try {
      wrapped.onError(t);
    } finally {
      CallerContext.clear();
    }
  }

  @Override
  public void onComplete() {
    Client client = getClient();
    CallerContext.setCaller(client);
    try {
      wrapped.onComplete();
    } finally {
      CallerContext.clear();
    }
  }
}
