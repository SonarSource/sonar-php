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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;
import org.sonar.check.Rule;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.php.symbols.ClassSymbol;
import org.sonar.php.tree.TreeUtils;
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.php.tree.impl.declaration.ClassNamespaceNameTreeImpl;
import org.sonar.php.tree.impl.expression.FunctionCallTreeImpl;
import org.sonar.php.tree.symbols.HasClassSymbol;
import org.sonar.plugins.php.api.symbols.Symbol;
import org.sonar.plugins.php.api.symbols.Symbol.Kind;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.ClassDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.ClassMemberTree;
import org.sonar.plugins.php.api.tree.declaration.ClassTree;
import org.sonar.plugins.php.api.tree.declaration.MethodDeclarationTree;
import org.sonar.plugins.php.api.tree.expression.AnonymousClassTree;
import org.sonar.plugins.php.api.tree.expression.CallableConvertTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.expression.IdentifierTree;
import org.sonar.plugins.php.api.tree.expression.LiteralTree;
import org.sonar.plugins.php.api.tree.expression.MemberAccessTree;
import org.sonar.plugins.php.api.tree.expression.NameIdentifierTree;
import org.sonar.plugins.php.api.tree.expression.VariableIdentifierTree;
import org.sonar.plugins.php.api.tree.statement.UseTraitDeclarationTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

import static org.sonar.plugins.php.api.tree.Tree.Kind.USE_TRAIT_DECLARATION;
import static org.sonar.plugins.php.api.tree.Tree.Kind.VARIABLE_IDENTIFIER;

@Rule(key = "S1144")
public class UnusedPrivateMethodCheck extends PHPVisitorCheck {

  private static final String MESSAGE = "Remove this unused private \"%s\" method.";
  private static final Pattern USES_PHPDOC_PATTERN = Pattern.compile("@uses\\s+(\\w+::)?(\\w+)");

  private static final List<String> CALL_USER_FUNCTIONS = List.of("call_user_func_array", "call_user_func");

  private final List<String> stringLiterals = new ArrayList<>();
  private final List<String> dynamicUsedMethods = new ArrayList<>();
  private final List<String> methodsUsedInFirstClassCallables = new ArrayList<>();

  @Override
  public void visitClassDeclaration(ClassDeclarationTree tree) {
    stringLiterals.clear();
    dynamicUsedMethods.clear();
    methodsUsedInFirstClassCallables.clear();
    super.visitClassDeclaration(tree);

    if (tree.is(Tree.Kind.CLASS_DECLARATION, Tree.Kind.ENUM_DECLARATION)) {
      checkClass(tree);
    }
  }

  @Override
  public void visitMethodDeclaration(MethodDeclarationTree tree) {
    ((PHPTree) tree).getFirstToken().trivias().stream()
      .flatMap(trivia -> USES_PHPDOC_PATTERN.matcher(trivia.text()).results())
      .map(result -> result.group(2))
      .map(methodName -> methodName.toLowerCase(Locale.ROOT))
      .forEach(dynamicUsedMethods::add);

    super.visitMethodDeclaration(tree);
  }

  @Override
  public void visitCallableConvert(CallableConvertTree tree) {
    if (tree.expression().is(Tree.Kind.OBJECT_MEMBER_ACCESS)) {
      var memberAccessTree = (MemberAccessTree) tree.expression();
      if (isThis(memberAccessTree.object()) && memberAccessTree.member().is(Tree.Kind.NAME_IDENTIFIER)) {
        methodsUsedInFirstClassCallables.add(((NameIdentifierTree) memberAccessTree.member()).text().toLowerCase(Locale.ROOT));
      }
    }

    super.visitCallableConvert(tree);
  }

  private void checkClass(ClassTree tree) {
    var classScope = context().symbolTable().getScopeFor(tree);
    for (Symbol methodSymbol : classScope.getSymbols(Kind.FUNCTION)) {

      // For enums private and protected are equivalent as inheritance is not allowed.
      boolean isUnusedMethodWithNarrowVisibility = (methodSymbol.hasModifier("private") || isProtectedEnumMethod(tree, methodSymbol)) &&
        methodSymbol.usages().isEmpty();

      if (isUnusedMethodWithNarrowVisibility
        && !dynamicUsedMethods.contains(methodSymbol.name())
        && !methodsUsedInFirstClassCallables.contains(methodSymbol.name())
        && !isConstructor(methodSymbol.declaration(), tree)
        && !isMagicMethod(methodSymbol.name())
        && !isUsedInStringLiteral(methodSymbol)
        && !isMagicMethodCallDefined(tree)) {
        context().newIssue(this, methodSymbol.declaration(), String.format(MESSAGE, methodSymbol.name()));
      }
    }
  }

