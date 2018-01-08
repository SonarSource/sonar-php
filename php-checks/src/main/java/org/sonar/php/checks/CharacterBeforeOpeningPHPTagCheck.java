/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2018 SonarSource SA
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
package org.sonar.php.checks;

import java.util.regex.Pattern;
import org.sonar.check.Rule;
import org.sonar.php.parser.LexicalConstant;
import org.sonar.plugins.php.api.tree.ScriptTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

@Rule(key = "S2000")
public class CharacterBeforeOpeningPHPTagCheck extends PHPVisitorCheck {

  public static final String KEY = "S2000";
  private static final String MESSAGE = "Remove the extra characters before the open tag.";

  private static final Pattern OPENING_TAG = Pattern.compile(LexicalConstant.PHP_OPENING_TAG);

  @Override
  public void visitScript(ScriptTree tree) {
    SyntaxToken openingTagToken = tree.fileOpeningTagToken();
    if (openingTagToken.column() != 0 || openingTagToken.line() != 1 || !OPENING_TAG.matcher(openingTagToken.text()).matches()) {
      context().newIssue(this, openingTagToken, MESSAGE);
    }
  }
}
