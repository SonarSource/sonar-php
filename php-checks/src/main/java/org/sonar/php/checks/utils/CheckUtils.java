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
package org.sonar.php.checks.utils;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.sonar.php.symbols.ClassSymbol;
import org.sonar.php.symbols.Symbols;
import org.sonar.php.tree.TreeUtils;
import org.sonar.php.tree.impl.VariableIdentifierTreeImpl;
import org.sonar.php.tree.symbols.SymbolImpl;
import org.sonar.php.utils.collections.MapBuilder;
import org.sonar.plugins.php.api.symbols.QualifiedName;
import org.sonar.plugins.php.api.symbols.Symbol;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.declaration.CallArgumentTree;
import org.sonar.plugins.php.api.tree.declaration.ClassDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.ClassMemberTree;
import org.sonar.plugins.php.api.tree.declaration.ClassPropertyDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.FunctionDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.FunctionTree;
import org.sonar.plugins.php.api.tree.declaration.MethodDeclarationTree;
import org.sonar.plugins.php.api.tree.expression.ArrayInitializerTree;
import org.sonar.plugins.php.api.tree.expression.ArrayPairTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.expression.LiteralTree;
import org.sonar.plugins.php.api.tree.expression.MemberAccessTree;
import org.sonar.plugins.php.api.tree.expression.NewExpressionTree;
import org.sonar.plugins.php.api.tree.expression.ParenthesisedExpressionTree;
import org.sonar.plugins.php.api.tree.expression.VariableIdentifierTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.tree.statement.ForStatementTree;
import org.sonar.plugins.php.api.tree.statement.InlineHTMLTree;
import org.sonar.plugins.php.api.visitors.PhpFile;

import static java.util.Collections.singletonList;
import static org.sonar.php.symbols.Symbols.get;
import static org.sonar.php.tree.TreeUtils.findAncestorWithKind;
import static org.sonar.plugins.php.api.symbols.QualifiedName.qualifiedName;

public final class CheckUtils {

  private static final Kind[] FUNCTION_KINDS_ARRAY = {
    Kind.METHOD_DECLARATION,
    Kind.FUNCTION_DECLARATION,
    Kind.FUNCTION_EXPRESSION,
    Kind.ARROW_FUNCTION_EXPRESSION};

  static final List<Kind> FUNCTION_KINDS = Arrays.asList(FUNCTION_KINDS_ARRAY);

  static final Map<String, String> SUPERGLOBALS_BY_OLD_NAME = MapBuilder.<String, String>builder()
    .put("$HTTP_SERVER_VARS", "$_SERVER")
    .put("$HTTP_GET_VARS", "$_GET")
    .put("$HTTP_POST_VARS", "$_POST")
    .put("$HTTP_POST_FILES", "$_FILES")
    .put("$HTTP_SESSION_VARS", "$_SESSION")
    .put("$HTTP_ENV_VARS", "$_ENV")
    .put("$HTTP_COOKIE_VARS", "$_COOKIE")
    .build();

  public static final Set<String> SUPERGLOBALS = Set.of(
    "$GLOBALS", "$_SERVER", "$_GET", "$_POST", "$_FILES", "$_COOKIE", "$_SESSION", "$_REQUEST", "$_ENV");

  /**
   * Official PHP magic methods.
   */
  public static final Set<String> MAGIC_METHODS = Set.of(
    "__construct", "__destruct", "__call", "__callStatic", "__get",
    "__set", "__isset", "__unset", "__sleep", "__wakeup", "__toString", "__invoke",
    "__set_state", "__clone", "__debugInfo");

  private CheckUtils() {
  }

  public static Map<String, String> getSuperGlobalsByOldName() {
    return SUPERGLOBALS_BY_OLD_NAME;
  }

