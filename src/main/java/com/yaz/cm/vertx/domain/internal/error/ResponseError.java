package com.yaz.cm.vertx.domain.internal.error;

public interface ResponseError {

  int httpCode();

  String internalMsg();

  String responseMsg();

  static ResponseError forbidden(String msg) {
    return forbidden(msg, msg);
  }

  static ResponseError forbidden(String internalMsg, String responseMsg) {
    return new ResponseErrorImpl(403, internalMsg, responseMsg);
  }
}
