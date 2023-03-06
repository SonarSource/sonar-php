/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2023 SonarSource SA
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

import java.io.IOException;
import org.junit.Test;
import org.sonar.plugins.php.api.visitors.PHPCheck;

public class WordPressFileEditorCheckTest extends WordPressConfigCheckTest {

  PHPCheck check = new WordPressFileEditorCheck();

  @Test
  public void edit_false() throws IOException {
    wordPressVerifier.verify(check, "wordpress/WordPressFileEditorCheck/editFalse.php");
  }

  @Test
  public void edit_false_mods_true() throws IOException {
    wordPressVerifier.verifyNoIssue(check, "wordpress/WordPressFileEditorCheck/editFalseModsTrue.php");
  }

  @Test
  public void edit_false_mods_false() throws IOException {
    wordPressVerifier.verify(check, "wordpress/WordPressFileEditorCheck/editFalseModsFalse.php");
  }

  @Test
  public void edit_not_set_leads_to_file_issue() throws IOException {
    wordPressVerifier.verifyAbsence(check, "Plugin and theme files editor is active");
  }
}
