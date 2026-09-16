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
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.sonar.check.Rule;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.php.symbols.ClassSymbol;
import org.sonar.php.symbols.MethodSymbol;
import org.sonar.php.symbols.Symbols;
import org.sonar.php.symbols.Trilean;
import org.sonar.php.symbols.Visibility;
import org.sonar.php.tree.symbols.Scope;
import org.sonar.plugins.php.api.symbols.QualifiedName;
import org.sonar.plugins.php.api.symbols.Symbol;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.ClassDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.DeclaredTypeTree;
import org.sonar.plugins.php.api.tree.declaration.FunctionDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.FunctionTree;
import org.sonar.plugins.php.api.tree.declaration.MethodDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.NamespaceNameTree;
import org.sonar.plugins.php.api.tree.declaration.ParameterTree;
import org.sonar.plugins.php.api.tree.declaration.TypeNameTree;
import org.sonar.plugins.php.api.tree.declaration.TypeTree;
import org.sonar.plugins.php.api.tree.expression.AnonymousClassTree;
import org.sonar.plugins.php.api.tree.expression.ArrowFunctionExpressionTree;
import org.sonar.plugins.php.api.tree.expression.CompoundVariableTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.expression.FunctionExpressionTree;
import org.sonar.plugins.php.api.tree.expression.IdentifierTree;
import org.sonar.plugins.php.api.tree.expression.VariableVariableTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

@Rule(key = UnusedFunctionParametersCheck.KEY)
public class UnusedFunctionParametersCheck extends PHPVisitorCheck {

  public static final String KEY = "S1172";
  private static final String MESSAGE = "Remove the unused function parameter \"%s\".";
  private List<IdentifierTree> constructorPromotedProperties = new ArrayList<>();

  @Override
  public void visitFunctionDeclaration(FunctionDeclarationTree tree) {
    super.visitFunctionDeclaration(tree);
    checkParameters(tree);
  }

  @Override
  public void visitFunctionExpression(FunctionExpressionTree tree) {
    super.visitFunctionExpression(tree);
    checkParameters(tree);
  }

  @Override
  public void visitMethodDeclaration(MethodDeclarationTree tree) {
    super.visitMethodDeclaration(tree);
    if (!isExcluded(tree)) {
      collectConstructorPromotedProperties(tree);
      checkParameters(tree);
    }
    constructorPromotedProperties.clear();
  }

  private void checkParameters(FunctionTree tree) {
    Scope scope = context().symbolTable().getScopeFor(tree);
    if (!canDetermineUnusedParameters(tree, scope)) {
      return;
    }
    List<ParameterTree> parameterTrees = tree.parameters().parameters();
    List<Symbol> parameters = parameterTrees.stream()
      .map(parameter -> context().symbolTable().getSymbol(parameter.variableIdentifier()))
      .toList();

    // Anonymous functions are often passed as callbacks, so their positional signature can be constrained by the callback invoker.
    // Do not report unused parameters before the last used one because removing them would shift argument binding.
    // Still report unused trailing parameters because PHP callbacks can ignore extra positional arguments.
    int lastUsedParameterIndex = tree.is(Tree.Kind.FUNCTION_EXPRESSION) ? lastUsedParameterIndex(parameters) : -1;
    List<ParameterTree> unused = new ArrayList<>();

    for (int i = 0; i < parameters.size(); i++) {
      Symbol symbol = parameters.get(i);
      if (i > lastUsedParameterIndex
        && !isExcluded(symbol)
        && symbol.usages().isEmpty()
        && !constructorPromotedProperties.contains(symbol.declaration())) {
        unused.add(parameterTrees.get(i));
      }
    }

    if (unused.isEmpty()) {
      return;
    }

    List<ParameterTree> parametersToReport = LaravelRouteModelBindingHeuristic.parametersToReport(tree, unused);

    for (ParameterTree parameter : parametersToReport) {
      IdentifierTree declaration = parameter.variableIdentifier().variableExpression();
      context().newIssue(this, declaration, String.format(MESSAGE, declaration.text()));
    }
  }

  private static int lastUsedParameterIndex(List<Symbol> parameters) {
    for (int i = parameters.size() - 1; i >= 0; i--) {
      if (!parameters.get(i).usages().isEmpty()) {
        return i;
      }
    }
    return -1;
  }

  private static boolean canDetermineUnusedParameters(FunctionTree tree, @Nullable Scope scope) {
    if (scope == null || scope.hasUnresolvedCompact()) {
      return false;
    }
    PotentialIndirectParameterAccessVisitor visitor = new PotentialIndirectParameterAccessVisitor();
    tree.body().accept(visitor);
    return !visitor.hasPotentialIndirectParameterAccess;
  }

