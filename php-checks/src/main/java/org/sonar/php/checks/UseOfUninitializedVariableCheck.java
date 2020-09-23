/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2020 SonarSource SA
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
package org.sonar.php.checks;

import org.sonar.check.Rule;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.php.tree.TreeUtils;
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.plugins.php.api.cfg.CfgBlock;
import org.sonar.plugins.php.api.cfg.CfgBranchingBlock;
import org.sonar.plugins.php.api.cfg.ControlFlowGraph;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.declaration.FunctionDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.FunctionTree;
import org.sonar.plugins.php.api.tree.declaration.MethodDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.VariableDeclarationTree;
import org.sonar.plugins.php.api.tree.expression.ArrayAccessTree;
import org.sonar.plugins.php.api.tree.expression.AssignmentExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.expression.FunctionExpressionTree;
import org.sonar.plugins.php.api.tree.expression.MemberAccessTree;
import org.sonar.plugins.php.api.tree.expression.VariableIdentifierTree;
import org.sonar.plugins.php.api.tree.expression.VariableTree;
import org.sonar.plugins.php.api.tree.statement.CatchBlockTree;
import org.sonar.plugins.php.api.tree.statement.ForEachStatementTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Rule(key = "S836")
public class UseOfUninitializedVariableCheck extends PHPVisitorCheck {
  private static final String MESSAGE = "Review the data-flow - use of uninitialized value.";

  private static final Set<Kind> PARENT_INITIALIZATION_KIND = EnumSet.of(
    // Note: LEXICAL_VARIABLES are both, read and write, see VariableVisitor#visitFunctionExpression
    Kind.PARAMETER,
    Kind.GLOBAL_STATEMENT,
    Kind.VARIABLE_DECLARATION,
    Kind.REFERENCE_VARIABLE,
    Kind.ARRAY_ASSIGNMENT_PATTERN_ELEMENT,
    Kind.UNSET_VARIABLE_STATEMENT,
    // CatchBlockTree#variable
    Kind.CATCH_BLOCK,
    Kind.ASSIGNMENT_BY_REFERENCE);

  private static final Set<String> FUNCTION_CHANGING_CURRENT_SCOPE = new HashSet<>(Arrays.asList(
    "eval",
    "extract",
    "parse_str",
    // PREG_REPLACE_EVAL option was deprecated in php 5.5 and has been removed in php 7.0
    "preg_replace",
    "include",
    "include_once",
    "require",
    "require_once"));

  // Note: "$argc" and "$argv" are not available in the function scope without using "global"
  private static final Set<String> PREDEFINED_VARIABLES = new HashSet<>(Arrays.asList(
    "$_COOKIE",
    "$_ENV",
    "$_FILES",
    "$_GET",
    "$_POST",
    "$_REQUEST",
    "$_SERVER",
    "$_SESSION",
    "$GLOBALS",
    "$HTTP_RAW_POST_DATA",
    "$HTTP_RESPONSE_HEADER",
    "$PHP_ERRORMSG",
    // "$this" is defined only in method, but rule S2014 raises issues when it's used elsewhere
    "$THIS"));

  private static final Set<String> FUNCTION_ALLOWING_ARGUMENT_CHECK;
  static {
    FUNCTION_ALLOWING_ARGUMENT_CHECK = new HashSet<>(IgnoredReturnValueCheck.PURE_FUNCTIONS);
    FUNCTION_ALLOWING_ARGUMENT_CHECK.add("echo");
    FUNCTION_ALLOWING_ARGUMENT_CHECK.remove("isset");
  }

  @Override
  public void visitFunctionDeclaration(FunctionDeclarationTree tree) {
    checkFunction(tree);
    super.visitFunctionDeclaration(tree);
  }

  @Override
  public void visitMethodDeclaration(MethodDeclarationTree tree) {
    checkFunction(tree);
    super.visitMethodDeclaration(tree);
  }

  @Override
  public void visitFunctionExpression(FunctionExpressionTree tree) {
    checkFunction(tree);
    super.visitFunctionExpression(tree);
  }

