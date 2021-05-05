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
package org.sonar.php.checks;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Deque;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.sonar.check.Rule;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.php.utils.collections.SetUtils;
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
    // Only consider reachable blocks to avoid confusing issues (+Unreachable blocks are reported by S1763)
    Set<CfgBlock> cfgBlocks = getReachableBlocks(cfg);

    Map<CfgBlock, BlockSummary> blockSummaries = new HashMap<>();
    cfgBlocks.forEach(b -> blockSummaries.put(b, getBlockSummary(b)));

    Set<String> providedVariables = getParameterVariableNames(tree);
    if (tree.is(Kind.FUNCTION_EXPRESSION)) {
      providedVariables.addAll(getLexicalVariableNames((FunctionExpressionTree) tree));
    }

    InitialDataCollector initialDataCollector = new InitialDataCollector();
    tree.accept(initialDataCollector);
    // Catch clauses do not appear in the CFG. We collect all exception variables in the function body and
    // consider them as provided in the whole function body to avoid false positives.
    providedVariables.addAll(initialDataCollector.exceptionVariables);

    blockSummaries.get(cfg.start()).stateOnBlockStart.initializedVariables.addAll(providedVariables);

    Deque<CfgBlock> workList = new ArrayDeque<>(cfgBlocks);
    while (!workList.isEmpty()) {
      CfgBlock block = workList.pop();
      BlockSummary summary = blockSummaries.get(block);

      for (CfgBlock successor : block.successors()) {
        StateOnBlockStart stateOnBlockStart = blockSummaries.get(successor).stateOnBlockStart;
        if (!stateOnBlockStart.containsBlockSummary(summary)) {
          stateOnBlockStart.addBlockSummary(summary);
          workList.add(successor);
        }
      }
    }

    Map<String, Set<Tree>> uninitializedVariableUses = new HashMap<>();
    cfgBlocks.forEach(b -> checkBlock(b, blockSummaries.get(b))
      .forEach((v, s) -> uninitializedVariableUses.computeIfAbsent(v, st -> new HashSet<>()).addAll(s)));

    // static variables could be initialized after first usage. We only raise an issue on them when they were never
    // initialized (i.e., not initialized by predecessors of the end block)
    uninitializedVariableUses.entrySet().stream()
      .filter(e -> !isInitializedStaticVariable(e.getKey(),
        initialDataCollector.uninitializedStaticVariables,
        blockSummaries.get(cfg.end())))
      .forEach(e -> reportOnFirstTree(e.getValue()));
  }

  private static boolean isInitializedStaticVariable(String var, Set<String> uninitializedStaticVariables, BlockSummary endBlockSummary) {
    return uninitializedStaticVariables.contains(var) && endBlockSummary.stateOnBlockStart.wasInitialized(var);
  }

  private void reportOnFirstTree(Set<Tree> trees) {
    trees.stream()
      .min(Comparator.comparingInt(a -> ((PHPTree) a).getFirstToken().line())
        .thenComparing(a -> ((PHPTree) a).getFirstToken().column()))
      .ifPresent(t -> newIssue(t, MESSAGE));
  }

  private static Set<CfgBlock> getReachableBlocks(ControlFlowGraph cfg) {
    Set<CfgBlock> result = new HashSet<>();
    result.add(cfg.start());

    Deque<CfgBlock> workList = new ArrayDeque<>(result);
    while (!workList.isEmpty()) {
      CfgBlock item = workList.pop();

      Set<CfgBlock> newSuccessors = item.successors().stream()
        .filter(b -> !result.contains(b))
        .collect(Collectors.toSet());

      result.addAll(newSuccessors);
      workList.addAll(newSuccessors);
    }

    return result;
  }

  private static Map<String, Set<Tree>> checkBlock(CfgBlock block, BlockSummary blockSummary) {
    Map<String, Set<Tree>> result = new HashMap<>();

    UninitializedUsageFindVisitor visitor = new UninitializedUsageFindVisitor(blockSummary.stateOnBlockStart);
    for (Tree element : block.elements()) {
      element.accept(visitor);
    }
    visitor.uninitializedVariableReads.entrySet().stream()
      .filter(e -> !PREDEFINED_VARIABLES.contains(e.getKey().toUpperCase(Locale.ROOT)))
      .forEach(e -> result.computeIfAbsent(e.getKey(), v -> new HashSet<>()).add(e.getValue()));

    return result;
  }

  private static Set<String> getLexicalVariableNames(FunctionExpressionTree tree) {
    Set<String> result = new HashSet<>();

    if (tree.lexicalVars() != null) {
      tree.lexicalVars().variables().stream()
        .map(VariableTree::variableExpression)
        .filter(v -> v.is(Kind.VARIABLE_IDENTIFIER))
        .forEach(v -> result.add(((VariableIdentifierTree) v).variableExpression().text()));
    }

    return result;
  }

  private static Set<String> getParameterVariableNames(FunctionTree tree) {
    return tree.parameters().parameters().stream()
      .map(p -> p.variableIdentifier().variableExpression().text())
      .collect(Collectors.toSet());
  }

  private static Set<String> getForEachVariables(ForEachStatementTree tree) {
    Set<String> result = new HashSet<>();

    if (tree.value().is(Kind.VARIABLE_IDENTIFIER)) {
      result.add(((VariableIdentifierTree) tree.value()).variableExpression().text());
    } else {
      TreeUtils.descendants(tree.value(), VariableIdentifierTree.class)
        .forEach(v -> result.add(v.variableExpression().text()));
    }

    if (tree.key() != null) {
      if (tree.key().is(Kind.VARIABLE_IDENTIFIER)) {
        result.add(((VariableIdentifierTree) tree.key()).variableExpression().text());
      } else {
        TreeUtils.descendants(tree.key(), VariableIdentifierTree.class).forEach(v -> result.add(v.variableExpression().text()));
      }
    }

    return result;
  }

  private static BlockSummary getBlockSummary(CfgBlock block) {
    SummaryCreationVisitor visitor = new SummaryCreationVisitor();
    for (Tree element : block.elements()) {
      element.accept(visitor);
    }
    BlockSummary summary = new BlockSummary(visitor.initializedVariables, visitor.scopeWasChanged);

    if (block instanceof CfgBranchingBlock) {
      Tree branchingTree = ((CfgBranchingBlock) block).branchingTree();
      if (branchingTree.is(Kind.FOREACH_STATEMENT, Kind.ALTERNATIVE_FOREACH_STATEMENT)) {
        summary.initializedVariables.addAll(getForEachVariables((ForEachStatementTree) branchingTree));
      }
    }

    return summary;
  }

  private static class BlockSummary {
    protected final StateOnBlockStart stateOnBlockStart = new StateOnBlockStart();
    protected Set<String> initializedVariables;
    protected boolean scopeWasChangedLocally;

    public BlockSummary(Set<String> initializedVariables, boolean scopeWasChangedLocally) {
      this.initializedVariables = new HashSet<>(initializedVariables);
      this.scopeWasChangedLocally = scopeWasChangedLocally;
    }

    protected Set<String> allVariables() {
      HashSet<String> result = new HashSet<>(initializedVariables);
      result.addAll(stateOnBlockStart.initializedVariables);
      return result;
    }

    private boolean scopeWasChanged() {
      return scopeWasChangedLocally || stateOnBlockStart.scopeWasChanged;
    }
  }

  private static class StateOnBlockStart {
    protected final Set<String> initializedVariables = new HashSet<>();
    protected boolean scopeWasChanged = false;

    protected boolean wasInitialized(String variable) {
      return initializedVariables.contains(variable);
    }

    private boolean containsBlockSummary(BlockSummary blockSummary) {
      return initializedVariables.containsAll(blockSummary.allVariables())
        && scopeWasChanged == blockSummary.scopeWasChanged();
    }

    private void addBlockSummary(BlockSummary blockSummary) {
      initializedVariables.addAll(blockSummary.allVariables());
      scopeWasChanged = scopeWasChanged || blockSummary.scopeWasChanged();
    }
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
    map.put(Kind.CALL_ARGUMENT, tree -> {
      if (!tree.getParent().getParent().is(Kind.FUNCTION_CALL)) {
        return false;
      }
      FunctionCallTree functionCall = (FunctionCallTree) tree.getParent().getParent();
      return tree == functionCall.callee() || FUNCTION_ALLOWING_ARGUMENT_CHECK.contains(CheckUtils.getLowerCaseFunctionName(functionCall));
    });
    map.put(Kind.ARRAY_ACCESS, tree -> !isArrayAssignment(tree));
    map.put(Kind.PARENTHESISED_EXPRESSION, tree -> isReadAccess(tree.getParent()));

    return map;
  }

  private static boolean isArrayAssignment(Tree tree) {
    Tree child = skipParentArrayAccess(tree);
    return child.getParent().is(Kind.ASSIGNMENT) &&
      ((AssignmentExpressionTree) child.getParent()).variable() == child;
  }

  private static Tree skipParentArrayAccess(Tree tree) {
    Tree child = tree;
    while (child.getParent().is(Kind.ARRAY_ACCESS) && ((ArrayAccessTree) child.getParent()).object() == child) {
      child = child.getParent();
    }
    return child;
  }

  private static boolean uninitializedVariableDeclaration(VariableIdentifierTree tree) {
    return (tree.getParent().is(Kind.VARIABLE_DECLARATION) &&
      ((VariableDeclarationTree) tree.getParent()).equalToken() == null);
  }

  private static class UninitializedUsageFindVisitor extends ScopeVisitor {
    private final StateOnBlockStart stateOnBlockStart;
    private final Map<String, Tree> uninitializedVariableReads = new HashMap<>();

    private UninitializedUsageFindVisitor(StateOnBlockStart stateOnBlockStart) {
      this.stateOnBlockStart = stateOnBlockStart;
    }

    @Override
    public void visitVariableIdentifier(VariableIdentifierTree tree) {
      if (isClassMemberAccess(tree) || uninitializedVariableDeclaration(tree)) {
        return;
      }

      String name = tree.variableExpression().text();
      if (!isReadAccess(tree)) {
        initializedVariables.add(name);
      } else if (!isInitializedRead(name)) {
        uninitializedVariableReads.putIfAbsent(name, tree);
      }

      super.visitVariableIdentifier(tree);
    }

    private boolean isInitializedRead(String variable) {
      if (scopeWasChanged || stateOnBlockStart.scopeWasChanged) {
        return true;
      }

      return initializedVariables.contains(variable) || stateOnBlockStart.initializedVariables.contains(variable);
    }
  }


  private static class SummaryCreationVisitor extends ScopeVisitor {
    @Override
    public void visitVariableIdentifier(VariableIdentifierTree tree) {
      if (isClassMemberAccess(tree) || uninitializedVariableDeclaration(tree)) {
        return;
      }

      if (!isReadAccess(tree)) {
        initializedVariables.add(tree.variableExpression().text());
      }

      super.visitVariableIdentifier(tree);
    }
  }

  private abstract static class ScopeVisitor extends PHPVisitorCheck {
    protected final Set<String> initializedVariables = new HashSet<>();
    protected boolean scopeWasChanged = false;

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
        scopeWasChanged = true;
      }
      super.visitFunctionCall(functionCall);
    }


    protected static boolean isClassMemberAccess(Tree tree) {
      Tree child = skipParentArrayAccess(tree);
      return (child.getParent().is(Kind.CLASS_MEMBER_ACCESS) &&
        ((MemberAccessTree) child.getParent()).member() == child);
    }
  }

  private static class InitialDataCollector extends PHPVisitorCheck {
    private final Set<String> exceptionVariables = new HashSet<>();
    private final Set<String> uninitializedStaticVariables = new HashSet<>();
    
    @Override
    public void visitCatchBlock(CatchBlockTree tree) {
      if (tree.variable() != null) {
        exceptionVariables.add(tree.variable().variableExpression().text());
      }
      super.visitCatchBlock(tree);
    }

    @Override
    public void visitVariableIdentifier(VariableIdentifierTree tree) {
      if (uninitializedVariableDeclaration(tree)
        && TreeUtils.findAncestorWithKind(tree, SetUtils.immutableSetOf(Kind.STATIC_STATEMENT)) != null) {
        uninitializedStaticVariables.add(tree.variableExpression().text());
      }
      super.visitVariableIdentifier(tree);
    }
  }
}
