package com.yaz.cm.vertx.service;

import com.yaz.cm.vertx.persistence.domain.NotificationEvent;
import com.yaz.cm.vertx.service.telegram.TelegramRestApi;
import com.yaz.cm.vertx.util.EnvUtil;
import io.reactivex.rxjava3.core.Completable;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import javax.inject.Inject;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class NotificationService {


  private final TelegramRestApi restApi;
  //private final SendLogs sendLogs;
  //private final TelegramChatService chatService;
  private final TranslationProvider translationProvider;

  public boolean sendAppStartup() {
    final var event = NotificationEvent.APP_STARTUP;
    final var msg = translationProvider.translate(event.name());
    return blocking(send(EnvUtil.addEnvInfo(msg, false), event));
  }

  private Completable sendNotification(Set<NotificationEvent> set, Function<Long, Completable> function) {
    return Completable.complete();
    /*return chatService.chatsByEvents(set)
        .filter(s -> !s.isEmpty())
        .flatMapObservable(Observable::fromIterable)
        .map(function::apply)
        .toList()
        .toFlowable()
        .flatMapCompletable(Completable::merge);*/
  }

  public Completable sendNewRate(String msg) {
    return send(EnvUtil.addEnvInfo(msg), NotificationEvent.NEW_RATE);
  }

  public Completable send(String msg, NotificationEvent event) {
    return sendNotification(Set.of(event), chat -> restApi.sendMessage(chat, msg).ignoreElement());
  }

  private boolean blocking(Completable completable) {
    return completable
        .blockingAwait(10, TimeUnit.SECONDS);
  }

  public Completable sendLogs(long chatId, String caption) {
    //return sendLogs.sendLogs(chatId, caption);
    return Completable.complete();
  }

  public boolean sendShuttingDownApp() {
    final var event = NotificationEvent.APP_SHUTTING_DOWN;
    final var caption = translationProvider.translate(event.name());
    return blocking(sendNotification(Set.of(event), chat -> sendLogs(chat, EnvUtil.addEnvInfo(caption))));
  }
}
