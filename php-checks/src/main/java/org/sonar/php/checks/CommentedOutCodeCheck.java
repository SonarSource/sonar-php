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
package org.sonar.php.checks;

import com.sonar.sslr.api.typed.ActionParser;
import java.util.ArrayDeque;
import java.util.Deque;
import org.sonar.check.Rule;
import org.sonar.php.parser.PHPLexicalGrammar;
import org.sonar.php.parser.PHPParserBuilder;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.ClassDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.MethodDeclarationTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxTrivia;
import org.sonar.plugins.php.api.tree.statement.BlockTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

@Rule(key = CommentedOutCodeCheck.KEY)
public class CommentedOutCodeCheck extends PHPVisitorCheck {

  public static final String KEY = "S125";
  private static final String MESSAGE = "Remove this commented out code.";

  private static final String MULTILINE_COMMENT_REPLACE = "((/\\*\\*?)|(\\n\\s*\\*(?!/))|(\\*/))";
  private static final String SINGLE_LINE_COMMENT_REPLACE = "^((//)|(#))";


  private static final String INNER_CLASS_CONTEXT = "class DummyClass{%s}";
  private static final String INNER_METHOD_CONTEXT = "class DummyClass{public function dummyMethod(){%s}}";

  private static final Tree.Kind[] TOP_STATEMENTS = {
    Tree.Kind.NAMESPACE_STATEMENT,
    Tree.Kind.GROUP_USE_STATEMENT
  };

  private static final ActionParser<Tree> PARSER = PHPParserBuilder.createParser(PHPLexicalGrammar.TOP_STATEMENT);
  private static final Deque<SyntaxTrivia> singleLineTrivias = new ArrayDeque<>();

  @Override
  public void visitCompilationUnit(CompilationUnitTree tree) {
    super.visitCompilationUnit(tree);

    // check possible last single line comment and clean up afterwards
    checkSingleLineComments();
  }

  @Override
  public void visitTrivia(SyntaxTrivia trivia) {
    String comment = trivia.text();
    if (comment.startsWith("/*") && isParsableCode(comment.replaceAll(MULTILINE_COMMENT_REPLACE, " "))) {
      context().newIssue(this, trivia, MESSAGE);
    }
    if (comment.startsWith("//") || comment.startsWith("#")) {
      collectSingleLineComment(trivia);
    }

    super.visitTrivia(trivia);
  }

  private void collectSingleLineComment(SyntaxTrivia trivia) {
    // summarise previously collected single line comments if they have formed a consistent block.
    if (!singleLineTrivias.isEmpty() && isNewCommentBlock(trivia)) {
      checkSingleLineComments();
    }

    singleLineTrivias.addLast(trivia);
  }

  // a continuous block has no free line and has the same commentary token.
  private static boolean isNewCommentBlock(SyntaxTrivia trivia) {
    SyntaxTrivia prevTrivia = singleLineTrivias.peekLast();
    return prevTrivia.line() + 1 != trivia.line()
      || prevTrivia.text().charAt(0) != trivia.text().charAt(0);
  }

  private void checkSingleLineComments() {
    StringBuilder mergedSingleLineComment = new StringBuilder();

    singleLineTrivias
      .iterator()
      .forEachRemaining(t -> mergedSingleLineComment
        .append(t.text().trim().replaceAll(SINGLE_LINE_COMMENT_REPLACE, "")));

    if (isParsableCode(mergedSingleLineComment.toString())) {
      SyntaxTrivia firstTrivia = singleLineTrivias.peekFirst();
      SyntaxTrivia lastTrivia = singleLineTrivias.peekLast();
      context().newIssue(this, firstTrivia, lastTrivia, MESSAGE);
    }
    singleLineTrivias.clear();
  }

  private static boolean isParsableCode(String possibleCode) {
    // empty comment should not be commented out code
    if (possibleCode.replaceAll("\\R+", "").trim().length() == 0) {
      return false;
    }

    // try to parse in an inner method context to cover statements which are only allowed in a method declaration
    // this also covers all statements which are allowed in function or first layer context
    try {
      ClassDeclarationTree classDeclaration = (ClassDeclarationTree) PARSER.parse(String.format(INNER_METHOD_CONTEXT, possibleCode));
      // an URL (http://test.com) is parsed as label which is valid syntax, but will lead to false positives
      return !((BlockTree) ((MethodDeclarationTree) classDeclaration.members().get(0)).body()).statements().get(0).is(Tree.Kind.LABEL);
    } catch (Exception e) {
      // continue on parser error
    }

    // try to parse in an inner class context to cover statements which are only allowed in a class declaration
    try {
      PARSER.parse(String.format(INNER_CLASS_CONTEXT, possibleCode));
      return true;
    } catch (Exception e) {
      // continue on parser error
    }

    // try to parse in script context to cover 'namespace', 'use' or other top statements which can not be citizens of classes or methods
    try {
      Tree tree = PARSER.parse(possibleCode);
      return tree.is(TOP_STATEMENTS);
    } catch (Exception e) {
      // continue on parser error
    }

    return false;
  }

}
