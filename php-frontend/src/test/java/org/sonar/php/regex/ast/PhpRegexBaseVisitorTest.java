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
package org.sonar.php.regex.ast;

import java.util.ArrayList;
import org.junit.Test;
import org.sonar.php.regex.RegexParserTestUtils;
import org.sonarsource.analyzer.commons.regex.ast.LookAroundTree;
import org.sonarsource.analyzer.commons.regex.ast.RegexBaseVisitor;
import org.sonarsource.analyzer.commons.regex.ast.RegexTree;
import org.sonarsource.analyzer.commons.regex.ast.SequenceTree;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.php.regex.RegexParserTestUtils.assertType;

public class PhpRegexBaseVisitorTest {

  private ArrayList<RegexTree> visitedItems = new ArrayList<>();

  @Test
  public void test_visitConditionalSubpatterns_with_no_pattern() {
    RegexTree tree = visitRegex("'/(?(?=1)ab|cd)/'", new ConditionalSubpatternsVisitor());
    ConditionalSubpatternsTree conditionalSubpatterns = assertType(ConditionalSubpatternsTree.class, tree);
    assertThat(visitedItems).containsExactly(
      conditionalSubpatterns.getCondition(),
      conditionalSubpatterns.getYesPattern(),
      conditionalSubpatterns.getNoPattern());
  }

  @Test
  public void test_visitConditionalSubpatterns_without_no_pattern() {
    RegexTree tree = visitRegex("'/(?(?=1)ab)/'", new ConditionalSubpatternsVisitor());
    ConditionalSubpatternsTree conditionalSubpatterns = assertType(ConditionalSubpatternsTree.class, tree);
    assertThat(visitedItems).containsExactly(
      conditionalSubpatterns.getCondition(),
      conditionalSubpatterns.getYesPattern());
  }

  @Test
  public void test_visitConditionalSubpatterns_with_reference_condition() {
    RegexTree tree = visitRegex("'/(?(1)ab|cd)/'", new ConditionalSubpatternsVisitor());
    ConditionalSubpatternsTree conditionalSubpatterns = assertType(ConditionalSubpatternsTree.class, tree);
    assertThat(visitedItems).containsExactly(
      conditionalSubpatterns.getYesPattern(),
      conditionalSubpatterns.getNoPattern());
  }

  private RegexTree visitRegex(String regex, RegexBaseVisitor visitor) {
    RegexTree tree = RegexParserTestUtils.assertSuccessfulParse(regex);
    visitedItems.clear();
    visitor.visit(tree);
    return tree;
  }

  class ConditionalSubpatternsVisitor extends PhpRegexBaseVisitor {
    @Override
    public void visitSequence(SequenceTree tree) {
      visitedItems.add(tree);
      super.visitSequence(tree);
    }

    @Override
    public void visitLookAround(LookAroundTree tree) {
      visitedItems.add(tree);
      super.visitLookAround(tree);
    }
  }
}