  private void checkFunction(FunctionTree tree) {
    ControlFlowGraph cfg = ControlFlowGraph.build(tree, context());
    if (cfg == null) {
      return;
    }

    Set<String> providedVariables = getParameterVariableNames(tree);
    if (tree.is(Kind.FUNCTION_EXPRESSION)) {
      providedVariables.addAll(getLexicalVariableNames((FunctionExpressionTree) tree));
    }

    // Catch clauses do not appear in the CFG. We collect all exception variables in the function body and consider them as provided in the whole function body to avoid false positives.
    ExceptionVariablesExtractor exceptionVariablesExtractor = new ExceptionVariablesExtractor();
    tree.accept(exceptionVariablesExtractor);
    providedVariables.addAll(exceptionVariablesExtractor.variables);

    Map<CfgBlock, BlockSummary> predecessorsSummary = new HashMap<>();
    cfg.blocks().forEach(
      b -> predecessorsSummary.put(b, new BlockSummary(new HashSet<>(providedVariables), false))
    );
    ArrayDeque<CfgBlock> workList = new ArrayDeque<>(cfg.blocks());

    while (!workList.isEmpty()) {
      CfgBlock block = workList.pop();

      BlockSummary init = BlockSummary.copyOf(predecessorsSummary.get(block));
      BlockSummary changesInBlock = initializedInThisBlock(block);
      init.initializedVariables.addAll(changesInBlock.initializedVariables);
      init.scopeWasChanged = changesInBlock.scopeWasChanged || init.scopeWasChanged;

      for (CfgBlock successor : block.successors()) {
        BlockSummary successorInit = predecessorsSummary.get(successor);
        if (!successorInit.initializedVariables.containsAll(init.initializedVariables)
          || successorInit.scopeWasChanged != init.scopeWasChanged) {
          successorInit.initializedVariables.addAll(init.initializedVariables);
          successorInit.scopeWasChanged = successorInit.scopeWasChanged || init.scopeWasChanged;
          workList.add(successor);
        }
      }
    }

    Map<String, Set<Tree>> uninitializedVariableUses = new HashMap<>();
    cfg.blocks().forEach(b -> checkBlock(b, predecessorsSummary.get(b))
      .forEach((v, s) -> uninitializedVariableUses.computeIfAbsent(v, st -> new HashSet<>()).addAll(s)));
    uninitializedVariableUses.forEach((v, trees) -> reportOnFirstTree(trees));
  }

  private void reportOnFirstTree(Set<Tree> trees) {
    trees.stream()
      .min(Comparator.comparingInt(a -> ((PHPTree)a).getFirstToken().line())
        .thenComparing(a -> ((PHPTree)a).getFirstToken().column()))
      .ifPresent(t -> newIssue(t, MESSAGE));
  }

  private Set<String> getLexicalVariableNames(FunctionExpressionTree tree) {
    Set<String> result = new HashSet<>();

    if (tree.lexicalVars() != null) {
      tree.lexicalVars().variables().stream()
        .map(VariableTree::variableExpression)
        .filter(v -> v.is(Kind.VARIABLE_IDENTIFIER))
        .forEach(v -> result.add(((VariableIdentifierTree)v).variableExpression().text()));
    }

    return result;
  }

  private Set<String> getParameterVariableNames(FunctionTree tree) {
    return tree.parameters().parameters().stream()
      .map(p -> p.variableIdentifier().variableExpression().text())
      .collect(Collectors.toSet());
  }

  private Map<String, Set<Tree>> checkBlock(CfgBlock block, BlockSummary blockSummary) {
    Map<String, Set<Tree>> result = new HashMap<>();

    BlockSummary summary = BlockSummary.copyOf(blockSummary);
    for (Tree element : block.elements()) {
      StatementVisitor visitor = new StatementVisitor(summary);
      element.accept(visitor);
      visitor.firstVariableReadAccess.entrySet().stream()
        .filter(e -> !PREDEFINED_VARIABLES.contains(e.getKey().toUpperCase()))
        .forEach(e -> result.computeIfAbsent(e.getKey(), v -> new HashSet<>()).add(e.getValue()));
    }

    return result;
  }

  private BlockSummary initializedInThisBlock(CfgBlock block) {
    BlockSummary summary = new BlockSummary();

    for (Tree element : block.elements()) {
      StatementVisitor visitor = new StatementVisitor(summary);
      element.accept(visitor);
    }

    if (block instanceof CfgBranchingBlock) {
      Tree branchingTree = ((CfgBranchingBlock)block).branchingTree();
      if (branchingTree.is(Kind.FOREACH_STATEMENT, Kind.ALTERNATIVE_FOREACH_STATEMENT)) {
        summary.initializedVariables.addAll(getForEachVariables((ForEachStatementTree)branchingTree));
      }
    }

    return summary;
  }

  private static class BlockSummary {
    private final Set<String> initializedVariables;
    private boolean scopeWasChanged;

    private BlockSummary(Set<String> initializedVariables, boolean scopeWasChanged) {
      this.initializedVariables = initializedVariables;
      this.scopeWasChanged = scopeWasChanged;
    }

    private BlockSummary() {
      this.initializedVariables = new HashSet<>();
      this.scopeWasChanged = false;
    }

    private static BlockSummary copyOf(BlockSummary toCopy) {
      return new BlockSummary(new HashSet<>(toCopy.initializedVariables), toCopy.scopeWasChanged);
    }
  }

  private Set<String> getForEachVariables(ForEachStatementTree tree) {
    Set<String> result = new HashSet<>();

    if (tree.value() != null) {
      if (tree.value().is(Kind.VARIABLE_IDENTIFIER)) {
        result.add(((VariableIdentifierTree)tree.value()).variableExpression().text());
      } else {
        TreeUtils.descendants(tree.value(), VariableIdentifierTree.class)
          .forEach(v -> result.add(v.variableExpression().text()));
      }
    }

    if (tree.key() != null) {
      if (tree.key().is(Kind.VARIABLE_IDENTIFIER)) {
        result.add(((VariableIdentifierTree)tree.key()).variableExpression().text());
      } else {
        TreeUtils.descendants(tree.key(), VariableIdentifierTree.class).forEach(v -> result.add(v.variableExpression().text()));
      }
    }

    return result;
  }

