package com.yaz.cm.vertx.service.telegram;

import javax.inject.Inject;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class TelegramSendEntityBackups {

 /* public static final String CALLBACK_KEY = "/backup_";

  private static final String ALL_KEY = "ALL";

  private final TelegramRestApi restApi;
  private final Map<String, EntityDownloader> downloaderMap;
  private final DeleteDirAfterDelay deleteDirAfterDelay;


  public Completable sendAvailableBackups(long chatId) {
    final List<List<InlineKeyboardButton>> list = new LinkedList<>();
    downloaderMap.keySet()
        .stream()
        .sorted()
        .map(key -> InlineKeyboardButton.callbackData(key, CALLBACK_KEY + key))
        .map(List::of)
        .forEach(list::add);
    list.add(List.of(InlineKeyboardButton.callbackData("ALL", CALLBACK_KEY + ALL_KEY)));

    final var keyboardMarkup = InlineKeyboardMarkup.builder()
        .inlineKeyboard(list)
        .build();

    final var sendMessage = SendMessage.builder()
        .chatId(chatId)
        .text("Escoge una opcion")
        .replyMarkup(keyboardMarkup)
        .build();

    return restApi.sendMessage(sendMessage)
        .ignoreElement();
  }

  public Single<Pair<String, String>> allGz() {
    return Observable.fromIterable(downloaderMap.values())
        .map(EntityDownloader::download)
        .toList()
        .toFlowable()
        .flatMap(Single::merge)
        .toList()
        .map(list -> {

          final var set = list.stream().map(FileResponse::path).collect(Collectors.toSet());
          final var tempFile = "tmp/" + System.currentTimeMillis() + "/";
          Files.createDirectories(Paths.get(tempFile));
          final var fileName = "all.tar.gz";
          final var filePath = tempFile + fileName;
          ZipUtility.createTarGzipFiles(filePath, set);

          return Pair.of(fileName, filePath);
        });
  }

  public Completable resolve(long chatId, String text) {

    if (text.equals(ALL_KEY)) {

      return allGz()
          .flatMap(pair -> restApi.sendDocument(chatId, "all", pair.getFirst(), pair.getSecond(), "application/gzip")
              .doAfterTerminate(deleteDirAfterDelay::deleteTmp))
          .ignoreElement();

    }

    final var downloader = downloaderMap.get(text);
    if (downloader == null) {
      return restApi.sendMessage(chatId, "DOWNLOADER NOT FOUND FOR " + text)
          .ignoreElement();
    }

    return downloader.download()
        .flatMap(fileResponse -> restApi.sendDocument(chatId, fileResponse.fileName(), fileResponse)
            .doAfterTerminate(() -> deleteDirAfterDelay.deleteDirNow(fileResponse.path())))
        .ignoreElement();
  }*/
}
