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
package org.sonar.php.tree.symbols;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.sonar.php.api.PHPKeyword;
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.php.utils.SourceBuilder;
import org.sonar.plugins.php.api.symbols.Symbol;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.tree.SeparatedList;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.declaration.ClassDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.ClassMemberTree;
import org.sonar.plugins.php.api.tree.declaration.ClassPropertyDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.ClassTree;
import org.sonar.plugins.php.api.tree.declaration.ConstantDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.FunctionDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.MethodDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.NamespaceNameTree;
import org.sonar.plugins.php.api.tree.declaration.ParameterTree;
import org.sonar.plugins.php.api.tree.declaration.VariableDeclarationTree;
import org.sonar.plugins.php.api.tree.expression.AnonymousClassTree;
import org.sonar.plugins.php.api.tree.expression.CompoundVariableTree;
import org.sonar.plugins.php.api.tree.expression.ComputedVariableTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.expression.FunctionExpressionTree;
import org.sonar.plugins.php.api.tree.expression.IdentifierTree;
import org.sonar.plugins.php.api.tree.expression.LexicalVariablesTree;
import org.sonar.plugins.php.api.tree.expression.LiteralTree;
import org.sonar.plugins.php.api.tree.expression.MemberAccessTree;
import org.sonar.plugins.php.api.tree.expression.NameIdentifierTree;
import org.sonar.plugins.php.api.tree.expression.VariableIdentifierTree;
import org.sonar.plugins.php.api.tree.expression.VariableTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.tree.statement.GlobalStatementTree;
import org.sonar.plugins.php.api.tree.statement.StaticStatementTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

public class SymbolVisitor extends PHPVisitorCheck {

  private static final Set<String> BUILT_IN_VARIABLES = ImmutableSet.of(
    "$THIS",
    "$GLOBALS",
    "$_SERVER",
    "$_GET",
    "$_POST",
    "$_FILES",
    "$_SESSION",
    "$_ENV",
    "$PHP_ERRORMSG",
    "$HTTP_RAW_POST_DATA",
    "$HTTP_RESPONSE_HEADER",
    "$ARGC",
    "$ARGV",
    "$_COOKIE",
    "$_REQUEST"
  );

  private Scope classScope = null;
  private Map<Symbol, Scope> scopeBySymbol = new HashMap<>();
  private static final ImmutableSet<String> SELF_OBJECTS = ImmutableSet.of("$this", "self", "static");

  static class ClassMemberUsageState {
    boolean isStatic = false;

    boolean isField = false;

    boolean isSelfMember = false;

    boolean isConst = false;
  }

  private SymbolTableImpl symbolTable;
  private Scope currentScope;
  private Scope globalScope;
  private ClassMemberUsageState classMemberUsageState = null;


  public SymbolVisitor(SymbolTableImpl symbolTable) {
    this.symbolTable = symbolTable;
    this.currentScope = null;
    this.globalScope = null;
  }

  @Override
  public void visitCompilationUnit(CompilationUnitTree tree) {
    enterScope(tree);
    globalScope = currentScope;
    super.visitCompilationUnit(tree);
  }

  @Override
  public void visitFunctionDeclaration(FunctionDeclarationTree tree) {
    createSymbol(tree.name(), Symbol.Kind.FUNCTION);
    enterScope(tree);
    super.visitFunctionDeclaration(tree);
    leaveScope();
  }

  @Override
  public void visitFunctionExpression(FunctionExpressionTree tree) {
    enterScope(tree);
    super.visitFunctionExpression(tree);
    leaveScope();
  }

  @Override
  public void visitMethodDeclaration(MethodDeclarationTree tree) {
    enterScope(tree);
    super.visitMethodDeclaration(tree);
    leaveScope();
  }

  @Override
  public void visitClassPropertyDeclaration(ClassPropertyDeclarationTree tree) {
    // do nothing as this symbols already saved during visiting class tree
  }

