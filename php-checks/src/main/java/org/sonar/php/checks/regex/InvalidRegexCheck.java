/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource SA
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
package org.sonar.php.checks.regex;

import java.util.List;
import org.sonar.check.Rule;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonarsource.analyzer.commons.regex.RegexIssueLocation;
import org.sonarsource.analyzer.commons.regex.RegexParseResult;
import org.sonarsource.analyzer.commons.regex.SyntaxError;
import org.sonarsource.analyzer.commons.regex.ast.RegexSyntaxElement;

@Rule(key = "S5856")
public class InvalidRegexCheck extends AbstractRegexCheck {

  private static final String MESSAGE_FORMAT = "Fix the syntax error%s inside this regex.";

  @Override
  public void checkRegex(RegexParseResult regexParseResult, FunctionCallTree regexFunctionCall) {
    List<SyntaxError> syntaxErrors = regexParseResult.getSyntaxErrors();
    if (!syntaxErrors.isEmpty()) {
      reportSyntaxErrors(syntaxErrors);
    }
  }

  private void reportSyntaxErrors(List<SyntaxError> syntaxErrors) {
    // report on the first issue
    RegexSyntaxElement tree = syntaxErrors.get(0).getOffendingSyntaxElement();
    List<RegexIssueLocation> secondaries = syntaxErrors.stream()
      .map(error -> new RegexIssueLocation(error.getOffendingSyntaxElement(), error.getMessage()))
      .toList();

    String msg = String.format(MESSAGE_FORMAT, secondaries.size() > 1 ? "s" : "");
    newIssue(tree, msg, null, secondaries);
  }
}
