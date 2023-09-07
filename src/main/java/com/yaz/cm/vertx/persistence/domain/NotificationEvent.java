package com.yaz.cm.vertx.persistence.domain;

public enum NotificationEvent {

  APP_STARTUP,
  APP_SHUTTING_DOWN,
  NEW_RATE,
  CONFIG_EMAIL_FAILED_CHECK;


  public static final NotificationEvent[] values = values();


}
