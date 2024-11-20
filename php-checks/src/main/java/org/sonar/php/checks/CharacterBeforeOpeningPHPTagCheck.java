/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2024 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
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
