package com.yaz.cm.vertx.domain.internal.error;


public record ResponseErrorImpl(int httpCode, String internalMsg, String responseMsg) implements ResponseError {



}
