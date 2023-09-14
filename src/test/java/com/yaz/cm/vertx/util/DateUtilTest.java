package com.yaz.cm.vertx.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class DateUtilTest {


  @Test
  void isValidLocalDate() {
    final var validDate = "2020-01-01";
    final var invalidDate = "2020-01-32";

    assertTrue(DateUtil.isValidLocalDate(validDate));
    assertFalse(DateUtil.isValidLocalDate(invalidDate));
  }
}