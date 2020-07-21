package org.sonar.php.checks;

import junit.framework.TestCase;
import org.junit.Test;
import org.sonar.plugins.php.CheckVerifier;

public class CatchThrowableCheckTest extends TestCase {
  @Test
  public void test() throws Exception {
    CheckVerifier.verify(new CatchThrowableCheck(), "CatchThrowableCheck/CatchThrowableCheck.php");
    CheckVerifier.verify(new CatchThrowableCheck(), "CatchThrowableCheck/A.php", "CatchThrowableCheck/B.php");
  }
}
