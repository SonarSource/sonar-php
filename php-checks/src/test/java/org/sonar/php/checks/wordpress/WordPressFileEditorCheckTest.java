/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2021 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
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
