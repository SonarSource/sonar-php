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
package org.sonar.php.checks.security;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nullable;
import org.sonar.check.Rule;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.php.utils.collections.SetUtils;
import org.sonar.php.tree.impl.expression.PrefixExpressionTreeImpl;
import org.sonar.plugins.php.api.symbols.QualifiedName;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.CallArgumentTree;
import org.sonar.plugins.php.api.tree.declaration.ClassDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.ClassTree;
import org.sonar.plugins.php.api.tree.declaration.NamespaceNameTree;
import org.sonar.plugins.php.api.tree.expression.AnonymousClassTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.expression.LiteralTree;
import org.sonar.plugins.php.api.tree.statement.UseTraitDeclarationTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.sonar.plugins.php.api.symbols.QualifiedName.qualifiedName;
import static org.sonar.plugins.php.api.tree.Tree.Kind.REGULAR_STRING_LITERAL;

@Rule(key = "S4792")
public class LoggerConfigurationCheck extends PHPVisitorCheck {

  private static final String MESSAGE = "Make sure that this logger's configuration is safe.";
  private static final String ERROR_REPORTING = "error_reporting";
  private static final Set<String> GLOBAL_CONFIGURATION_FUNCTIONS = SetUtils.immutableSetOf("ini_set", "ini_alter");
  private static final Map<String, List<String>> WHITELISTED_VALUE_BY_DIRECTIVE = buildWhitelistedValues();
  private static final QualifiedName PSR_LOG_ABSTRACT_LOGGER_CLASS = qualifiedName("Psr\\Log\\AbstractLogger");
  private static final QualifiedName PSR_LOG_LOGGER_INTERFACE = qualifiedName("Psr\\Log\\LoggerInterface");
  private static final QualifiedName PSR_LOG_LOGGER_TRAIT = qualifiedName("Psr\\Log\\LoggerTrait");

  private static Map<String, List<String>> buildWhitelistedValues() {
    Map<String, List<String>> map = new HashMap<>();
    map.put("docref_root", singletonList("0"));
    map.put("display_errors", singletonList("0"));
    map.put("display_startup_errors", singletonList("0"));
    map.put("error_log", emptyList());
    map.put(ERROR_REPORTING, asList("E_ALL", "32767", "-1"));
    map.put("log_errors", singletonList("1"));
    map.put("log_errors_max_length", singletonList("0"));
    map.put("ignore_repeated_errors", singletonList("0"));
    map.put("ignore_repeated_source", singletonList("0"));
    map.put("track_errors", singletonList("1"));
    return map;
  }

  @Override
  public void visitFunctionCall(FunctionCallTree tree) {
    super.visitFunctionCall(tree);

    ExpressionTree callee = tree.callee();
    if (!callee.is(Tree.Kind.NAMESPACE_NAME)) {
      return;
    }

    String lowerCaseQualifiedName = ((NamespaceNameTree) callee).qualifiedName().toLowerCase(Locale.ROOT);
    if (ERROR_REPORTING.equals(lowerCaseQualifiedName)) {
      Optional<CallArgumentTree> argument = CheckUtils.argument(tree, "level", 0);
      if (argument.isPresent() && isSuspiciousDirective(ERROR_REPORTING, argument.get().value())) {
        context().newIssue(this, tree, MESSAGE);
      }
    } else {
      if (isSuspiciousGlobalConfiguration(lowerCaseQualifiedName, tree)) {
        context().newIssue(this, tree, MESSAGE);
      }
    }
  }

  @Override
  public void visitClassDeclaration(ClassDeclarationTree tree) {
    super.visitClassDeclaration(tree);
    checkSuspiciousClassDeclaration(tree);
  }

  @Override
  public void visitAnonymousClass(AnonymousClassTree tree) {
    super.visitAnonymousClass(tree);
    checkSuspiciousClassDeclaration(tree);
  }

  @Override
  public void visitUseTraitDeclaration(UseTraitDeclarationTree tree) {
    super.visitUseTraitDeclaration(tree);

    tree.traits().stream()
      .filter(trait -> PSR_LOG_LOGGER_TRAIT.equals(getFullyQualifiedName(trait)))
      .forEach(trait -> context().newIssue(this, trait, MESSAGE));
  }

  private void checkSuspiciousClassDeclaration(ClassTree tree) {
    NamespaceNameTree superClass = tree.superClass();
    if (superClass != null && getFullyQualifiedName(superClass).equals(PSR_LOG_ABSTRACT_LOGGER_CLASS)) {
      context().newIssue(this, superClass, MESSAGE);
    }

    tree.superInterfaces().stream()
      .filter(superInterface -> PSR_LOG_LOGGER_INTERFACE.equals(getFullyQualifiedName(superInterface)))
      .forEach(superInterface -> context().newIssue(this, superInterface, MESSAGE));
  }

  private static boolean isSuspiciousGlobalConfiguration(String lowerCaseQualifiedName, FunctionCallTree tree) {
    Optional<CallArgumentTree> arg1 = CheckUtils.argument(tree, "varname", 0);
    Optional<CallArgumentTree> arg2 = CheckUtils.argument(tree, "newvalue", 1);

    return GLOBAL_CONFIGURATION_FUNCTIONS.contains(lowerCaseQualifiedName) && tree.callArguments().size() == 2
      && arg1.isPresent() && arg2.isPresent() && isSuspiciousDirective(getStringValue(arg1.get().value()), arg2.get().value());
  }

  private static boolean isSuspiciousDirective(@Nullable String directive, ExpressionTree argumentValue) {
    List<String> expectedArguments = WHITELISTED_VALUE_BY_DIRECTIVE.get(directive);
    if (expectedArguments == null) {
      // any directive not in the map is not considered for raising issue
      return false;
    }
    return !expectedArguments.contains(getRawValue(argumentValue));
  }

  @Nullable
  private static String getStringValue(ExpressionTree argumentName) {
    if (argumentName.is(REGULAR_STRING_LITERAL)) {
      return CheckUtils.trimQuotes((LiteralTree) argumentName);
    }
    return null;
  }

  @Nullable
  private static String getRawValue(ExpressionTree tree) {
    if (tree.is(Tree.Kind.NULL_LITERAL, Tree.Kind.BOOLEAN_LITERAL, Tree.Kind.NUMERIC_LITERAL)) {
      return ((LiteralTree) tree).value();
    } else if (tree instanceof PrefixExpressionTreeImpl) {
      PrefixExpressionTreeImpl prefixExpressionTree = (PrefixExpressionTreeImpl) tree;
      return prefixExpressionTree.operator().text() + getRawValue(prefixExpressionTree.expression());
    } else if (tree.is(Tree.Kind.NAMESPACE_NAME)) {
      // Predefined constants (ex: E_ALL) are of type NamespaceNameTree
      return ((NamespaceNameTree) tree).qualifiedName();
    }
    return getStringValue(tree);
  }

}
