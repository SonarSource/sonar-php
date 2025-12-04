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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.php.symbols.ClassSymbol;
import org.sonar.php.symbols.Symbols;
import org.sonar.php.tree.TreeUtils;
import org.sonar.php.tree.impl.VariableIdentifierTreeImpl;
import org.sonar.plugins.php.api.symbols.QualifiedName;
import org.sonar.plugins.php.api.symbols.Symbol;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.CallArgumentTree;
import org.sonar.plugins.php.api.tree.declaration.ClassDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.MethodDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.NamespaceNameTree;
import org.sonar.plugins.php.api.tree.declaration.ParameterTree;
import org.sonar.plugins.php.api.tree.declaration.TypeNameTree;
import org.sonar.plugins.php.api.tree.declaration.TypeTree;
import org.sonar.plugins.php.api.tree.expression.ArrayInitializerTree;
import org.sonar.plugins.php.api.tree.expression.ArrayPairTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.expression.LiteralTree;
import org.sonar.plugins.php.api.tree.expression.MemberAccessTree;
import org.sonar.plugins.php.api.tree.expression.NewExpressionTree;
import org.sonar.plugins.php.api.tree.statement.ReturnStatementTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

import static java.util.Collections.singletonList;
import static org.sonar.php.checks.utils.CheckUtils.arrayValue;
import static org.sonar.php.tree.TreeUtils.descendants;
import static org.sonar.plugins.php.api.symbols.QualifiedName.qualifiedName;

@Rule(key = "S5693")
public class RequestContentLengthCheck extends PHPVisitorCheck {
  public static final int DEFAULT = 8_000_000;
  @RuleProperty(
    key = "fileUploadSizeLimit",
    defaultValue = "" + DEFAULT)
  long fileUploadSizeLimit = DEFAULT;

  private static final QualifiedName SYMFONY_FILE_CONSTRAINT = qualifiedName("Symfony\\Component\\Validator\\Constraints\\File");
  private static final Pattern SIZE_FORMAT = Pattern.compile("^(?<number>[0-9]+)(?<unit>k|M|Ki|Mi)?$");
  private static final Pattern LARAVEL_SIZE_FORMAT = Pattern.compile("^max:(?<size>[0-9]+)$");
  private static final Tree.Kind[] ARRAY = {Tree.Kind.ARRAY_INITIALIZER_BRACKET, Tree.Kind.ARRAY_INITIALIZER_FUNCTION};

  private static final QualifiedName LARAVEL_FORM_REQUEST = qualifiedName("Illuminate\\Foundation\\Http\\FormRequest");
  private static final QualifiedName LARAVEL_CONTROLLER = qualifiedName("App\\Http\\Controllers\\Controller");
  private static final String LARAVEL_RULES_KEY = "rules";
  private static final Map<String, Integer> LARAVEL_RULES_ARGUMENT = new HashMap<>();
  static {
    LARAVEL_RULES_ARGUMENT.put("illuminate\\http\\request::validate", 0);
    LARAVEL_RULES_ARGUMENT.put("illuminate\\http\\request::validatewithbag", 1);
    LARAVEL_RULES_ARGUMENT.put("validator", 1);
    LARAVEL_RULES_ARGUMENT.put("illuminate\\support\\facades\\validator::make", 1);
  }

  private static final String MESSAGE = "Make sure the content length limit is safe here.";

  @Override
  public void visitNewExpression(NewExpressionTree tree) {
    if (isInstantiationOf(tree, SYMFONY_FILE_CONSTRAINT) && ((FunctionCallTree) tree.expression()).callArguments().size() == 1) {
      checkSymfonyFileConstraint(((FunctionCallTree) tree.expression()).callArguments().get(0).value());
    }
    super.visitNewExpression(tree);
  }

  @Override
  public void visitFunctionCall(FunctionCallTree tree) {
    super.visitFunctionCall(tree);

    String fullFunctionName = tree.callee().is(Tree.Kind.NAMESPACE_NAME) ? CheckUtils.getLowerCaseFunctionName(tree) : getMethodName(tree);

    if (fullFunctionName == null) {
      return;
    }

    getLaravelValidationsArgument(tree, fullFunctionName)
      .map(CallArgumentTree::value)
      .ifPresent(this::checkLaravelFileRules);
  }

