package com.yaz.cm.vertx.service.telegram;

import com.fasterxml.jackson.databind.JsonNode;
import com.yaz.cm.vertx.domain.Currency;
import com.yaz.cm.vertx.service.RateService;
import com.yaz.cm.vertx.service.telegram.domain.Chat;
import com.yaz.cm.vertx.service.telegram.domain.TelegramUpdate;
import com.yaz.cm.vertx.service.telegram.domain.TelegramUser;
import com.yaz.cm.vertx.util.DateUtil;
import com.yaz.cm.vertx.util.SystemUtil;
import dagger.Component;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.jackson.DatabindCodec;
import java.util.stream.Collectors;
import javax.inject.Inject;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class TelegramCommandResolver {

  /*private final TelegramChatService chatService;
  private final UserService userService;
  private final SendLogs sendLogs;
  private final TelegramRestApi telegramRestApi;
  private final RateService rateService;
  private final TelegramChatLinkHandler linkHandler;
  private final TelegramSendEntityBackups sendEntityBackups;
  */


  public Completable resolve(JsonNode json) {
    return null;
  }
   /*
    return Completable.defer(() -> {
      final var update = DatabindCodec.mapper().treeToValue(json, TelegramUpdate.class);

      final var message = update.message();

      if (message != null) {
        final var text = message.text();
        final var from = message.from();
        final var chat = message.chat();

        if (text != null && from != null) {
          final var chatId = from.id();

          if (text.startsWith("/start") && text.length() > 8 && !from.isBot() && chat != null) {
            final var userId = text.substring(7).trim();

            return addAccount(userId, from, chat);
          }

          if (text.startsWith("/log")) {
            return sendLogs.sendLogs(chatId, "logs");
          }

          if (text.startsWith("/system_info")) {
            return telegramRestApi.sendMessage(chatId, SystemUtil.systemInfo().collect(Collectors.joining("\n")))
                .ignoreElement();
          }

          if (text.startsWith("/tasa")) {

            return rateService.last(Currency.USD, Currency.VED)
                .toSingle()
                .map(rate -> "TASA:%s\nFECHA: %s\nCREADO: %s\nID: %s".formatted(rate.rate(), rate.dateOfRate(),
                    DateUtil.formatVe(rate.createdAt()), rate.id()))
                .flatMap(msg -> telegramRestApi.sendMessage(chatId, msg))
                .ignoreElement();
          }

          if (text.startsWith("/backups")) {
            return sendEntityBackups.sendAvailableBackups(chatId);
          }
        }
      }

      final var callbackQuery = update.callbackQuery();

      if (callbackQuery != null) {
        final var from = callbackQuery.from();
        if (callbackQuery.data().startsWith(TelegramSendEntityBackups.CALLBACK_KEY)) {
          return sendEntityBackups.resolve(from.id(),
              callbackQuery.data().replace(TelegramSendEntityBackups.CALLBACK_KEY, "").trim());
        }

      }

      return Completable.complete();
    });
  }

  private Completable addAccount(String userId, TelegramUser from, Chat chatToSave) {
    final var userMaybe = userService.find(userId)
        .cache();

    final var chatId = from.id();

    final var chatMaybe = chatService.find(userId, chatId)
        .cache();

    return Single.zip(userMaybe, chatMaybe, (user, chat) -> {

      if (user.isEmpty()) {

        return telegramRestApi.sendMessage(chatId, "Usuario no encontrado")
            .ignoreElement();
      }

      if (chat.isPresent()) {
        return telegramRestApi.sendMessage(chatId, "Cuenta ya enlazada")
            .ignoreElement();
      }

      final var telegramChat = TelegramChat.builder()
          .id(new TelegramChatId(chatId, userId))
          .chatId(chatId)
          .firstName(from.firstName())
          .lastName(from.lastName())
          .username(from.username())
          .user(user.get())
          .update(new JsonObject()
              .put("from", new JsonObject(Json.encode(from)))
              .put("chat", new JsonObject(Json.encode(chatToSave)))
              .encode())
          .createdAt(DateUtil.nowZonedWithUTC())
          .build();

      return chatService.save(telegramChat)
          .ignoreElement()
          .doOnComplete(linkHandler::fire)
          .andThen(telegramRestApi.sendMessage(chatId, "Chat guardado"))
          .ignoreElement();
    }).flatMapCompletable(c -> c);


  }*/
}
