package org.sonar.php.checks.wordpress;

import java.io.IOException;
import org.junit.Test;

public class WordPressExternalRequestsCheckTest extends WordPressConfigCheckTest {

  private final WordPressExternalRequestsCheck check = new WordPressExternalRequestsCheck();

  @Test
  public void test() throws IOException {
    wordPressVerifier.verify(check, "wordpress/WordPressExternalRequestsCheck/test.php");
  }

  @Test
  public void test_absence() throws IOException {
    wordPressVerifier.verifyAbsence(check, "Make sure allowing external requests is intended.");
  }
}
