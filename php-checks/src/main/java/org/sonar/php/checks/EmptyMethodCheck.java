/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2026 SonarSource Sàrl
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

import java.util.Collections;
import java.util.regex.Pattern;
import org.sonar.check.Rule;
import org.sonar.php.tree.TreeUtils;
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.declaration.ClassDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.FunctionDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.FunctionTree;
import org.sonar.plugins.php.api.tree.declaration.MethodDeclarationTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.tree.lexical.SyntaxTrivia;
import org.sonar.plugins.php.api.tree.statement.BlockTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;
import org.sonar.plugins.php.api.visitors.PhpFile;

import static org.sonar.php.utils.collections.ListUtils.getLast;

@Rule(key = "S1186")
public class EmptyMethodCheck extends PHPVisitorCheck {

  private static final String MESSAGE = "Add a comment explaining why this %s is empty, throw an Exception or complete the implementation.";

  @Override
  public void visitCompilationUnit(CompilationUnitTree tree) {
    if (!StubFileDetector.isStub(context().getPhpFile(), tree)) {
      super.visitCompilationUnit(tree);
    }
  }

  @Override
  public void visitMethodDeclaration(MethodDeclarationTree tree) {
    if (tree.body().is(Kind.BLOCK) && !(hasContent((BlockTree) tree.body()) || isClassAbstract(tree)
      || hasCommentAbove(((PHPTree) tree).getFirstToken()) || isConstructorPropertyPromotion(tree))) {
      commitIssue(tree, "method");
    }

    super.visitMethodDeclaration(tree);
  }

  @Override
  public void visitFunctionDeclaration(FunctionDeclarationTree tree) {
    if (!(hasContent(tree.body()) || hasCommentAbove(((PHPTree) tree).getFirstToken()))) {
      commitIssue(tree, "function");
    }

    super.visitFunctionDeclaration(tree);
  }

  private static boolean hasCommentAbove(SyntaxToken token) {
    int beforeDeclarationLine = token.line() - 1;
    SyntaxTrivia trivia = getLast(token.trivias(), null);
    return trivia != null && beforeDeclarationLine == trivia.endLine();
  }

  private static boolean isClassAbstract(MethodDeclarationTree tree) {
    ClassDeclarationTree classTree = (ClassDeclarationTree) TreeUtils.findAncestorWithKind(tree, Collections.singletonList(Kind.CLASS_DECLARATION));
    return classTree != null && classTree.isAbstract();
  }

  private static boolean hasContent(BlockTree tree) {
    if (!tree.statements().isEmpty()) {
      return true;
    }

    // Comments are attached to the closing brace when the body has no statements.
    SyntaxTrivia trivia = getLast(tree.closeCurlyBraceToken().trivias(), null);
    return trivia != null;
  }

  private static boolean isConstructorPropertyPromotion(MethodDeclarationTree tree) {
    return tree.name().text().equalsIgnoreCase("__construct") && tree.parameters().parameters().stream().anyMatch(p -> p.visibility() != null);
  }

  private void commitIssue(FunctionTree tree, String type) {
    context().newIssue(this, tree, String.format(MESSAGE, type));
  }

  // API stubs describe declarations, so their empty bodies do not represent missing implementations.
  // Recognize the conventional .stub.php suffix and file-level generation markers.
  private static class StubFileDetector {
    private static final Pattern GENERATION_MARKER = Pattern.compile("(?m)^[ \\t]*(?:/\\*\\*|\\*)[ \\t]*@generate-(?:class|function)-entries(?=[ \\t]|\\*/|$)");

    private static boolean isStub(PhpFile file, CompilationUnitTree tree) {
      // PHP's stub guide names .stub.php as the conventional filename suffix:
      // https://github.com/php/php-src/blob/d7f966e073be8b1ad4bd829d60b0dfa730f53dda/docs/source/miscellaneous/stubs.rst#L49-L56
      return file.filename().endsWith(".stub.php") || hasGenerationMarker(tree);
    }

    // File-level generation tags are documented in the PHP stub guide:
    // https://github.com/php/php-src/blob/master/docs/source/miscellaneous/stubs.rst
    private static boolean hasGenerationMarker(CompilationUnitTree tree) {
      if (tree.script() == null || tree.script().statements().isEmpty()) {
        return false;
      }
      var leadingComments = ((PHPTree) tree.script().statements().get(0)).getFirstToken().trivias();
      return leadingComments.stream()
        .map(SyntaxTrivia::text)
        .anyMatch(comment -> comment.startsWith("/**") && GENERATION_MARKER.matcher(comment).find());
    }
  }

}
