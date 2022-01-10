/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2022 SonarSource SA
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
package org.sonar.php.checks.regex;

import java.util.Collections;
import java.util.Set;
import org.sonar.check.Rule;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonarsource.analyzer.commons.regex.RegexIssueLocation;
import org.sonarsource.analyzer.commons.regex.RegexParseResult;
import org.sonarsource.analyzer.commons.regex.ast.RegexTree;
import org.sonarsource.analyzer.commons.regex.ast.SequenceTree;

@Rule(key = "S5361")
public class StringReplaceCheck extends AbstractRegexCheck {

  private static final String MESSAGE = "Replace this \"preg_replace()\" call by a \"str_replace()\" function call.";

  @Override
  protected Set<String> lookedUpFunctionNames() {
    return Collections.singleton("preg_replace");
  }

  @Override
  public void checkRegex(RegexParseResult regexParseResult, FunctionCallTree regexFunctionCall) {
    RegexTree regex = regexParseResult.getResult();
    if (!regexParseResult.hasSyntaxErrors() && isPlainString(regex)) {
      newIssue(regexFunctionCall.callee(), MESSAGE, null, Collections.singletonList(new RegexIssueLocation(regex, "Expression without regular expression features.")));
    }
  }

  private static boolean isPlainString(RegexTree regex) {
    return regex.is(RegexTree.Kind.CHARACTER)
      || (regex.is(RegexTree.Kind.SEQUENCE)
        && !((SequenceTree) regex).getItems().isEmpty()
        && ((SequenceTree) regex).getItems().stream().allMatch(item -> item.is(RegexTree.Kind.CHARACTER)));
  }
}
