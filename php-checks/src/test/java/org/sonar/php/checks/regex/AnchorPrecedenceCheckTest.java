package org.sonar.php.checks.regex;

import junit.framework.TestCase;
import org.junit.Test;
import org.sonar.plugins.php.CheckVerifier;

public class AnchorPrecedenceCheckTest extends TestCase {

  @Test
  public void test() throws Exception {
    CheckVerifier.verify(new AnchorPrecedenceCheck(), "regex/AnchorPrecedenceCheck.php");
  }
}