  private static boolean isReadAccess(Tree tree) {
    Predicate<Tree> predicate = IS_READ_ACCESS_BY_PARENT_KIND.get(tree.getParent().getKind());
    return predicate == null || predicate.test(tree);
  }

  private static final Map<Kind, Predicate<Tree>> IS_READ_ACCESS_BY_PARENT_KIND = initializeReadPredicate();

  private static Map<Kind, Predicate<Tree>> initializeReadPredicate() {
    Map<Kind, Predicate<Tree>> map = new EnumMap<>(Kind.class);

    PARENT_INITIALIZATION_KIND.forEach(kind -> map.put(kind, tree -> false));
    map.put(Kind.ASSIGNMENT, tree -> tree == ((AssignmentExpressionTree) tree.getParent()).value());
    map.put(Kind.FUNCTION_CALL, tree -> {
      FunctionCallTree functionCall = (FunctionCallTree) tree.getParent();
      return tree == functionCall.callee() || FUNCTION_ALLOWING_ARGUMENT_CHECK.contains(CheckUtils.getLowerCaseFunctionName(functionCall));
    });
    map.put(Kind.FOREACH_STATEMENT, UseOfUninitializedVariableCheck::isInsideForEachExpression);
    map.put(Kind.ALTERNATIVE_FOREACH_STATEMENT, UseOfUninitializedVariableCheck::isInsideForEachExpression);
    map.put(Kind.ARRAY_ACCESS, tree -> !isArrayAssignment(tree));
    map.put(Kind.PARENTHESISED_EXPRESSION, tree -> isReadAccess(tree.getParent()));

    return map;
  }

  private static boolean isArrayAssignment(Tree tree) {
    Tree child = skipParentArrayAccess(tree);
    return child.getParent().is(Kind.ASSIGNMENT) &&
      ((AssignmentExpressionTree) child.getParent()).variable() == child;
  }

  private static boolean isInsideForEachExpression(Tree tree) {
    return tree == ((ForEachStatementTree) tree.getParent()).expression();
  }

  private static Tree skipParentArrayAccess(Tree tree) {
    Tree child = tree;
    while (child.getParent().is(Kind.ARRAY_ACCESS) && ((ArrayAccessTree) child.getParent()).object() == child) {
      child = child.getParent();
    }
    return child;
  }

  private static class StatementVisitor extends PHPVisitorCheck {
    private final BlockSummary blockSummary;
    private final Map<String, Tree> firstVariableReadAccess = new HashMap<>();

    private StatementVisitor(BlockSummary blockSummary) {
      this.blockSummary = blockSummary;
    }

    @Override
    public void visitFunctionDeclaration(FunctionDeclarationTree tree) {
      // do not visit nested functions
    }

    @Override
    public void visitFunctionExpression(FunctionExpressionTree tree) {
      if (tree.lexicalVars() != null) {
        tree.lexicalVars().accept(this);
      }
      // do not visit nested body
    }

    @Override
    public void visitFunctionCall(FunctionCallTree functionCall) {
      if (FUNCTION_CHANGING_CURRENT_SCOPE.contains(CheckUtils.getLowerCaseFunctionName(functionCall))) {
        blockSummary.scopeWasChanged = true;
      }
      super.visitFunctionCall(functionCall);
    }

    @Override
    public void visitVariableIdentifier(VariableIdentifierTree tree) {
      if (isClassMemberAccess(tree)) {
        return;
      }

      String name = tree.variableExpression().text();
      if (isReadAccess(tree)
        && !blockSummary.initializedVariables.contains(name)
        && !blockSummary.scopeWasChanged) {
        firstVariableReadAccess.putIfAbsent(name, tree);
      } else {
        blockSummary.initializedVariables.add(name);
      }

      super.visitVariableIdentifier(tree);
    }

    private static boolean isClassMemberAccess(Tree tree) {
      Tree child = skipParentArrayAccess(tree);
      return (child.getParent().is(Kind.CLASS_MEMBER_ACCESS) &&
              ((MemberAccessTree) child.getParent()).member() == child);
    }

    private static boolean uninitializedVariableDeclaration(Tree tree) {
      return (tree.getParent().is(Kind.VARIABLE_DECLARATION) &&
              ((VariableDeclarationTree) tree.getParent()).equalToken() == null);
    }
  }

  private static class ExceptionVariablesExtractor extends PHPVisitorCheck {
    private final Set<String> variables = new HashSet<>();

    @Override
    public void visitCatchBlock(CatchBlockTree tree) {
      variables.add(tree.variable().variableExpression().text());
      super.visitCatchBlock(tree);
    }
  }
}