  @Override
  public void visitMethodDeclaration(MethodDeclarationTree tree) {
    super.visitMethodDeclaration(tree);
    if (CheckUtils.isMethodInheritedFromClassOrInterface(LARAVEL_FORM_REQUEST, tree) && LARAVEL_RULES_KEY.equalsIgnoreCase(tree.name().text())) {
      checkLaravelFormRequestRulesReturns(tree);
    }
  }

  private void checkLaravelFormRequestRulesReturns(MethodDeclarationTree tree) {
    descendants(tree, ReturnStatementTree.class)
      .map(ReturnStatementTree::expression)
      .filter(Objects::nonNull)
      .map(CheckUtils::assignedValue)
      .forEach(this::checkLaravelFileRules);
  }

  private void checkLaravelFileRules(ExpressionTree tree) {
    if (!tree.is(ARRAY)) {
      return;
    }

    getLaravelFileValidations((ArrayInitializerTree) tree)
      .filter(v -> v.isMaxHigher(fileUploadSizeLimit))
      .forEach(v -> context().newIssue(this, v.definition, MESSAGE));
  }

  private String getMethodName(FunctionCallTree tree) {
    ExpressionTree callee = tree.callee();
    String className = null;

    if (callee.is(Tree.Kind.CLASS_MEMBER_ACCESS) && (((MemberAccessTree) callee).object().is(Tree.Kind.NAMESPACE_NAME))) {
      className = getFullyQualifiedName((NamespaceNameTree) ((MemberAccessTree) callee).object()).toString();
    } else if (callee.is(Tree.Kind.OBJECT_MEMBER_ACCESS) && (((MemberAccessTree) callee).object() instanceof VariableIdentifierTreeImpl receiver)) {
      Symbol receiverSymbol = receiver.symbol();
      if (receiverSymbol == null) {
        return null;
      }

      Tree receiverDeclarationParent = receiverSymbol.declaration().getParent();
      if (!receiverDeclarationParent.is(Tree.Kind.PARAMETER)) {
        return null;
      }

      className = parameterType((ParameterTree) receiverDeclarationParent);
    }

    if (className == null) {
      return null;
    }

    return (className + "::" + CheckUtils.functionName(tree)).toLowerCase(Locale.ROOT);
  }

  private static Optional<CallArgumentTree> getLaravelValidationsArgument(FunctionCallTree tree, String fullFunctionName) {
    if (LARAVEL_RULES_ARGUMENT.containsKey(fullFunctionName)
      && !("validator".equals(fullFunctionName) && !isInLaravelController(tree))) {
      return CheckUtils.argument(tree, LARAVEL_RULES_KEY, LARAVEL_RULES_ARGUMENT.get(fullFunctionName));
    }

    return Optional.empty();
  }

  private static boolean isInLaravelController(FunctionCallTree tree) {
    Tree classDeclaration = TreeUtils.findAncestorWithKind(tree, singletonList(Tree.Kind.CLASS_DECLARATION));
    if (classDeclaration != null) {
      ClassSymbol classSymbol = Symbols.get((ClassDeclarationTree) classDeclaration);
      if (classSymbol != null) {
        return classSymbol.isSubTypeOf(LARAVEL_CONTROLLER).isTrue();
      }
    }
    return false;
  }

  private static Stream<LaravelFileValidation> getLaravelFileValidations(ArrayInitializerTree tree) {
    return tree.arrayPairs().stream()
      .map(ArrayPairTree::value)
      .map(LaravelFileValidation::of)
      .filter(Objects::nonNull);
  }

  private static class LaravelFileValidation {
    ExpressionTree definition;
    Long maxBytes;

    public LaravelFileValidation(ExpressionTree definition, @Nullable Long maxBytes) {
      this.definition = definition;
      this.maxBytes = maxBytes;
    }

