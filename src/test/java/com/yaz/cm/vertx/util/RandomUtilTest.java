package com.yaz.cm.vertx.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class RandomUtilTest {

  @Test
  public void randomStr() {
    final var size = 20;
    final var randomStr = RandomUtil.randomStr(size);
    System.out.println(randomStr);
    Assertions.assertNotNull(randomStr);
    assert randomStr.length() == size;
  }
}