  public static List<Kind> getFunctionKinds() {
    return FUNCTION_KINDS;
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
   * @return Returns function, static method's or known dynamic method's name, like "f" or "A::f". Warning, use case insensitive
   * comparison of the result.
   */
  @Nullable
  public static String getFunctionName(FunctionCallTree functionCall) {
    return nameOf(functionCall.callee());
  }

  /**
   * @return Returns function, static method's or known dynamic method's lower case name, like "f" or "a::f".
   */
  @Nullable
  public static String getLowerCaseFunctionName(FunctionCallTree functionCall) {
    String name = getFunctionName(functionCall);
    return name != null ? name.toLowerCase(Locale.ROOT) : null;
  }

  /**
   * @return Returns function or method's name without receiver, like "fooBar".
   */
  @Nullable
  public static String functionName(FunctionCallTree functionCall) {
    return TreeUtils.functionName(functionCall);
  }

  /**
   * @return Returns function or method's lower case name without receiver, like "foobar".
   */
  @Nullable
  public static String lowerCaseFunctionName(FunctionCallTree functionCall) {
    String name = functionName(functionCall);
    return name != null ? name.toLowerCase(Locale.ROOT) : null;
  }

  public static Set<String> lowerCaseSet(String... names) {
    return Arrays.stream(names).map(name -> name.toLowerCase(Locale.ROOT)).collect(Collectors.toSet());
  }

  public static String getClassName(ClassDeclarationTree classDeclaration) {
    return Objects.requireNonNull(nameOf(classDeclaration.name()));
  }

  public static String getLowerCaseClassName(ClassDeclarationTree classDeclarationTree) {
    return getClassName(classDeclarationTree).toLowerCase(Locale.ROOT);
  }

  /**
   * @return Returns the name of a tree.
   */
  @Nullable
  public static String nameOf(Tree tree) {
    return TreeUtils.nameOf(tree);
  }

  public static boolean isExitExpression(FunctionCallTree functionCallTree) {
    String callee = functionCallTree.callee().toString();
    return "die".equalsIgnoreCase(callee) || "exit".equalsIgnoreCase(callee);
  }

  public static boolean hasModifier(ClassMemberTree tree, String toFind) {
    if (tree.is(Kind.METHOD_DECLARATION)) {
      return hasModifier(((MethodDeclarationTree) tree).modifiers(), toFind);
    } else if (tree.is(Kind.CLASS_PROPERTY_DECLARATION)) {
      return hasModifier(((ClassPropertyDeclarationTree) tree).modifierTokens(), toFind);
    }
    return false;
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

  public static boolean isClosingTag(Tree tree) {
    if (tree.is(Kind.INLINE_HTML)) {
      return isClosingTag(((InlineHTMLTree) tree).inlineHTMLToken());
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
  public static ExpressionTree getForCondition(ForStatementTree tree) {
    if (tree.condition().isEmpty()) {
      return null;
    }
    // in a loop, all conditions are evaluated but only the last one is used as the result
    return tree.condition().get(tree.condition().size() - 1);
  }

  public static String trimQuotes(String value) {
    if (value.length() > 1 && (value.startsWith("'") || value.startsWith("\""))) {
      return value.substring(1, value.length() - 1);
    }
    return value;
  }

  public static String trimQuotes(LiteralTree literalTree) {
    return TreeUtils.trimQuotes(literalTree);
  }

  /**
   * <a href="http://php.net/manual/en/language.types.boolean.php">PHP boolean</a>
   *
   * @param tree
   * @return true if {@code tree} represents false boolean value
   */
  public static boolean isFalseValue(ExpressionTree tree) {
    if (tree.is(Tree.Kind.BOOLEAN_LITERAL, Kind.NUMERIC_LITERAL)) {
      String value = ((LiteralTree) tree).value();
      return "false".equalsIgnoreCase(value)
        || "0".equals(value)
        || "0.0".equals(value);
    }
    if (tree.is(Kind.REGULAR_STRING_LITERAL)) {
      String value = trimQuotes(((LiteralTree) tree).value());
      return value == null || value.isEmpty() || "0".equals(value);
    }
    return tree.is(Kind.NULL_LITERAL);
  }

  /**
   * @see #isFalseValue(ExpressionTree)
   */
  public static boolean isTrueValue(ExpressionTree tree) {
    return tree.is(Kind.BOOLEAN_LITERAL, Kind.NUMERIC_LITERAL, Kind.REGULAR_STRING_LITERAL, Kind.NULL_LITERAL)
      && !isFalseValue(tree);
  }

  public static boolean isStringLiteralWithValue(@Nullable Tree tree, String s) {
    return tree != null && tree.is(Kind.REGULAR_STRING_LITERAL) && s.equalsIgnoreCase(trimQuotes((LiteralTree) tree));
  }

  public static boolean argumentIsStringLiteralWithValue(CallArgumentTree argument, String s) {
    return isStringLiteralWithValue(assignedValue(argument.value()), s);
  }

  public static boolean isNullOrEmptyString(ExpressionTree tree) {
    if (tree.is(Kind.NULL_LITERAL)) {
      return true;
    }
    if (tree.is(Tree.Kind.REGULAR_STRING_LITERAL)) {
      String value = CheckUtils.trimQuotes(((LiteralTree) tree).value());
      return value.trim().isEmpty();
    }
    return false;
  }

  public static boolean isPublic(ClassMemberTree tree) {
    return !tree.is(Kind.USE_TRAIT_DECLARATION) && !(hasModifier(tree, "private") || hasModifier(tree, "protected"));
  }

  public static boolean isStatic(ClassMemberTree tree) {
    return hasModifier(tree, "static");
  }

  /**
   * Retrieves an argument based on position and name.
   *
   * If an argument with the given name exists, it is returned no matter the position.
   * Else, the argument at the supplied position is returned if it exists and is not named.
   *
   * @since 3.11
   */
  public static Optional<CallArgumentTree> argument(FunctionCallTree call, String name, int position) {
    return TreeUtils.argument(call, name, position);
  }

  public static Optional<LiteralTree> resolvedArgumentLiteral(FunctionCallTree call, String name, int position) {
    return argumentValue(call, name, position)
      .map(CheckUtils::assignedValue)
      .filter(LiteralTree.class::isInstance)
      .map(LiteralTree.class::cast);
  }

  public static Optional<ExpressionTree> argumentValue(FunctionCallTree call, String name, int position) {
    return argument(call, name, position).map(CallArgumentTree::value);
  }

  public static List<ExpressionTree> argumentsOfKind(FunctionCallTree call, Kind kind) {
    return call.callArguments().stream()
      .map(CallArgumentTree::value)
      .filter(arg -> arg.is(kind))
      .toList();
  }

  public static boolean hasNamedArgument(FunctionCallTree call) {
    return call.callArguments().stream().anyMatch(arg -> arg.name() != null);
  }

  /**
   * Checks if an expression is a variable, and returns the assigned value of the variable if clear, otherwise returns the original
   * expression.
   * This method is well suited if you want to compare the value of an expression with a scalar. It can resolve possible variables in the
   * small scope.
   * Thus, it is not necessary to consider beforehand whether the expression is already a scalar expression, a variable, or an expression
   * that is not to be treated.
   */
  public static ExpressionTree assignedValue(ExpressionTree tree) {
    if (tree.is(Kind.VARIABLE_IDENTIFIER)) {
      return CheckUtils.uniqueAssignedValue((VariableIdentifierTree) tree).orElse(tree);
    }
    return tree;
  }

  public static Optional<ExpressionTree> uniqueAssignedValue(VariableIdentifierTree tree) {
    Symbol symbol = ((VariableIdentifierTreeImpl) tree).symbol();
    if (symbol != null) {
      return ((SymbolImpl) symbol).uniqueAssignedValue();
    }
    return Optional.empty();
  }

  public static Optional<String> resolveReceiver(MemberAccessTree tree) {
    ExpressionTree receiver = skipParenthesis(assignedValue(tree.object()));
    if (receiver.is(Kind.NEW_EXPRESSION)) {
      ExpressionTree newExpression = ((NewExpressionTree) receiver).expression();
      return Optional.ofNullable(newExpression.is(Kind.FUNCTION_CALL) ? functionName((FunctionCallTree) newExpression) : nameOf(newExpression));
    }
    return Optional.empty();
  }

  public static Optional<ExpressionTree> arrayValue(ArrayInitializerTree array, String searchKey) {
    return arrayValue(array.arrayPairs(), searchKey);
  }

  public static Optional<ExpressionTree> arrayValue(List<ArrayPairTree> arrayPairs, String searchKey) {
    for (ArrayPairTree arrayPair : arrayPairs) {
      ExpressionTree key = arrayPair.key();
      if (key != null && key.is(Kind.REGULAR_STRING_LITERAL) && searchKey.equals(trimQuotes((LiteralTree) key))) {
        return Optional.of(arrayPair.value());
      }
    }
    return Optional.empty();
  }

  public static boolean isMethodInheritedFromClassOrInterface(QualifiedName qualifiedName, MethodDeclarationTree methodDeclarationTree) {
    ClassDeclarationTree classTree = (ClassDeclarationTree) findAncestorWithKind(methodDeclarationTree,
      singletonList(Tree.Kind.CLASS_DECLARATION));
    if (classTree != null) {
      return get(classTree).isSubTypeOf(qualifiedName).isTrue();
    }
    return false;
  }

  public static boolean isSubClassOfTestCase(ClassDeclarationTree tree) {
    ClassSymbol symbol = Symbols.get(tree);
    return symbol.isSubTypeOf(qualifiedName("PHPUnit\\Framework\\TestCase")).isTrue()
      || symbol.isSubTypeOf(qualifiedName("PHPUnit_Framework_TestCase")).isTrue();
  }

  public static boolean isEmptyArrayConstructor(ExpressionTree arg) {
    return (arg.is(Tree.Kind.ARRAY_INITIALIZER_FUNCTION, Tree.Kind.ARRAY_INITIALIZER_BRACKET) &&
      ((ArrayInitializerTree) arg).arrayPairs().isEmpty());
  }
}
