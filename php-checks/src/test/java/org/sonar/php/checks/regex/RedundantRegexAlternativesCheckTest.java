package org.sonar.php.checks.regex;

import junit.framework.TestCase;
import org.junit.Test;
import org.sonar.plugins.php.CheckVerifier;

public class RedundantRegexAlternativesCheckTest extends TestCase {

  @Test
  public void test() throws Exception {
    CheckVerifier.verify(new RedundantRegexAlternativesCheck(), "regex/RedundantRegexAlternativesCheck.php");
  }
}
