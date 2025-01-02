/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource SA
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
package org.sonar.php.checks;

import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import org.sonar.check.Rule;
import org.sonar.php.api.PHPKeyword;
import org.sonar.plugins.php.api.symbols.SymbolTable;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.declaration.NamespaceNameTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.expression.LiteralTree;
import org.sonar.plugins.php.api.tree.expression.MatchExpressionTree;
import org.sonar.plugins.php.api.tree.expression.MemberAccessTree;
import org.sonar.plugins.php.api.tree.expression.NameIdentifierTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

@Rule(key = KeywordsAndConstantsNotLowerCaseCheck.KEY)
public class KeywordsAndConstantsNotLowerCaseCheck extends PHPVisitorCheck {

  public static final String KEY = "S1781";
  private static final String MESSAGE = "Write this \"%s\" %s in %s case.";

  private static final Pattern PATTERN = Pattern.compile("[a-z_]+");
  private static final Pattern PATTERN_DRUPAL = Pattern.compile("[A-Z_]+");
  private static final Set<String> KEYWORDS = Set.of(PHPKeyword.getKeywordValues());

  @Override
  public void visitLiteral(LiteralTree tree) {
    super.visitLiteral(tree);

    if (tree.is(Kind.NULL_LITERAL, Kind.BOOLEAN_LITERAL)) {
      if (context().getFramework() == SymbolTable.Framework.DRUPAL) {
        checkWithDrupalConvention(tree, tree.value(), "constant");
      } else {
        check(tree, tree.value(), "constant");
      }
    }
  }

  @Override
  public void visitToken(SyntaxToken token) {
    super.visitToken(token);

    if (token.text().toLowerCase(Locale.ENGLISH).equals("match")) {
      if (token.getParent() instanceof MatchExpressionTree) {
        check(token, token.text(), "keyword");
      }
    } else {
      if (KEYWORDS.contains(token.text().toLowerCase(Locale.ENGLISH))) {
        check(token, token.text(), "keyword");
      }
    }
  }

  @Override
  public void visitNameIdentifier(NameIdentifierTree tree) {
    // do nothing
  }

  // We usually don't want to check identifiers...
  // except in some places where we use fake identifiers such as `echo` or `static`
  private void checkForFakeIdentifier(Tree tree) {
    if (tree.is(Kind.NAME_IDENTIFIER)) {
      visitToken(((NameIdentifierTree) tree).token());
    }
  }

  @Override
  public void visitFunctionCall(FunctionCallTree tree) {
    ExpressionTree callee = tree.callee();
    if (callee.is(Kind.NAMESPACE_NAME)) {
      NamespaceNameTree name = (NamespaceNameTree) callee;
      callee = name.name();
    }
    checkForFakeIdentifier(callee);
    super.visitFunctionCall(tree);
  }

  @Override
  public void visitMemberAccess(MemberAccessTree tree) {
    checkForFakeIdentifier(tree.object());
    super.visitMemberAccess(tree);
  }

  private void check(Tree tree, String value, String kind) {
    if (!PATTERN.matcher(value).matches()) {
      String message = MESSAGE.formatted(value, kind, "lower");
      context().newIssue(this, tree, message);
    }
  }

  private void checkWithDrupalConvention(Tree tree, String value, String kind) {
    if (!PATTERN_DRUPAL.matcher(value).matches()) {
      String message = MESSAGE.formatted(value, kind, "upper");
      context().newIssue(this, tree, message);
    }
  }

}
