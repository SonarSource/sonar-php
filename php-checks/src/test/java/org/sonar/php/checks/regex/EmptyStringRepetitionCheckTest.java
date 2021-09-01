package org.sonar.php.checks.regex;

import org.junit.Test;
import org.sonar.plugins.php.CheckVerifier;


public class EmptyStringRepetitionCheckTest {

  @Test
  public void test() throws Exception {
    CheckVerifier.verify(new EmptyStringRepetitionCheck(), "regex/EmptyStringRepetitionCheck.php");
  }

}