  private static boolean isMagicMethodCallDefined(ClassTree tree) {
    if (tree instanceof ClassDeclarationTree classDeclaration) {
      Optional<ClassMemberTree> magicMethodCall = findMagicMethodCall(classDeclaration);
      if (magicMethodCall.isPresent() && containsCallUserFunction(magicMethodCall.get())) {
        return true;
      }
      Optional<ClassSymbol> superClass = ((HasClassSymbol) tree).symbol().superClass();
      if (superClass.isPresent() && containsSuperClassCallMethod(superClass.get())) {
        return true;
      }
      return containsTraitWithMagicMethodCall(tree);
    }
    return false;
  }

  private static Optional<ClassMemberTree> findMagicMethodCall(ClassDeclarationTree tree) {
    return tree.members().stream()
      .filter(MethodDeclarationTree.class::isInstance)
      .filter(method -> "__call".equals(((MethodDeclarationTree) method).name().text()))
      .findFirst();
  }

  private static boolean containsCallUserFunction(ClassMemberTree magicMethodCall) {
    return TreeUtils.descendants(magicMethodCall, FunctionCallTree.class)
      .anyMatch(expression -> {
        String functionName = ((FunctionCallTreeImpl) expression).symbol().qualifiedName().simpleName().toLowerCase(Locale.ROOT);
        return CALL_USER_FUNCTIONS.contains(functionName);
      });
  }

  private static boolean containsSuperClassCallMethod(ClassSymbol tree) {
    if (containsMagicMethodCall(tree)) {
      return true;
    }
    Optional<ClassSymbol> superClass = tree.superClass();
    if (superClass.isPresent()) {
      return containsSuperClassCallMethod(superClass.get());
    }
    return false;
  }

  private static boolean containsTraitWithMagicMethodCall(ClassTree tree) {
    return tree.members().stream()
      .filter(member -> member.is(USE_TRAIT_DECLARATION))
      .map(member -> ((UseTraitDeclarationTree) member).traits())
      .flatMap(List::stream)
      .filter(ClassNamespaceNameTreeImpl.class::isInstance)
      .map(trait -> ((ClassNamespaceNameTreeImpl) trait).symbol())
      .anyMatch(UnusedPrivateMethodCheck::containsMagicMethodCall);
  }

  private static boolean containsMagicMethodCall(ClassSymbol tree) {
    return tree.declaredMethods().stream()
      .anyMatch(m -> m.name().startsWith("__call"));
  }

  @Override
  public void visitAnonymousClass(AnonymousClassTree tree) {
    stringLiterals.clear();
    dynamicUsedMethods.clear();
    methodsUsedInFirstClassCallables.clear();
    super.visitAnonymousClass(tree);

    checkClass(tree);
  }

  @Override
  public void visitLiteral(LiteralTree tree) {
    if (tree.is(Tree.Kind.REGULAR_STRING_LITERAL)) {
      stringLiterals.add(CheckUtils.trimQuotes(tree).toLowerCase(Locale.ROOT));
    }
  }

  private static boolean isProtectedEnumMethod(ClassTree tree, Symbol methodSymbol) {
    return tree.is(Tree.Kind.ENUM_DECLARATION) && methodSymbol.hasModifier("protected");
  }

  private boolean isUsedInStringLiteral(Symbol methodSymbol) {
    for (String stringLiteral : stringLiterals) {
      if (stringLiteral.contains(methodSymbol.name())) {
        return true;
      }
    }
    return false;
  }

  private static boolean isConstructor(IdentifierTree methodName, ClassTree classDec) {
    MethodDeclarationTree constructor = classDec.fetchConstructor();
    return constructor != null && constructor.name().equals(methodName);
  }

  private static boolean isMagicMethod(String methodName) {
    return methodName.startsWith("__");
  }

  private static boolean isThis(Tree object) {
    return object.is(VARIABLE_IDENTIFIER) && "$this".equals(((VariableIdentifierTree) object).text());
  }
}
