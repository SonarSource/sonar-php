/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource Sàrl
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

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import org.sonar.check.Rule;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonarsource.analyzer.commons.regex.RegexIssueLocation;
import org.sonarsource.analyzer.commons.regex.RegexParseResult;
import org.sonarsource.analyzer.commons.regex.ast.RegexTree;
import org.sonarsource.analyzer.commons.regex.ast.SequenceTree;

@Rule(key = "S5361")
public class StringReplaceCheck extends AbstractRegexCheck {

  private static final String MESSAGE = "Replace this \"preg_replace()\" call by a \"str_replace()\" function call.";
  private static final int LIMIT_PARAM_INDEX = 3;
  private static final String LIMIT_PARAM_NAME = "limit";

  @Override
  protected Set<String> lookedUpFunctionNames() {
    return Collections.singleton("preg_replace");
  }

  @Override
  protected void checkFunctionCall(FunctionCallTree tree) {
    if (limitParameterIsNotUsedOrSetToDefault(tree)) {
      super.checkFunctionCall(tree);
    }
  }

  @Override
  public void checkRegex(RegexParseResult regexParseResult, FunctionCallTree regexFunctionCall) {
    RegexTree regex = regexParseResult.getResult();
    if (!regexParseResult.hasSyntaxErrors() && isPlainString(regex)) {
      newIssue(regexFunctionCall.callee(), MESSAGE, null, Collections.singletonList(new RegexIssueLocation(regex, "Expression without regular expression features.")));
    }
  }

  private static boolean limitParameterIsNotUsedOrSetToDefault(FunctionCallTree tree) {
    Optional<ExpressionTree> limitParamTree = CheckUtils.argumentValue(tree, LIMIT_PARAM_NAME, LIMIT_PARAM_INDEX);
    // only expected negative numeric value is -1 which is the default value
    return limitParamTree.isEmpty() || limitParamTree.get().is(Tree.Kind.UNARY_MINUS);
  }

  private static boolean isPlainString(RegexTree regex) {
    return regex.is(RegexTree.Kind.CHARACTER)
      || (regex.is(RegexTree.Kind.SEQUENCE)
        && !((SequenceTree) regex).getItems().isEmpty()
        && ((SequenceTree) regex).getItems().stream().allMatch(item -> item.is(RegexTree.Kind.CHARACTER)));
  }
}
