/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource Sàrl
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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.sonar.check.Rule;
import org.sonar.php.checks.phpini.PhpIniBoolean;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.php.ini.PhpIniCheck;
import org.sonar.php.ini.PhpIniIssue;
import org.sonar.php.ini.tree.PhpIniFile;
import org.sonar.php.tree.impl.declaration.ClassNamespaceNameTreeImpl;
import org.sonar.plugins.php.api.symbols.QualifiedName;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.declaration.CallArgumentTree;
import org.sonar.plugins.php.api.tree.declaration.NamespaceNameTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.expression.MemberAccessTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

import static org.sonar.php.checks.phpini.PhpIniFiles.checkRequiredBoolean;
import static org.sonar.php.checks.utils.CheckUtils.getLowerCaseFunctionName;
import static org.sonar.php.checks.utils.CheckUtils.isFalseValue;
import static org.sonar.php.checks.utils.CheckUtils.nameOf;
import static org.sonar.plugins.php.api.symbols.QualifiedName.qualifiedName;
import static org.sonar.plugins.php.api.tree.Tree.Kind.CLASS_MEMBER_ACCESS;

@Rule(key = "S3330")
public class HttpOnlyCheck extends PHPVisitorCheck implements PhpIniCheck {

  private static final String MESSAGE_PHP_INI = "Set the \"session.cookie_httponly\" property to \"true\" if needed.";
  private static final String MESSAGE = "Make sure creating this cookie without the \"httpOnly\" flag is safe here.";

  private static final List<String> SET_COOKIE_FUNCTIONS = Arrays.asList("setcookie", "setrawcookie");
  private static final QualifiedName SYMFONY_COOKIE = qualifiedName("Symfony\\Component\\HttpFoundation\\Cookie");

  @Override
  public List<PhpIniIssue> analyze(PhpIniFile phpIniFile) {
    return checkRequiredBoolean(
      phpIniFile,
      "session.cookie_httponly",
      PhpIniBoolean.ON,
      MESSAGE_PHP_INI, MESSAGE_PHP_INI);
  }

  @Override
  public void visitFunctionCall(FunctionCallTree tree) {

    if (isSetCookie(tree)) {
      Optional<CallArgumentTree> argument = CheckUtils.argument(tree, "httponly", 6);

      if (argument.isPresent()) {
        createIssueIfHttpOnlyIsFalse(argument.get().value(), tree);
      } else if (tree.callArguments().size() != 3) {
        // if only 3 argument are defined there is an ambiguity so we don't raise issue
        createIssueIfCookieValueIsNotHardcoded(tree);
      }
    }
    if (isSymfonyCookieCreation(tree)) {
      Optional<CallArgumentTree> argument = CheckUtils.argument(tree, "httpOnly", 6);

      if (argument.isPresent()) {
        createIssueIfHttpOnlyIsFalse(argument.get().value(), tree);
      }
    }

    super.visitFunctionCall(tree);
  }

  private static boolean isSetCookie(FunctionCallTree tree) {
    String functionName = getLowerCaseFunctionName(tree);
    return functionName != null && SET_COOKIE_FUNCTIONS.contains(functionName);
  }

  private boolean isSymfonyCookieCreation(FunctionCallTree tree) {
    ExpressionTree callee = tree.callee();

    if (callee.is(CLASS_MEMBER_ACCESS)) {
      MemberAccessTree memberAccessTree = (MemberAccessTree) callee;
      ExpressionTree receiver = memberAccessTree.object();
      String method = nameOf(memberAccessTree.member());
      return "create".equals(method) && receiver.is(Kind.NAMESPACE_NAME) && SYMFONY_COOKIE.equals(getFullyQualifiedName((NamespaceNameTree) receiver));
    }
    if (callee instanceof ClassNamespaceNameTreeImpl classNamespaceName) {
      return classNamespaceName.symbol().qualifiedName().equals(SYMFONY_COOKIE);
    }
    return false;
  }

  private void createIssueIfHttpOnlyIsFalse(ExpressionTree argument, FunctionCallTree tree) {
    if (isFalseValue(argument)) {
      context().newIssue(this, tree.callee(), MESSAGE).secondary(argument, null);
    }
  }

  private void createIssueIfCookieValueIsNotHardcoded(FunctionCallTree tree) {
    Optional<CallArgumentTree> cookieValue = CheckUtils.argument(tree, "value", 1);
    if (cookieValue.isEmpty() || isHardcodedOrNullCookieValue(cookieValue.get())) {
      return;
    }
    context().newIssue(this, tree.callee(), MESSAGE);
  }

  private static boolean isHardcodedOrNullCookieValue(CallArgumentTree cookieValue) {
    return cookieValue.value().is(Kind.NULL_LITERAL) || cookieValue.value().is(Kind.REGULAR_STRING_LITERAL);
  }
}