    private static @Nullable LaravelFileValidation of(ExpressionTree tree) {
      Set<String> validationValues = new HashSet<>();

      if (tree.is(Tree.Kind.REGULAR_STRING_LITERAL)) {
        Collections.addAll(validationValues, CheckUtils.trimQuotes((LiteralTree) tree).split("\\|"));
      } else if (tree.is(ARRAY)) {
        validationValues = ((ArrayInitializerTree) tree).arrayPairs().stream()
          .map(ArrayPairTree::value)
          .filter(v -> v.is(Tree.Kind.REGULAR_STRING_LITERAL))
          .map(v -> CheckUtils.trimQuotes((LiteralTree) v))
          .collect(Collectors.toSet());
      }

      return ofArray(tree, validationValues);
    }

    private static @Nullable LaravelFileValidation ofArray(ExpressionTree definition, Set<String> validationValues) {
      if (!validationValues.contains("file")) {
        return null;
      }

      Long size = validationValues.stream()
        .map(LARAVEL_SIZE_FORMAT::matcher)
        .filter(Matcher::matches)
        .map(m -> Long.parseLong(m.group("size")) * 1000)
        .findFirst().orElse(null);

      return new LaravelFileValidation(definition, size);
    }

    public boolean isMaxHigher(long v) {
      return maxBytes == null || maxBytes > v;
    }
  }

  private @Nullable String parameterType(ParameterTree parameter) {
    if (parameter.declaredType() != null && parameter.declaredType().isSimple()) {
      TypeNameTree typeNameTree = ((TypeTree) parameter.declaredType()).typeName();
      if (typeNameTree.is(Tree.Kind.NAMESPACE_NAME)) {
        return getFullyQualifiedName((NamespaceNameTree) typeNameTree).toString();
      }
    }

    return null;
  }

  private void checkSymfonyFileConstraint(ExpressionTree tree) {
    ExpressionTree value = CheckUtils.assignedValue(tree);
    if (!value.is(ARRAY)) {
      return;
    }

    Optional<ExpressionTree> setSize = arrayValue((ArrayInitializerTree) value, "maxSize");
    if (setSize.isPresent()) {
      checkFileSize(setSize.get());
    } else {
      context().newIssue(this, value, MESSAGE);
    }
  }

  private void checkFileSize(ExpressionTree size) {
    ExpressionTree sizeValue = CheckUtils.assignedValue(size);
    long setBytes = 0;

    if (sizeValue.is(Tree.Kind.NUMERIC_LITERAL, Tree.Kind.REGULAR_STRING_LITERAL)) {
      Matcher matcher = SIZE_FORMAT.matcher(sizeValue.is(Tree.Kind.REGULAR_STRING_LITERAL) ? CheckUtils.trimQuotes((LiteralTree) sizeValue) : ((LiteralTree) sizeValue).value());
      if (!matcher.matches()) {
        return;
      }

      setBytes = Long.parseLong(matcher.group("number"));

      String unit = matcher.group("unit") != null ? matcher.group("unit") : "";
      switch (unit) {
        case "k":
          setBytes *= 1000;
          break;
        case "M":
          setBytes *= 1_000_000;
          break;
        case "Ki":
          setBytes *= 1024;
          break;
        case "Mi":
          setBytes *= 1_048_576;
          break;
        default:
      }
    } else if (sizeValue.is(Tree.Kind.NULL_LITERAL)) {
      // null means unlimited
      setBytes = fileUploadSizeLimit + 1;
    }

    if (setBytes > fileUploadSizeLimit) {
      context().newIssue(this, sizeValue, MESSAGE);
    }
  }

  private boolean isInstantiationOf(NewExpressionTree tree, QualifiedName name) {
    ExpressionTree expression = tree.expression();
    if (!expression.is(Tree.Kind.FUNCTION_CALL) || !((FunctionCallTree) expression).callee().is(Tree.Kind.NAMESPACE_NAME)) {
      return false;
    }

    return name.equals(getFullyQualifiedName((NamespaceNameTree) ((FunctionCallTree) expression).callee()));
  }
}
