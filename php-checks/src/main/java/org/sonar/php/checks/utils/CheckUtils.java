/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2018 SonarSource SA
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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.io.BufferedReader;
import java.io.StringReader;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.apache.commons.lang.StringUtils;
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.declaration.FunctionDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.FunctionTree;
import org.sonar.plugins.php.api.tree.declaration.MethodDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.NamespaceNameTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.expression.MemberAccessTree;
import org.sonar.plugins.php.api.tree.expression.NameIdentifierTree;
import org.sonar.plugins.php.api.tree.expression.ParenthesisedExpressionTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.tree.lexical.SyntaxTrivia;
import org.sonar.plugins.php.api.tree.statement.ForStatementTree;
import org.sonar.plugins.php.api.visitors.PhpFile;

public final class CheckUtils {

  private static final Kind[] FUNCTION_KINDS_ARRAY = {
    Kind.METHOD_DECLARATION,
    Kind.FUNCTION_DECLARATION,
    Kind.FUNCTION_EXPRESSION};

  public static final ImmutableList<Kind> FUNCTION_KINDS = ImmutableList.copyOf(FUNCTION_KINDS_ARRAY);

  public static final ImmutableMap<String, String> SUPERGLOBALS_BY_OLD_NAME = ImmutableMap.<String, String>builder()
    .put("$HTTP_SERVER_VARS", "$_SERVER")
    .put("$HTTP_GET_VARS", "$_GET")
    .put("$HTTP_POST_VARS", "$_POST")
    .put("$HTTP_POST_FILES", "$_FILES")
    .put("$HTTP_SESSION_VARS", "$_SESSION")
    .put("$HTTP_ENV_VARS", "$_ENV")
    .put("$HTTP_COOKIE_VARS", "$_COOKIE").build();

  private CheckUtils() {
  }

  public static boolean isFunction(Tree tree) {
    return tree.is(FUNCTION_KINDS_ARRAY);
  }

  /**
   * Returns function or method's name, or "expression" if the given node is a function expression.
   *
   * @param functionDec FUNCTION_DECLARATION, METHOD_DECLARATION or FUNCTION_EXPRESSION
   * @return name of function or "expression" if function expression
   */
  public static String getFunctionName(FunctionTree functionDec) {
    if (functionDec.is(Kind.FUNCTION_DECLARATION)) {
      return "\"" + ((FunctionDeclarationTree) functionDec).name().text() + "\"";
    } else if (functionDec.is(Kind.METHOD_DECLARATION)) {
      return "\"" + ((MethodDeclarationTree) functionDec).name().text() + "\"";
    }
    return "expression";
  }

  /**
   * @return Returns function or static method's name, like "f" or "A::f". Warning, use case insensitive comparison of the result.
   */
  @Nullable
  public static String getFunctionName(FunctionCallTree functionCall) {
    return nameOf(functionCall.callee());
  }

  @Nullable
  private static String nameOf(Tree tree) {
    if (tree.is(Tree.Kind.NAMESPACE_NAME)) {
      return ((NamespaceNameTree) tree).qualifiedName();
    } else if (tree.is(Tree.Kind.NAME_IDENTIFIER)) {
      return ((NameIdentifierTree) tree).text();
    } else if (tree.is(Tree.Kind.CLASS_MEMBER_ACCESS)) {
      MemberAccessTree memberAccess = (MemberAccessTree) tree;
      String className = nameOf(memberAccess.object());
      String memberName = nameOf(memberAccess.member());
      if (className != null && memberName != null) {
        return className + "::" + memberName;
      }
    }
    return null;
  }

  /**
   * Return whether the method is overriding a parent method or not.
   *
   * @param declaration METHOD_DECLARATION
   * @return true if method has tag "@inheritdoc" in it's doc comment.
   */
  public static boolean isOverriding(MethodDeclarationTree declaration) {
    for (SyntaxTrivia comment : ((PHPTree) declaration).getFirstToken().trivias()) {
      if (StringUtils.containsIgnoreCase(comment.text(), "@inheritdoc")) {
        return true;
      }
    }
    return false;
  }

  public static boolean isExitExpression(FunctionCallTree functionCallTree) {
    String callee = functionCallTree.callee().toString();
    return "die".equalsIgnoreCase(callee) || "exit".equalsIgnoreCase(callee);
  }

  public static boolean hasModifier(List<SyntaxToken> modifiers, String toFind) {
    for (SyntaxToken modifier : modifiers) {
      if (modifier.text().equalsIgnoreCase(toFind)) {
        return true;
      }
    }
    return false;
  }

  public static boolean isClosingTag(SyntaxToken token) {
    if (token.is(Kind.INLINE_HTML_TOKEN)) {
      String text = token.text().trim();
      return "?>".equals(text) || "%>".equals(text);
    }
    return false;
  }

  public static Stream<String> lines(PhpFile file) {
    return new BufferedReader(new StringReader(file.contents())).lines();
  }

  public static ExpressionTree skipParenthesis(ExpressionTree expr) {
    if (expr.is(Tree.Kind.PARENTHESISED_EXPRESSION)) {
      return skipParenthesis(((ParenthesisedExpressionTree) expr).expression());
    }
    return expr;
  }

  @Nullable
  public static Tree findPreviousSibling(Tree tree) {
    Tree parent = tree.getParent();
    if (parent == null) {
      return null;
    }
    Tree previousSibling = null;
    Iterator<Tree> childrenIterator = ((PHPTree) parent).childrenIterator();
    while (childrenIterator.hasNext()) {
      Tree child = childrenIterator.next();
      if (child == tree) {
        break;
      }
      previousSibling = child;
    }
    return previousSibling;
  }

  @Nullable
  public static SyntaxToken findPreviousToken(Tree tree) {
    Tree previousSibling = findPreviousSibling(tree);
    if (previousSibling != null) {
      return ((PHPTree) previousSibling).getLastToken();
    }
    return null;
  }

  public static boolean isDisguisedShortEchoStatement(Tree tree) {
    if (!tree.is(Kind.EXPRESSION_STATEMENT, Kind.EXPRESSION_LIST_STATEMENT)) {
      return false;
    }
    SyntaxToken previousToken = findPreviousToken(tree);
    if (previousToken == null) {
      return false;
    }
    boolean isFileOpeningTagToken = previousToken.line() == 1 && previousToken.column() == 0;
    return (isFileOpeningTagToken || previousToken.is(Kind.INLINE_HTML_TOKEN)) && previousToken.text().endsWith("<?=");
  }

  @Nullable
  public static ExpressionTree getForCondition(ForStatementTree tree) {
    if (tree.condition().isEmpty()) {
      return null;
    }
    // in a loop, all conditions are evaluated but only the last one is used as the result
    return tree.condition().get(tree.condition().size() - 1);
  }

}
