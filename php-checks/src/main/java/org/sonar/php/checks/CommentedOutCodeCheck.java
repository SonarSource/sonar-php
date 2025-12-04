/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
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

import com.sonar.sslr.api.typed.ActionParser;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.regex.Pattern;
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

@Rule(key = "S125")
public class CommentedOutCodeCheck extends PHPVisitorCheck {
  private static final String MESSAGE = "Remove this commented out code.";

  private static final Pattern MULTILINE_COMMENT_REPLACE = Pattern.compile("((/\\*\\*?)|(\\n\\s*\\*(?!/))|(\\*/))");
  private static final Pattern SINGLE_LINE_COMMENT_REPLACE = Pattern.compile("^((//)|(#))");
  private static final Pattern MULTIPLE_LINEBREAKS = Pattern.compile("\\R+");

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
    if (comment.startsWith("/*") && isParsableCode(MULTILINE_COMMENT_REPLACE.matcher(comment).replaceAll(" "))) {
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
        .append(SINGLE_LINE_COMMENT_REPLACE.matcher(t.text().trim()).replaceAll("")));

    if (isParsableCode(mergedSingleLineComment.toString())) {
      SyntaxTrivia firstTrivia = singleLineTrivias.peekFirst();
      SyntaxTrivia lastTrivia = singleLineTrivias.peekLast();
      context().newIssue(this, firstTrivia, lastTrivia, MESSAGE);
    }
    singleLineTrivias.clear();
  }

  private static boolean isParsableCode(String possibleCode) {
    // empty comment should not be commented out code
    if (MULTIPLE_LINEBREAKS.matcher(possibleCode).replaceAll("").trim().isEmpty()) {
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
      ClassDeclarationTree parsedCode = (ClassDeclarationTree) PARSER.parse(String.format(INNER_CLASS_CONTEXT, possibleCode));
      // if this is empty, possibleCode starts with a comment opener, which leads to parsing possibleCode as a single comment
      // But we should only raise an issue, if the parsedCode contains valid code.
      return !parsedCode.members().isEmpty();
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