  private void collectConstructorPromotedProperties(MethodDeclarationTree tree) {
    if (tree.name().text().equalsIgnoreCase("__construct")) {
      constructorPromotedProperties = tree.parameters().parameters().stream()
        .filter(ParameterTree::isPropertyPromotion)
        .map(p -> p.variableIdentifier().variableExpression())
        .collect(Collectors.toList());
    }
  }

  /**
   * Exclude methods from the check that is overriding/implementing a method and are not private.
   * Also exclude Magento plugin methods (before*, around*, after*) with specific signatures.
   * Also exclude magic methods (methods starting with __).
   */
  private static boolean isExcluded(MethodDeclarationTree tree) {
    MethodSymbol methodSymbol = Symbols.get(tree);
    return !tree.body().is(Tree.Kind.BLOCK)
      || !(methodSymbol.isOverriding().isFalse())
      || (methodSymbol.visibility() != Visibility.PRIVATE && methodSymbol.owner().is(ClassSymbol.Kind.ABSTRACT))
      || isMagentoPluginMethod(tree)
      || isMagicMethod(tree);
  }

  /**
   * Check if the method is a Magento plugin method with the expected signature.
   * Magento plugin methods are:
   * - before* methods with at least 3 parameters
   * - around* methods with at least 4 parameters
   * - after* methods with at least 2 parameters
   */
  private static boolean isMagentoPluginMethod(MethodDeclarationTree tree) {
    String methodName = tree.name().text();
    int parameterCount = tree.parameters().parameters().size();

    return (methodName.startsWith("before") && parameterCount >= 3)
      || (methodName.startsWith("around") && parameterCount >= 4)
      || (methodName.startsWith("after") && parameterCount >= 2);
  }

  /**
   * Check if the method is a PHP magic method (excluding __construct which has special handling).
   * Uses the official list of PHP magic methods from CheckUtils.
   * __construct is excluded because it should still check for unused parameters
   * (promoted properties are handled separately).
   */
  private static boolean isMagicMethod(MethodDeclarationTree tree) {
    String methodName = tree.name().text();
    return CheckUtils.MAGIC_METHODS.contains(methodName) && !"__construct".equalsIgnoreCase(methodName);
  }

  private static boolean isExcluded(Symbol symbol) {
    return symbol.name().chars()
      // skip the leading '$'
      .skip(1)
      .allMatch(c -> '_' == c);
  }

  private static class LaravelRouteModelBindingHeuristic {
    private static final QualifiedName LARAVEL_CONTROLLER = QualifiedName.qualifiedName("Illuminate\\Routing\\Controller");
    private static final QualifiedName ELOQUENT_MODEL = QualifiedName.qualifiedName("Illuminate\\Database\\Eloquent\\Model");
    private static final QualifiedName URL_ROUTABLE = QualifiedName.qualifiedName("Illuminate\\Contracts\\Routing\\UrlRoutable");
    private static final QualifiedName LARAVEL_REQUEST = QualifiedName.qualifiedName("Illuminate\\Http\\Request");
    private static final QualifiedName LARAVEL_FORM_REQUEST = QualifiedName.qualifiedName("Illuminate\\Foundation\\Http\\FormRequest");
    private static final Set<String> RESOURCE_ACTIONS = Set.of("index", "show", "create", "store", "edit", "update", "destroy");

    /**
     * Filters out parameters likely consumed by Laravel's implicit route-model binding, using controller, action,
     * and model-type evidence available during single-file analysis.
     */
    private static List<ParameterTree> parametersToReport(FunctionTree tree, List<ParameterTree> unusedParameters) {
      if (!(tree instanceof MethodDeclarationTree method) || !isPotentialControllerAction(method)) {
        return unusedParameters;
      }

      return unusedParameters.stream()
        .filter(parameter -> !isPotentialRouteModelParameter(parameter))
        .toList();
    }

    private static boolean isPotentialRouteModelParameter(ParameterTree parameter) {
      NamespaceNameTree parameterType = classType(parameter);
      if (parameterType == null) {
        return false;
      }

      ClassSymbol typeSymbol = Symbols.getClass(parameterType);
      Trilean isRouteBindable = typeSymbol.isSubTypeOf(ELOQUENT_MODEL, URL_ROUTABLE);
      if (isRouteBindable.isTrue()) {
        return true;
      }

      return isRouteBindable == Trilean.UNKNOWN && hasModelNamespaceSegment(typeSymbol.qualifiedName());
    }

