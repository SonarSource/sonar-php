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
package org.sonar.php.checks.utils;

import com.sonar.sslr.api.RecognitionException;
import com.sonar.sslr.api.typed.ActionParser;
import java.util.Set;
import java.util.function.Function;
import org.junit.Test;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.php.parser.PHPLexicalGrammar;
import org.sonar.php.parser.PHPParserBuilder;
import org.sonar.php.utils.SourceBuilder;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.sslr.grammar.GrammarRuleKey;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class FunctionArgumentCheckTest {


  private static final Function<ExpressionTree, Boolean> trueFunction = s -> true;

  @Test
  public void checkArgumentRaisesIssueBecausePositionMatches() {
    FunctionCallTree tree = parse("methodName(\"first\", \"second\", \"third\")", PHPLexicalGrammar.MEMBER_EXPRESSION);

    ArgumentVerifierUnaryFunction name = new ArgumentVerifierUnaryFunction(2, "name", trueFunction);

    TestFunctionArgumentCheck testCheck = spy(new TestFunctionArgumentCheck());

    testCheck.checkArgument(tree, "methodname", name);
    verify(testCheck).createIssue(any());
  }

  @Test
  public void checkArgumentRaisesIssueBecauseNameMatches() {
    FunctionCallTree tree = parse("methodName(firstVar:\"first\", secondVar:\"second\", thirdVar:\"third\")",
      PHPLexicalGrammar.MEMBER_EXPRESSION);

    ArgumentVerifierUnaryFunction name = new ArgumentVerifierUnaryFunction(10, "thirdVar", trueFunction);

    TestFunctionArgumentCheck testCheck = spy(new TestFunctionArgumentCheck());

    testCheck.checkArgument(tree, "methodname", name);
    verify(testCheck).createIssue(any());
  }

  @Test
  public void checkArgumentRaisesNoIssueBecauseOfSequentialOrderedMatchers() {
    FunctionCallTree tree = parse("methodName(\"first\", secondVar:\"second\")", PHPLexicalGrammar.MEMBER_EXPRESSION);

    ArgumentVerifierUnaryFunction shouldNotMatch = new ArgumentVerifierUnaryFunction(2, "test", trueFunction);
    ArgumentVerifierUnaryFunction shouldMatch = new ArgumentVerifierUnaryFunction(1, "secondVar", trueFunction);

    TestFunctionArgumentCheck testCheck = spy(new TestFunctionArgumentCheck());

    testCheck.checkArgument(tree, "methodname", shouldNotMatch, shouldMatch);
    verify(testCheck, times(0)).createIssue(any());
  }

  @Test
  public void checkArgumentRaisesIssueBecauseOrderOfSequentialMatchers() {
    FunctionCallTree tree = parse("methodName(\"first\", secondVar:\"second\")", PHPLexicalGrammar.MEMBER_EXPRESSION);

    ArgumentVerifierUnaryFunction shouldNotMatch = new ArgumentVerifierUnaryFunction(2, "test", trueFunction);
    ArgumentVerifierUnaryFunction shouldMatch = new ArgumentVerifierUnaryFunction(1, "secondVar", trueFunction);

    TestFunctionArgumentCheck testCheck = spy(new TestFunctionArgumentCheck());

    testCheck.checkArgument(tree, "methodname", shouldMatch, shouldNotMatch);
    verify(testCheck, times(1)).createIssue(any());
  }

  @Test
  public void checkArgumentRaisesIssuesOnBothMatchers() {

    FunctionCallTree tree = parse("methodName(\"first\", secondVar:\"second\", thirdVar:\"third\")", PHPLexicalGrammar.MEMBER_EXPRESSION);

    ArgumentVerifierUnaryFunction shouldMatchOnSecondVar = new ArgumentVerifierUnaryFunction(1, "secondVar", trueFunction);
    ArgumentVerifierUnaryFunction shouldMatchOnThirdVar = new ArgumentVerifierUnaryFunction(2, "thirdVar", trueFunction);

    TestFunctionArgumentCheck testCheck = spy(new TestFunctionArgumentCheck());

    testCheck.checkArgument(tree, "methodname", shouldMatchOnSecondVar, shouldMatchOnThirdVar);
    verify(testCheck, times(2)).createIssue(any());
  }

  @Test
  public void checkArgumentRaisesIssueMethodNameInMatcherIsNull() {
    FunctionCallTree tree = parse("methodName(\"first\")", PHPLexicalGrammar.MEMBER_EXPRESSION);
    ArgumentVerifierValueContainment matcher = new ArgumentVerifierValueContainment(0, null, Set.of("first"));

    TestFunctionArgumentCheck testCheck = spy(new TestFunctionArgumentCheck());

    testCheck.checkArgument(tree, "methodname", matcher);
    verify(testCheck, times(1)).createIssue(any());
  }

  @Test
  public void checkArgumentRaisesNoIssueMethodNameInMatcherIsNull() {
    FunctionCallTree tree = parse("methodName(\"first\")", PHPLexicalGrammar.MEMBER_EXPRESSION);
    ArgumentMatcherValueContainment matcher = new ArgumentMatcherValueContainment(10, null, Set.of("first"));

    TestFunctionArgumentCheck testCheck = spy(new TestFunctionArgumentCheck());

    testCheck.checkArgument(tree, "methodname", matcher);
    verify(testCheck, times(0)).createIssue(any());
  }

  @Test
  public void checkArgumentRaisesIssuePositionInMatcherIsOutOfBounds() {
    FunctionCallTree tree = parse("methodName(\"first\")", PHPLexicalGrammar.MEMBER_EXPRESSION);
    ArgumentMatcherValueContainment matcher = new ArgumentMatcherValueContainment(10, "name", Set.of("first"));

    TestFunctionArgumentCheck testCheck = spy(new TestFunctionArgumentCheck());

    testCheck.checkArgument(tree, "methodname", matcher);
    verify(testCheck, times(0)).createIssue(any());
  }

  //TODO: Refactor this code snippet below
  protected ActionParser<Tree> p;

  /**
   * Parse the given string and return the first descendant of the given kind.
   *
   * @param s        the string to parse
   * @param rootRule the rule to start parsing from
   * @return the node found for the given kind, null if not found.
   */
  protected <T extends Tree> T parse(String s, GrammarRuleKey rootRule) {
    p = PHPParserBuilder.createParser(rootRule);
    Tree node = p.parse(s);
    checkFullFidelity(node, s.trim());
    return (T) node;
  }

  /**
   * Return the concatenation of all the given node tokens value.
   */
  protected static String expressionToString(Tree node) {
    return SourceBuilder.build(node).trim();
  }

  private static void checkFullFidelity(Tree tree, String inputString) {
    String resultString = expressionToString(tree);
    if (!inputString.equals(resultString)) {
      if (inputString.startsWith(resultString)) {
        String message = "Only beginning of the input string is parsed: " + resultString;
        throw new RecognitionException(0, message);
      } else {
        String message = "Some tokens are lost. See result tree string: " + resultString;
        throw new RecognitionException(0, message);
      }
    }
  }

}

class TestFunctionArgumentCheck extends FunctionArgumentCheck {
  private static final Logger LOG = Loggers.get(TestFunctionArgumentCheck.class);

  @Override
  protected void createIssue(ExpressionTree argument) {
    LOG.info("Issue created");
  }
}
