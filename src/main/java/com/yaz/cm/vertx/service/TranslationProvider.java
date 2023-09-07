package com.yaz.cm.vertx.service;

import java.util.Locale;
import javax.inject.Inject;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class TranslationProvider {

  public static final String BUNDLE_PREFIX = "i18n.translate";

  public final Locale LOCALE_ES = Locale.of("es", "VE");
  public final Locale LOCALE_EN = Locale.of("en", "US");

  public String translate(String str) {
    return getTranslation(str, LOCALE_ES);
  }


  public String getTranslation(String key, Locale locale, Object... params) {
    return key;
  }
}
