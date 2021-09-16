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
package org.sonar.php.checks.regex;

import org.sonar.check.Rule;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonarsource.analyzer.commons.regex.RegexParseResult;
import org.sonarsource.analyzer.commons.regex.ast.AutomatonState;
import org.sonarsource.analyzer.commons.regex.ast.Quantifier;
import org.sonarsource.analyzer.commons.regex.ast.RegexBaseVisitor;
import org.sonarsource.analyzer.commons.regex.ast.RepetitionTree;
import org.sonarsource.analyzer.commons.regex.ast.StartState;
import org.sonarsource.analyzer.commons.regex.helpers.RegexTreeHelper;

@Rule(key = "S6019")
public class ReluctantQuantifierWithEmptyContinuationCheck extends AbstractRegexCheck {

  private static final String MESSAGE_FIX = "Fix this reluctant quantifier that will only ever match %s repetition%s.";
  private static final String MESSAGE_UNNECESSARY = "Remove the '?' from this unnecessarily reluctant quantifier.";

  @Override
  public void checkRegex(RegexParseResult regexParseResult, FunctionCallTree regexFunctionCall) {
    new ReluctantQuantifierWithEmptyContinuationFinder().visit(regexParseResult);
  }

  private class ReluctantQuantifierWithEmptyContinuationFinder extends RegexBaseVisitor {
    private AutomatonState endState;

    @Override
    protected void before(RegexParseResult regexParseResult) {
      endState = regexParseResult.getFinalState();
    }

    @Override
    public void visitRepetition(RepetitionTree tree) {
      super.visitRepetition(tree);
      if (tree.getQuantifier().getModifier() == Quantifier.Modifier.RELUCTANT) {
        if (RegexTreeHelper.isAnchoredAtEnd(tree.continuation())) {
          if (RegexTreeHelper.onlyMatchesEmptySuffix(tree.continuation())) {
            newIssue(tree, MESSAGE_UNNECESSARY);
          }
        } else if (ImpossibleBoundariesCheck.canReachWithoutConsumingInput(new StartState(tree.continuation(), tree.activeFlags()), endState)) {
          int minimumRepetitions = tree.getQuantifier().getMinimumRepetitions();
          newIssue(tree, String.format(MESSAGE_FIX, minimumRepetitions, minimumRepetitions == 1 ? "" : "s"));
        }
      }
    }
  }
}
