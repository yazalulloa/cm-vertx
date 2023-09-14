package com.yaz.cm.vertx.util;

import java.time.LocalDateTime;
import java.time.Month;
import java.time.MonthDay;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import org.apache.commons.lang3.StringUtils;

public class DateUtil {

  public static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  public static final ZoneId VE_ZONE = ZoneId.of("America/Caracas");

  private DateUtil() {
    super();
  }

  public static ZonedDateTime nowZonedWithUTC() {
    return ZonedDateTime.now(ZoneOffset.UTC);
  }

  public static LocalDateTime utcLocalDateTime() {
    return LocalDateTime.now(ZoneOffset.UTC);
  }

  public static String format(TemporalAccessor temporalAccessor) {
    return DATE_FORMAT.format(temporalAccessor);
  }

  public static String formatVe(ZonedDateTime zonedDateTime) {
    return format(zonedDateTime.withZoneSameInstant(VE_ZONE));
  }

  public static boolean isValidLocalDate(String str) {
    if (str == null) {
      return false;
    }

    if (str.length() != 10) {
      return false;
    }

    final var split = str.split("-");
    if (split.length != 3) {
      return false;
    }

    final var isNumeric = StringUtils.isNumeric(split[0])
        && StringUtils.isNumeric(split[1])
        && StringUtils.isNumeric(split[2]);

    if (!isNumeric) {
      return false;
    }

    final var year = Integer.parseInt(split[0]);
    final var monthInt = Integer.parseInt(split[1]);
    final var day = Integer.parseInt(split[2]);

    final var isValidYear = ChronoField.YEAR.range().isValidValue(year);
    final var isValidMonth = ChronoField.MONTH_OF_YEAR.range().isValidValue(monthInt);
    final var isValidDay = ChronoField.DAY_OF_MONTH.range().isValidValue(day);
    final var isValidDayMonth = MonthDay.of(monthInt, 1).range(ChronoField.DAY_OF_MONTH).isValidValue(day);

    return isValidYear && isValidMonth && isValidDay && isValidDayMonth;
  }


}