  @Override
  public void visitClassDeclaration(ClassDeclarationTree tree) {
    Symbol classSymbol = createSymbol(tree.name(), Symbol.Kind.CLASS);
    enterScope(tree);
    classScope = currentScope;
    scan(tree.name());
    NamespaceNameTree superClass = tree.superClass();
    if(superClass != null) {
      scan(superClass.namespaces());
      Symbol superClassSymbol = resolveSymbol(superClass.name());
      classScope.superClassScope = scopeBySymbol.get(superClassSymbol);
    }
    scopeBySymbol.put(classSymbol, classScope);
    scan(tree.superInterfaces());
    createMemberSymbols(tree);
    scan(tree.members());
    classScope = null;
    leaveScope();
  }

  @Override
  public void visitAnonymousClass(AnonymousClassTree tree) {
    // the arguments are passed from the outer scope
    scan(tree.arguments());

    enterScope(tree);
    classScope = currentScope;
    createMemberSymbols(tree);

    // we've already scanned the arguments
    NamespaceNameTree superClass = tree.superClass();
    if (superClass != null) {
      scan(superClass);
    }
    scan(tree.superInterfaces());
    scan(tree.members());

    classScope = null;
    leaveScope();
  }

  private void createMemberSymbols(ClassTree tree) {
    for (ClassMemberTree member : tree.members()) {
      if (member.is(Kind.METHOD_DECLARATION)) {
        createSymbol(((MethodDeclarationTree) member).name(), Symbol.Kind.FUNCTION).addModifiers(((MethodDeclarationTree) member).modifiers());

      } else if (member.is(Kind.CLASS_CONSTANT_PROPERTY_DECLARATION, Kind.CLASS_PROPERTY_DECLARATION)) {
        ClassPropertyDeclarationTree classPropertyDeclaration = (ClassPropertyDeclarationTree) member;
        for (VariableDeclarationTree field : classPropertyDeclaration.declarations()) {
          createSymbol(field.identifier(), Symbol.Kind.FIELD).addModifiers(classPropertyDeclaration.modifierTokens());
          ExpressionTree initValue = field.initValue();
          if(initValue != null) {
            initValue.accept(this);
          }
        }
      }

    }
  }

  @Override
  public void visitConstDeclaration(ConstantDeclarationTree tree) {
    for (VariableDeclarationTree constant : tree.declarations()) {
      createSymbol(constant.identifier(), Symbol.Kind.VARIABLE).addModifiers(Lists.newArrayList(tree.constToken()));
    }
  }

  @Override
  public void visitVariableIdentifier(VariableIdentifierTree tree) {
    if (!isBuiltInVariable(tree)) {
      if (classMemberUsageState == null) {
        createOrUseVariableIdentifierSymbol(tree);
      } else {

        if (classMemberUsageState.isSelfMember && classScope != null && classMemberUsageState.isStatic) {
          Symbol symbol = classScope.getSymbol(tree.text(), Symbol.Kind.FIELD);
          if (symbol != null) {
            associateSymbol(tree, symbol);
          }
        }

        // see test_property_name_in_variable and case $this->$key ($key stores the name of field)
        Symbol symbol = currentScope.getSymbol(tree.text(), Symbol.Kind.VARIABLE, Symbol.Kind.PARAMETER);
        if (symbol != null) {
          associateSymbol(tree, symbol);
        }

        classMemberUsageState = null;
      }
    }
  }

  private void createOrUseVariableIdentifierSymbol(VariableIdentifierTree identifier) {
    Symbol symbol = currentScope.getSymbol(identifier.text(), Symbol.Kind.PARAMETER);
    if (symbol == null) {
      createSymbol(identifier, Symbol.Kind.VARIABLE);
      return;
    }
    associateSymbol(identifier, symbol);
  }


  @Override
  public void visitToken(SyntaxToken token) {
    if (classMemberUsageState != null && classMemberUsageState.isStatic && token.text().equals(PHPKeyword.CLASS.getValue())){
      classMemberUsageState = null;
    }

    super.visitToken(token);
  }

  @Override
  public void visitNameIdentifier(NameIdentifierTree tree) {
    if (classMemberUsageState != null && classScope != null) {
      resolveProperty(tree);
    } else {
      resolveSymbol(tree);
    }
    classMemberUsageState = null;
  }