    /**
     * Laravel does not require controllers to extend a framework base class, and route registrations are often in
     * another file. Identify likely controller actions from information available during single-file analysis:
     * - The method is public and non-static.
     * - The declaring class either inherits from {@code Illuminate\Routing\Controller}, or its qualified name
     * contains {@code Http\Controllers} and its simple name ends with {@code Controller}.
     * - The method either has a conventional resource-action name or declares a Request-like parameter.
     * Combining these signals avoids treating arbitrary methods that merely accept an Eloquent model as route
     * actions.
     */
    private static boolean isPotentialControllerAction(MethodDeclarationTree method) {
      MethodSymbol methodSymbol = Symbols.get(method);
      ClassSymbol owner = methodSymbol.owner();
      QualifiedName ownerName = owner.qualifiedName();
      String qualifiedOwnerName = "\\" + ownerName.toString().toLowerCase(Locale.ROOT) + "\\";
      boolean hasControllerRole = owner.isSubTypeOf(LARAVEL_CONTROLLER).isTrue()
        || (ownerName.simpleName().toLowerCase(Locale.ROOT).endsWith("controller")
          && qualifiedOwnerName.contains("\\http\\controllers\\"));

      return methodSymbol.visibility() == Visibility.PUBLIC
        && !CheckUtils.isStatic(method)
        && hasControllerRole
        && (RESOURCE_ACTIONS.contains(method.name().text().toLowerCase(Locale.ROOT))
          || method.parameters().parameters().stream().anyMatch(LaravelRouteModelBindingHeuristic::isRequestLikeParameter));
    }

    private static boolean isRequestLikeParameter(ParameterTree parameter) {
      NamespaceNameTree type = classType(parameter);
      if (type == null) {
        return false;
      }

      ClassSymbol typeSymbol = Symbols.getClass(type);
      Trilean isLaravelRequest = typeSymbol.isSubTypeOf(LARAVEL_REQUEST, LARAVEL_FORM_REQUEST);
      if (isLaravelRequest.isTrue()) {
        return true;
      }

      return isLaravelRequest == Trilean.UNKNOWN
        && typeSymbol.qualifiedName().simpleName().toLowerCase(Locale.ROOT).endsWith("request");
    }

    @Nullable
    private static NamespaceNameTree classType(ParameterTree parameter) {
      DeclaredTypeTree declaredType = parameter.declaredType();
      if (declaredType instanceof TypeTree type) {
        TypeNameTree typeName = type.typeName();
        if (typeName instanceof NamespaceNameTree namespaceName) {
          return namespaceName;
        }
      }
      return null;
    }

    private static boolean hasModelNamespaceSegment(QualifiedName typeName) {
      // In IDE analysis the model declaration is usually unavailable, but its imported qualified name is known.
      String qualifiedName = "\\" + typeName.toString().toLowerCase(Locale.ROOT) + "\\";
      return qualifiedName.contains("\\model\\") || qualifiedName.contains("\\models\\");
    }
  }

  private static class PotentialIndirectParameterAccessVisitor extends PHPVisitorCheck {
    private static final Set<String> FUNCTIONS_WITH_POTENTIAL_INDIRECT_PARAMETER_ACCESS = Set.of(
      "eval",
      // With collision flags such as EXTR_SKIP, extract() observes existing parameters matching array keys.
      "extract",
      "func_get_arg",
      "func_get_args",
      "get_defined_vars",
      "include",
      "include_once",
      "require",
      "require_once");

    private boolean hasPotentialIndirectParameterAccess;

    @Override
    public void visitFunctionCall(FunctionCallTree tree) {
      if (tree.callee() instanceof NamespaceNameTree callee) {
        String functionName = callee.qualifiedName().toLowerCase(Locale.ROOT);
        if (FUNCTIONS_WITH_POTENTIAL_INDIRECT_PARAMETER_ACCESS.contains(functionName)) {
          hasPotentialIndirectParameterAccess = true;
          return;
        }
      }
      super.visitFunctionCall(tree);
    }

    @Override
    public void visitVariableVariable(VariableVariableTree tree) {
      hasPotentialIndirectParameterAccess = true;
    }

    @Override
    public void visitCompoundVariable(CompoundVariableTree tree) {
      // ${foo} directly interpolates $foo; other expressions compute the variable name.
      if (!tree.variableExpression().is(Tree.Kind.NAME_IDENTIFIER, Tree.Kind.NAMESPACE_NAME)) {
        hasPotentialIndirectParameterAccess = true;
      }
    }

    @Override
    public void visitFunctionDeclaration(FunctionDeclarationTree tree) {
      // Do not visit nested functions.
    }

    @Override
    public void visitFunctionExpression(FunctionExpressionTree tree) {
      // Do not visit nested closures.
    }

    @Override
    public void visitArrowFunctionExpression(ArrowFunctionExpressionTree tree) {
      // Do not visit nested arrow functions.
    }

    @Override
    public void visitClassDeclaration(ClassDeclarationTree tree) {
      // Do not visit nested classes.
    }

    @Override
    public void visitAnonymousClass(AnonymousClassTree tree) {
      // Constructor arguments are evaluated in the enclosing scope, class members are not.
      scan(tree.callArguments());
    }
  }
}
