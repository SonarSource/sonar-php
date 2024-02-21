/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2024 SonarSource SA
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
package org.sonar.php.checks.utils.argumentmatching;

import java.util.Set;
import java.util.function.Function;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.php.parser.PHPLexicalGrammar;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class FunctionArgumentCheckTest extends PhpTreeModelTest {

  private static final Function<ExpressionTree, Boolean> trueFunction = s -> true;

  @Test
  void checkArgumentRaisesIssueWhenPositionMatches() {
    FunctionCallTree tree = parse("methodName(\"firstValue\", \"secondValue\", \"thirdValue\")", PHPLexicalGrammar.MEMBER_EXPRESSION);

    ArgumentVerifierUnaryFunction matcher = ArgumentVerifierUnaryFunction.builder()
      .position(2)
      .name("name")
      .matchingFunction(trueFunction)
      .build();

    TestFunctionArgumentCheck testCheck = spy(new TestFunctionArgumentCheck());

    testCheck.checkArgument(tree, "methodname", matcher);
    verify(testCheck).createIssue(any());
  }

  @Test
  void checkArgumentRaisesIssueWhenNameMatches() {
    FunctionCallTree tree = parse("methodName(firstVar:\"firstValue\", secondVar:\"secondValue\", thirdVar:\"thirdValue\")",
      PHPLexicalGrammar.MEMBER_EXPRESSION);

    ArgumentVerifierUnaryFunction matcher = ArgumentVerifierUnaryFunction.builder()
      .position(10)
      .name("thirdVar")
      .matchingFunction(trueFunction)
      .build();

    TestFunctionArgumentCheck testCheck = spy(new TestFunctionArgumentCheck());

    testCheck.checkArgument(tree, "methodname", matcher);
    verify(testCheck).createIssue(any());
  }

  @Test
  void checkArgumentRaisesIssueBecauseOrderOfSequentialMatchers() {
    FunctionCallTree tree = parse("methodName(\"firstValue\", secondVar:\"secondValue\")", PHPLexicalGrammar.MEMBER_EXPRESSION);

    ArgumentVerifierUnaryFunction shouldNotMatch = ArgumentVerifierUnaryFunction.builder()
      .position(2)
      .name("test")
      .matchingFunction(trueFunction)
      .build();

    ArgumentVerifierUnaryFunction shouldMatch = ArgumentVerifierUnaryFunction.builder()
      .position(1)
      .name("secondVar")
      .matchingFunction(trueFunction)
      .build();

    TestFunctionArgumentCheck testCheck = spy(new TestFunctionArgumentCheck());

    testCheck.checkArgument(tree, "methodname", shouldMatch, shouldNotMatch);
    verify(testCheck, times(1)).createIssue(any());
  }

  @Test
  void checkArgumentRaisesIssuesOnBothMatchers() {
    FunctionCallTree tree = parse("methodName(\"firstValue\", secondVar:\"secondValue\", thirdVar:\"thirdValue\")",
      PHPLexicalGrammar.MEMBER_EXPRESSION);

    ArgumentVerifierUnaryFunction shouldMatchOnSecondVar = ArgumentVerifierUnaryFunction.builder()
      .position(1)
      .name("secondVar")
      .matchingFunction(trueFunction)
      .build();

    ArgumentVerifierUnaryFunction shouldMatchOnThirdVar = ArgumentVerifierUnaryFunction.builder()
      .position(2)
      .name("thirdVar")
      .matchingFunction(trueFunction)
      .build();

    TestFunctionArgumentCheck testCheck = spy(new TestFunctionArgumentCheck());

    testCheck.checkArgument(tree, "methodname", shouldMatchOnSecondVar, shouldMatchOnThirdVar);
    verify(testCheck, times(2)).createIssue(any());
  }

  @Test
  void checkArgumentRaisesNoIssueWhenMatchedPositionIsNamed() {
    FunctionCallTree tree = parse("methodName(\"firstValue\", secondVar:\"secondValue\", thirdVar:\"thirdValue\")",
      PHPLexicalGrammar.MEMBER_EXPRESSION);

    ArgumentVerifierUnaryFunction matcher = ArgumentVerifierUnaryFunction.builder()
      .position(2)
      .name("name")
      .matchingFunction(trueFunction)
      .build();

    TestFunctionArgumentCheck testCheck = spy(new TestFunctionArgumentCheck());

    testCheck.checkArgument(tree, "methodname", matcher);
    verify(testCheck, times(0)).createIssue(any());
  }

  @Test
  void checkArgumentRaisesNoIssueBecauseOfSequentialOrderedMatchers() {
    FunctionCallTree tree = parse("methodName(\"firstValue\", secondVar:\"secondValue\")", PHPLexicalGrammar.MEMBER_EXPRESSION);

    ArgumentVerifierUnaryFunction shouldNotMatch = ArgumentVerifierUnaryFunction.builder()
      .position(2)
      .name("test")
      .matchingFunction(trueFunction)
      .build();

    ArgumentVerifierUnaryFunction shouldMatch = ArgumentVerifierUnaryFunction.builder()
      .position(1)
      .name("secondVar")
      .matchingFunction(trueFunction)
      .build();

    TestFunctionArgumentCheck testCheck = spy(new TestFunctionArgumentCheck());

    testCheck.checkArgument(tree, "methodname", shouldNotMatch, shouldMatch);
    verify(testCheck, times(0)).createIssue(any());
  }

  @Test
  void checkArgumentRaisesIssueWhenMethodNameInMatcherIsNull() {
    FunctionCallTree tree = parse("methodName(\"firstValue\")", PHPLexicalGrammar.MEMBER_EXPRESSION);

    ArgumentVerifierValueContainment matcher = ArgumentVerifierValueContainment.builder()
      .position(0)
      .values(Set.of("firstValue"))
      .build();

    TestFunctionArgumentCheck testCheck = spy(new TestFunctionArgumentCheck());

    testCheck.checkArgument(tree, "methodname", matcher);
    verify(testCheck, times(1)).createIssue(any());
  }

  @Test
  void checkArgumentRaisesNoIssueWhenMethodNameInMatcherIsNull() {
    FunctionCallTree tree = parse("methodName(\"firstValue\")", PHPLexicalGrammar.MEMBER_EXPRESSION);

    ArgumentMatcherValueContainment matcher = ArgumentMatcherValueContainment.builder()
      .position(10)
      .values(Set.of("firstValue"))
      .build();

    TestFunctionArgumentCheck testCheck = spy(new TestFunctionArgumentCheck());

    testCheck.checkArgument(tree, "methodname", matcher);
    verify(testCheck, times(0)).createIssue(any());
  }

  @Test
  void checkArgumentRaisesIssueWhenPositionInMatcherIsOutOfBounds() {
    FunctionCallTree tree = parse("methodName(\"first\")", PHPLexicalGrammar.MEMBER_EXPRESSION);

    ArgumentMatcherValueContainment matcher = ArgumentMatcherValueContainment.builder()
      .position(10)
      .name("name")
      .values(Set.of("firstValue"))
      .build();

    TestFunctionArgumentCheck testCheck = spy(new TestFunctionArgumentCheck());

    testCheck.checkArgument(tree, "methodname", matcher);
    verify(testCheck, times(0)).createIssue(any());
  }
}

class TestFunctionArgumentCheck extends FunctionArgumentCheck {
  private static final Logger LOG = LoggerFactory.getLogger(TestFunctionArgumentCheck.class);

  @Override
  protected void createIssue(ExpressionTree argument) {
    LOG.info("Issue created");
  }
}