  private Symbol resolveSymbol(IdentifierTree tree) {
    Symbol symbol = null;
    Scope outer = currentScope;
    while (outer != null) {
      symbol = outer.getSymbol(tree.text(), Symbol.Kind.CLASS);
      if (symbol != null) {
        associateSymbol(tree, symbol);
        break;
      }
      outer = outer.outer();
    }
    return symbol;
  }

  private void resolveProperty(NameIdentifierTree tree) {
    String name = tree.text();
    Symbol.Kind kind = Symbol.Kind.FUNCTION;
    if (classMemberUsageState.isField) {
      name = (classMemberUsageState.isConst ? "" : "$") + name;
      kind = Symbol.Kind.FIELD;
    }
    Symbol symbol = classScope.getSymbol(name, kind);
    if (symbol != null) {
      associateSymbol(tree, symbol);
    }
  }

  private static boolean isBuiltInVariable(VariableIdentifierTree tree) {
    return BUILT_IN_VARIABLES.contains(tree.text().toUpperCase(Locale.ENGLISH));
  }

  @Override
  public void visitCompoundVariable(CompoundVariableTree tree) {
    SyntaxToken firstExpressionToken = ((PHPTree) tree.variableExpression()).getFirstToken();
    if (firstExpressionToken.text().charAt(0) != '$') {
      Symbol symbol = currentScope.getSymbol("$" + firstExpressionToken.text());
      if (symbol != null) {
        associateSymbol(firstExpressionToken, symbol);
      }
    }

    super.visitCompoundVariable(tree);
  }

  @Override
  public void visitParameter(ParameterTree tree) {
    createSymbol(tree.variableIdentifier(), Symbol.Kind.PARAMETER);
    ExpressionTree initValue = tree.initValue();
    if(initValue != null) {
      initValue.accept(this);
    }
    // do not scan the children to not pass through variableIdentifier
  }

  @Override
  public void visitGlobalStatement(GlobalStatementTree tree) {
    for (VariableTree variable : tree.variables()) {
      // Other cases are not supported
      if (variable.is(Tree.Kind.VARIABLE_IDENTIFIER)) {
        IdentifierTree identifier = (IdentifierTree) variable.variableExpression();
        Symbol symbol = globalScope.getSymbol(identifier.text(), Symbol.Kind.VARIABLE);
        if (symbol != null) {
          // actually this identifier in global statement is not usage, but we do this for the symbol highlighting
          associateSymbol(identifier, symbol);
          currentScope.addSymbol(symbol);
        } else {
          symbol = createSymbol(identifier, Symbol.Kind.VARIABLE);
        }
        // consider 'global' has being a modifier for the variable
        symbol.addModifiers(Collections.singletonList(tree.globalToken()));

      }
    }
  }

  @Override
  public void visitStaticStatement(StaticStatementTree tree) {
    // first visit declarations to create symbols which may not have been used yet
    super.visitStaticStatement(tree);
    // consider 'static' has being a modifier for the variables
    for (VariableDeclarationTree variable : tree.variables()) {
      // FIXME SONARPHP-741: can generate inconsistencies if the variable has already
      // been declared static in another statement, or if its global
      Symbol symbol = currentScope.getSymbol(variable.identifier().text(), Symbol.Kind.VARIABLE);
      if (symbol != null) {
        symbol.addModifiers(Collections.singletonList(tree.staticToken()));
      }
    }
  }

  @Override
  public void visitLexicalVariables(LexicalVariablesTree tree) {
    for (VariableTree variable : tree.variables()) {
      IdentifierTree identifier = null;
      if (variable.is(Tree.Kind.VARIABLE_IDENTIFIER)) {
        identifier = (IdentifierTree) variable.variableExpression();
      } else if (variable.is(Tree.Kind.REFERENCE_VARIABLE) && variable.variableExpression().is(Tree.Kind.VARIABLE_IDENTIFIER)) {
        identifier = ((VariableIdentifierTree) variable.variableExpression()).variableExpression();
      }

      if (identifier != null) {
        Symbol symbol = currentScope.outer().getSymbol(identifier.text());
        if (symbol != null) {
          associateSymbol(identifier, symbol);
        } else if (variable.is(Kind.REFERENCE_VARIABLE)) {
          symbolTable.declareSymbol(identifier, Symbol.Kind.VARIABLE, currentScope.outer());
        }
        createSymbol(identifier, Symbol.Kind.VARIABLE);
      }
    }

  }

