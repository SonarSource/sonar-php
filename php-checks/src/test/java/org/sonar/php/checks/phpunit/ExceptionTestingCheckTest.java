package org.sonar.php.checks.phpunit;

import org.junit.Test;
import org.sonar.plugins.php.CheckVerifier;

public class ExceptionTestingCheckTest {
  @Test
  public void test() {
    CheckVerifier.verify(new ExceptionTestingCheck(), "phpunit/ExceptionTestingCheck.php");
  }
}
