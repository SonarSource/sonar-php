package org.sonar.php.checks.regex;

import junit.framework.TestCase;
import org.sonar.plugins.php.CheckVerifier;

public class SingleCharacterAlternationCheckTest extends TestCase {

  public void test() throws Exception {
    CheckVerifier.verify(new SingleCharacterAlternationCheck(), "regex/SingleCharacterAlternationCheck.php");
  }
}
