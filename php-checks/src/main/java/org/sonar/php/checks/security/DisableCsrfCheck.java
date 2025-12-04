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
package org.sonar.php.checks.security;

import java.util.Optional;
import javax.annotation.Nullable;
import org.sonar.check.Rule;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.php.symbols.ClassSymbol;
import org.sonar.php.symbols.Symbols;
import org.sonar.php.tree.TreeUtils;
import org.sonar.plugins.php.api.symbols.QualifiedName;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.CallArgumentTree;
import org.sonar.plugins.php.api.tree.declaration.ClassDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.ClassPropertyDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.VariableDeclarationTree;
import org.sonar.plugins.php.api.tree.expression.ArrayInitializerTree;
import org.sonar.plugins.php.api.tree.expression.ArrayPairTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.expression.LiteralTree;
import org.sonar.plugins.php.api.visitors.CheckContext;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;
import org.sonar.plugins.php.api.visitors.PreciseIssue;

import static java.util.Collections.singletonList;
import static org.sonar.php.checks.utils.CheckUtils.argumentIsStringLiteralWithValue;
import static org.sonar.php.checks.utils.CheckUtils.arrayValue;
import static org.sonar.php.checks.utils.CheckUtils.isFalseValue;
import static org.sonar.php.checks.utils.CheckUtils.trimQuotes;
import static org.sonar.plugins.php.api.symbols.QualifiedName.qualifiedName;

@Rule(key = "S4502")
public class DisableCsrfCheck extends PHPVisitorCheck {

  private static final String MESSAGE = "Make sure disabling CSRF protection is safe here.";
  private static final String SECONDARY_MESSAGE = "Setting variable to false.";

  private static final QualifiedName SYMFONY_ABSTRACT_CONTROLLER = qualifiedName("Symfony\\Bundle\\FrameworkBundle\\Controller\\AbstractController");
  private static final QualifiedName SYMFONY_CONTROLLER = qualifiedName("Symfony\\Bundle\\FrameworkBundle\\Controller\\Controller");
  private static final QualifiedName SYMFONY_ABSTRACT_TYPE = qualifiedName("Symfony\\Component\\Form\\AbstractType");
  private static final QualifiedName LARAVEL_CSRF_MIDDLEWARE = qualifiedName("Illuminate\\Foundation\\Http\\Middleware\\VerifyCsrfToken");

  private static final Tree.Kind[] ARRAY = {Tree.Kind.ARRAY_INITIALIZER_BRACKET, Tree.Kind.ARRAY_INITIALIZER_FUNCTION};

  @Override
  public void visitFunctionCall(FunctionCallTree tree) {
    Call call = new Call(tree, context());
    if (call.hasName("createForm") && call.inClass(SYMFONY_ABSTRACT_CONTROLLER, SYMFONY_CONTROLLER)) {
      call.argument("options", 2).ifPresent(this::checkCsrfInSymfonyOptions);
    } else if (call.hasName("setDefaults") && call.inClass(SYMFONY_ABSTRACT_TYPE)) {
      call.argument("defaults", 0).ifPresent(this::checkCsrfInSymfonyOptions);
    } else if (call.inPath("config/packages")) {
      checkCsrfInSymfonyConfig(call);
    } else if (call.hasName("prependExtensionConfig") || call.hasName("loadFromExtension")) {
      checkCsrfInSymfonyExtensionConfig(call);
    }
    super.visitFunctionCall(tree);
  }

  @Override
  public void visitClassPropertyDeclaration(ClassPropertyDeclarationTree tree) {
    tree.declarations().stream()
      .map(Property::new)
      .filter(Property::isException)
      .filter(Property::isLaravelMiddleware)
      .filter(Property::hasExceptions)
      .findFirst()
      .ifPresent(p -> context().newIssue(this, tree, MESSAGE));

    super.visitClassPropertyDeclaration(tree);
  }

  private void checkCsrfInSymfonyConfig(Call call) {
    if (call.hasName("set")) {
      checkCsrfInSymfonyParametersConfig(call);
    } else if (call.hasName("extension")) {
      checkCsrfInSymfonyExtensionConfig(call);
    }
  }

  private void checkCsrfInSymfonyParametersConfig(Call call) {
    if (call.argument("name", 0).filter(a -> argumentIsStringLiteralWithValue(a, "csrf_protection")).isPresent()) {
      call.argument("value", 1).map(CallArgumentTree::value).ifPresent(this::raiseIssueIfFalse);
    }
  }

  private void checkCsrfInSymfonyExtensionConfig(Call call) {
    if (call.argument("extension", 0).filter(a -> argumentIsStringLiteralWithValue(a, "framework")).isPresent()) {
      call.argument("values", 1).ifPresent(this::checkCsrfInSymfonyOptions);
    }
  }

  private void checkCsrfInSymfonyOptions(CallArgumentTree argument) {
    if (argument.value().is(ARRAY)) {
      arrayValue((ArrayInitializerTree) argument.value(), "csrf_protection")
        .ifPresent(this::raiseIssueIfFalse);
    }
  }

  private void raiseIssueIfFalse(ExpressionTree value) {
    ExpressionTree assignedValue = CheckUtils.assignedValue(value);
    if (isDisabled(assignedValue)) {
      PreciseIssue issue = context().newIssue(this, value.getParent(), MESSAGE);
      if (assignedValue != value) {
        issue.secondary(assignedValue.getParent(), SECONDARY_MESSAGE);
      }
    }
  }

  // In Symfony config null is equal to true
  public static boolean isDisabled(ExpressionTree tree) {
    return !tree.is(Tree.Kind.NULL_LITERAL) && isFalseValue(tree);
  }

  private abstract static class Component {
    Tree tree;
    String name;

    public Component(Tree tree, @Nullable String name) {
      this.tree = tree;
      this.name = name;
    }

    protected boolean inClass(QualifiedName... typeNames) {
      Tree classDeclaration = TreeUtils.findAncestorWithKind(tree, singletonList(Tree.Kind.CLASS_DECLARATION));
      if (classDeclaration != null) {
        ClassSymbol classSymbol = Symbols.get((ClassDeclarationTree) classDeclaration);
        if (classSymbol != null) {
          return classSymbol.isSubTypeOf(typeNames).isTrue();
        }
      }
      return false;
    }
  }

  private static class Property extends Component {
    public Property(VariableDeclarationTree tree) {
      super(tree, tree.identifier().text());
    }

    public boolean isException() {
      return "$except".equals(name);
    }

    public boolean isLaravelMiddleware() {
      return inClass(LARAVEL_CSRF_MIDDLEWARE);
    }

    public boolean hasExceptions() {
      ExpressionTree initValue = ((VariableDeclarationTree) tree).initValue();
      return initValue != null && initValue.is(ARRAY) && ((ArrayInitializerTree) initValue).arrayPairs().stream().map(ArrayPairTree::value)
        .anyMatch(v -> !v.is(Tree.Kind.REGULAR_STRING_LITERAL) || !"".equals(trimQuotes((LiteralTree) v)));
    }
  }

  private static class Call extends Component {
    CheckContext context;

    public Call(FunctionCallTree tree, CheckContext context) {
      super(tree, CheckUtils.functionName(tree));
      this.context = context;
    }

    public Optional<CallArgumentTree> argument(String name, int position) {
      return CheckUtils.argument((FunctionCallTree) tree, name, position);
    }

    public boolean hasName(String expectedName) {
      return expectedName.equalsIgnoreCase(name);
    }

    public boolean inPath(String path) {
      return context.getPhpFile().uri().getPath().contains(path);
    }
  }
}
