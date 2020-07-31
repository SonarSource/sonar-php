package org.sonar.php.checks.phpunit;

import org.junit.Test;
import org.sonar.plugins.php.CheckVerifier;

public class AssertionInTryCatchCheckTest {
  @Test
  public void test() {
    CheckVerifier.verify(new AssertionInTryCatchCheck(), "phpunit/AssertionInTryCatchCheck.php");
  }
}