  @Override
  public void visitFunctionCall(FunctionCallTree tree) {
    if (tree.callee().is(Tree.Kind.NAMESPACE_NAME)) {
      NamespaceNameTree namespaceNameCallee = (NamespaceNameTree) tree.callee();
      usageForNamespaceName(namespaceNameCallee, Symbol.Kind.FUNCTION);

    }

    tree.callee().accept(this);

    String callee = SourceBuilder.build(tree.callee()).trim();
    if ("compact".equals(callee) || "\\compact".equals(callee)) {
      visitCompactFunctionCall(tree.arguments());
    }

    scan(tree.arguments());
  }

  /**
   * See <a href="http://php.net/manual/en/function.compact.php">docs</a> of "compact" function
   * @param arguments of call of "compact" function
   */
  private void visitCompactFunctionCall(SeparatedList<ExpressionTree> arguments) {
    for (ExpressionTree argument : arguments) {

      if (argument.is(Kind.REGULAR_STRING_LITERAL)) {
        String value = ((LiteralTree) argument).value();
        String variableName = "$" + value.substring(1, value.length() - 1);

        Symbol symbol = currentScope.getSymbol(variableName, Symbol.Kind.VARIABLE, Symbol.Kind.PARAMETER);

        if (symbol != null) {
          associateSymbol(((LiteralTree) argument).token(), symbol);
        }
      } else {
        // argument is array which can contain references to any variable
        currentScope.setUnresolvedCompact(true);
      }
    }
  }

  private void usageForNamespaceName(NamespaceNameTree namespaceName, Symbol.Kind kind) {
    if (namespaceName.name().is(Tree.Kind.NAME_IDENTIFIER) && namespaceName.namespaces().isEmpty()) {
      NameIdentifierTree usageIdentifier = (NameIdentifierTree) namespaceName.name();
      Symbol symbol = currentScope.getSymbol(usageIdentifier.text(), kind);
      if (symbol != null) {
        associateSymbol(usageIdentifier, symbol);
      }

    }
  }

  @Override
  public void visitMemberAccess(MemberAccessTree tree) {
    boolean functionCall = tree.getParent().is(Kind.FUNCTION_CALL) && ((FunctionCallTree) tree.getParent()).callee() == tree;
    tree.object().accept(this);

    classMemberUsageState = new ClassMemberUsageState();
    classMemberUsageState.isStatic = tree.isStatic();
    classMemberUsageState.isSelfMember = isSelfMember(tree);
    classMemberUsageState.isField = !functionCall;
    classMemberUsageState.isConst = classMemberUsageState.isField && tree.isStatic();

    tree.member().accept(this);
  }

  private static boolean isSelfMember(MemberAccessTree tree) {
    String strObject = SourceBuilder.build(tree.object()).trim();
    return SELF_OBJECTS.contains(strObject.toLowerCase(Locale.ENGLISH));
  }

  @Override
  public void visitComputedVariable(ComputedVariableTree tree) {
    classMemberUsageState = null;
    super.visitComputedVariable(tree);
  }

  private void leaveScope() {
    Preconditions.checkState(currentScope != null, "Current scope should never be null when calling method \"leaveScope\"");
    currentScope = currentScope.outer();
  }

  private void enterScope(Tree tree) {
    currentScope = new Scope(currentScope, tree);
    symbolTable.addScope(currentScope);
  }

  private Symbol createSymbol(IdentifierTree identifier, Symbol.Kind kind) {
    Symbol symbol = currentScope.getSymbol(identifier.text(), kind);

    if (symbol == null) {
      symbol = symbolTable.declareSymbol(identifier, kind, currentScope);
      symbolTable.associateSymbol(identifier, symbol);
    } else {
      associateSymbol(identifier, symbol);
    }
    return symbol;
  }

  private void associateSymbol(IdentifierTree tree, Symbol symbol) {
    symbol.addUsage(tree);
    symbolTable.associateSymbol(tree, symbol);
  }
  private void associateSymbol(SyntaxToken token, Symbol symbol) {
    symbol.addUsage(token);
    symbolTable.associateSymbol(token, symbol);
  }


}
