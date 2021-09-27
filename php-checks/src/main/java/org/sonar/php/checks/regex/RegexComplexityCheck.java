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

import java.util.ArrayList;
import java.util.List;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonarsource.analyzer.commons.regex.RegexParseResult;
import org.sonarsource.analyzer.commons.regex.ast.BackReferenceTree;
import org.sonarsource.analyzer.commons.regex.ast.CharacterClassTree;
import org.sonarsource.analyzer.commons.regex.ast.DisjunctionTree;
import org.sonarsource.analyzer.commons.regex.ast.LookAroundTree;
import org.sonarsource.analyzer.commons.regex.ast.NonCapturingGroupTree;
import org.sonarsource.analyzer.commons.regex.ast.RegexBaseVisitor;
import org.sonarsource.analyzer.commons.regex.ast.RegexSyntaxElement;
import org.sonarsource.analyzer.commons.regex.ast.RepetitionTree;
import org.sonarsource.analyzer.commons.regex.ast.SourceCharacter;

@Rule(key = "S5843")
public class RegexComplexityCheck extends AbstractRegexCheck {

  private static final String MESSAGE = "Simplify this regular expression to reduce its complexity from %d to the %d allowed.";

  private static final int DEFAULT_MAX = 20;

  @RuleProperty(
    key = "maxComplexity",
    description = "The maximum authorized complexity.",
    defaultValue = "" + DEFAULT_MAX)
  public int max = DEFAULT_MAX;

  @Override
  public void checkRegex(RegexParseResult regex, FunctionCallTree methodInvocation) {
    ComplexityCalculator complexityCalculator = new ComplexityCalculator();
    complexityCalculator.visit(regex);
    int complexity = complexityCalculator.complexity;
    if (complexity > max ) {
      double cost = (double) complexity - max;
      newIssue(regex.getResult(), String.format(MESSAGE, complexity, max), complexityCalculator.components, cost);
    }
  }

  private static class ComplexityCalculator extends RegexBaseVisitor {

    int complexity = 0;

    int nesting = 1;

    List<RegexIssueLocation> components = new ArrayList<>();

    private void increaseComplexity(RegexSyntaxElement syntaxElement, int increment) {
      complexity += increment;
      String message = "+" + increment;
      if (increment > 1) {
        message += " (incl " + (increment - 1) + " for nesting)";
      }
      components.add(new RegexIssueLocation(syntaxElement, message));
    }

    @Override
    public void visitDisjunction(DisjunctionTree tree) {
      increaseComplexity(tree.getOrOperators().get(0), nesting);
      for (SourceCharacter orOperator : tree.getOrOperators().subList(1, tree.getOrOperators().size())) {
        increaseComplexity(orOperator, 1);
      }
      nesting++;
      super.visitDisjunction(tree);
      nesting--;
    }

    @Override
    public void visitRepetition(RepetitionTree tree) {
      increaseComplexity(tree.getQuantifier(), nesting);
      nesting++;
      super.visitRepetition(tree);
      nesting--;
    }

    // Character classes increase the complexity by only one regardless of nesting because they're not that complex by
    // themselves
    @Override
    public void visitCharacterClass(CharacterClassTree tree) {
      increaseComplexity(tree.getOpeningBracket(), 1);
      nesting++;
      super.visitCharacterClass(tree);
      nesting--;
    }

    // Regular groups, names groups and non-capturing groups without flags don't increase complexity because they don't
    // do anything by themselves. However lookarounds, atomic groups and non-capturing groups with flags do because
    // they're more complicated features
    @Override
    public void visitNonCapturingGroup(NonCapturingGroupTree tree) {
      if (tree.getEnabledFlags().isEmpty() && tree.getDisabledFlags().isEmpty()) {
        super.visitNonCapturingGroup(tree);
      } else {
        if (tree.getGroupHeader() == null) {
          increaseComplexity(tree, nesting);
        } else {
          increaseComplexity(tree.getGroupHeader(), nesting);
        }
        nesting++;
        super.visitNonCapturingGroup(tree);
        nesting--;
      }
    }

    @Override
    public void visitLookAround(LookAroundTree tree) {
      increaseComplexity(tree.getGroupHeader(), nesting);
      nesting++;
      super.visitLookAround(tree);
      nesting--;
    }

    @Override
    public void visitBackReference(BackReferenceTree tree) {
      increaseComplexity(tree, 1);
    }
  }

}
