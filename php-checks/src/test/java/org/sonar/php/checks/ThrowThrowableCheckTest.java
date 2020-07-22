package org.sonar.php.checks;

import junit.framework.TestCase;
import org.sonar.plugins.php.CheckVerifier;

public class ThrowThrowableCheckTest extends TestCase {
  public void test() throws Exception {
    CheckVerifier.verify(new ThrowThrowableCheck(), "ThrowThrowableCheck.php");
  }
}
