package org.sonar.php.checks.utils;

public class RegexUtils {
  public static String oneOrMore(String ...s) {
    return "(?:" + String.join("", s) + ")++";
  }

  public static String zeroOrMore(String ...s) {
    return "(?:" + String.join("", s) + ")*+";
  }

  public static String optional(String ...s) {
    return "(?:" + String.join("", s) + ")?+";
  }

  public static String firstOf(String ...s) {
    return "(?:(?:" + String.join(")|(?:", s) + "))";
  }
}
