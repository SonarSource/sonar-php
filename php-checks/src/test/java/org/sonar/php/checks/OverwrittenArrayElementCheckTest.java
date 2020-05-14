package org.sonar.php.checks;

import org.junit.Test;
import org.sonar.plugins.php.CheckVerifier;

public class OverwrittenArrayElementCheckTest {

  @Test
  public void test() throws Exception {
    CheckVerifier.verify(new OverwrittenArrayElementCheck(), "OverwrittenArrayElementCheck.php");
  }
}
