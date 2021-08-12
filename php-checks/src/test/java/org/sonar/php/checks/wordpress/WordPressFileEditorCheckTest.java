package org.sonar.php.checks.wordpress;

import java.util.Collections;
import org.junit.Test;
import org.sonar.plugins.php.CheckVerifier;
import org.sonar.plugins.php.TestUtils;
import org.sonar.plugins.php.api.tests.PHPCheckTest;
import org.sonar.plugins.php.api.visitors.FileIssue;

public class WordPressFileEditorCheckTest {

  @Test
  public void edit_false() {
    CheckVerifier.verify(new WordPressFileEditorCheck(), "wordpress/WordPressFileEditorCheck/editFalse/wp-config.php");
  }

  @Test
  public void edit_false_mods_true() {
    CheckVerifier.verifyNoIssue(new WordPressFileEditorCheck(), "wordpress/WordPressFileEditorCheck/editFalseModsTrue/wp-config.php");
  }

  @Test
  public void edit_false_mods_false() {
    CheckVerifier.verify(new WordPressFileEditorCheck(), "wordpress/WordPressFileEditorCheck/editFalseModsFalse/wp-config.php");
  }

  @Test
  public void edit_not_set_leads_to_file_issue() {
    WordPressFileEditorCheck check = new WordPressFileEditorCheck();
    PHPCheckTest.check(check, TestUtils.getCheckFile("wordpress/WordPressFileEditorCheck/notSet/wp-config.php"),
      Collections.singletonList(new FileIssue(check, "Plugin and theme files editor is activated")));
  }
}
