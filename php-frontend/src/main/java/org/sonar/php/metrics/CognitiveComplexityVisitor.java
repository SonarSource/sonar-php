package org.sonar.php.metrics;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.declaration.FunctionDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.FunctionTree;
import org.sonar.plugins.php.api.tree.declaration.MethodDeclarationTree;
import org.sonar.plugins.php.api.tree.expression.BinaryExpressionTree;
import org.sonar.plugins.php.api.tree.expression.ConditionalExpressionTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionExpressionTree;
import org.sonar.plugins.php.api.tree.expression.ParenthesisedExpressionTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.tree.statement.BreakStatementTree;
import org.sonar.plugins.php.api.tree.statement.CatchBlockTree;
import org.sonar.plugins.php.api.tree.statement.ContinueStatementTree;
import org.sonar.plugins.php.api.tree.statement.DoWhileStatementTree;
import org.sonar.plugins.php.api.tree.statement.ElseClauseTree;
import org.sonar.plugins.php.api.tree.statement.ElseifClauseTree;
import org.sonar.plugins.php.api.tree.statement.ForEachStatementTree;
import org.sonar.plugins.php.api.tree.statement.ForStatementTree;
import org.sonar.plugins.php.api.tree.statement.GotoStatementTree;
import org.sonar.plugins.php.api.tree.statement.IfStatementTree;
import org.sonar.plugins.php.api.tree.statement.StatementTree;
import org.sonar.plugins.php.api.tree.statement.SwitchStatementTree;
import org.sonar.plugins.php.api.tree.statement.WhileStatementTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

import static org.sonar.plugins.php.api.tree.Tree.Kind.CONDITIONAL_AND;
import static org.sonar.plugins.php.api.tree.Tree.Kind.CONDITIONAL_OR;

public class CognitiveComplexityVisitor extends PHPVisitorCheck {

  private CognitiveComplexity complexity = new CognitiveComplexity();

  private Set<IfStatementTree> ifStatementWithoutNesting = new HashSet<>();
  private Set<ExpressionTree> logicalOperationsToIgnore = new HashSet<>();

  public static CognitiveComplexity complexity(FunctionTree functionTree) {
    CognitiveComplexityVisitor cognitiveComplexityVisitor = new CognitiveComplexityVisitor();
    cognitiveComplexityVisitor.scan(functionTree);
    return cognitiveComplexityVisitor.complexity;
  }

  @Override
  public void visitIfStatement(IfStatementTree tree) {
    if (ifStatementWithoutNesting.contains(tree)) {
      complexity.addComplexityWithoutNesting(tree.ifToken());

    } else {
      complexity.addComplexityWithNesting(tree.ifToken());
    }

    visit(tree.condition());
    visitWithNesting(tree.statements());
    tree.elseifClauses().forEach(this::visit);
    visit(tree.elseClause());
  }

  @Override
  public void visitElseifClause(ElseifClauseTree tree) {
    complexity.addComplexityWithoutNesting(tree.elseifToken());

    visit(tree.condition());
    visitWithNesting(tree.statements());
  }

  @Override
  public void visitElseClause(ElseClauseTree tree) {
    if (tree.is(Kind.ELSE_CLAUSE) && tree.statements().get(0).is(Kind.IF_STATEMENT)) {
      ifStatementWithoutNesting.add((IfStatementTree) tree.statements().get(0));

    } else {
      complexity.addComplexityWithoutNesting(tree.elseToken());
    }

    visitWithNesting(tree.statements());
  }

  @Override
  public void visitSwitchStatement(SwitchStatementTree tree) {
    complexity.addComplexityWithNesting(tree.switchToken());

    complexity.incNesting();
    super.visitSwitchStatement(tree);
    complexity.decNesting();
  }

  @Override
  public void visitWhileStatement(WhileStatementTree tree) {
    complexity.addComplexityWithNesting(tree.whileToken());

    visit(tree.condition());
    visitWithNesting(tree.statements());
  }

  @Override
  public void visitDoWhileStatement(DoWhileStatementTree tree) {
    complexity.addComplexityWithNesting(tree.doToken());

    visitWithNesting(tree.statement());
    visit(tree.condition());
  }

  @Override
  public void visitForStatement(ForStatementTree tree) {
    complexity.addComplexityWithNesting(tree.forToken());

    tree.init().forEach(this::visit);
    tree.condition().forEach(this::visit);
    tree.update().forEach(this::visit);
    visitWithNesting(tree.statements());
  }

  @Override
  public void visitForEachStatement(ForEachStatementTree tree) {
    complexity.addComplexityWithNesting(tree.foreachToken());

    visit(tree.expression());
    visit(tree.key());
    visit(tree.value());
    visitWithNesting(tree.statements());
  }

  @Override
  public void visitCatchBlock(CatchBlockTree tree) {
    complexity.addComplexityWithNesting(tree.catchToken());

    complexity.incNesting();
    super.visitCatchBlock(tree);
    complexity.decNesting();
  }

  @Override
  public void visitFunctionDeclaration(FunctionDeclarationTree tree) {
    complexity.incNesting();
    super.visitFunctionDeclaration(tree);
    complexity.decNesting();
  }

  @Override
  public void visitFunctionExpression(FunctionExpressionTree tree) {
    complexity.incNesting();
    super.visitFunctionExpression(tree);
    complexity.decNesting();
  }

  @Override
  public void visitMethodDeclaration(MethodDeclarationTree tree) {
    complexity.incNesting();
    super.visitMethodDeclaration(tree);
    complexity.decNesting();
  }

  @Override
  public void visitConditionalExpression(ConditionalExpressionTree tree) {
    complexity.addComplexityWithNesting(tree.queryToken());
    visit(tree.condition());
    visitWithNesting(tree.trueExpression());
    visitWithNesting(tree.falseExpression());
  }

  @Override
  public void visitBreakStatement(BreakStatementTree tree) {
    if (tree.argument() != null) {
      complexity.addComplexityWithoutNesting(tree.breakToken());
    }
    super.visitBreakStatement(tree);
  }

  @Override
  public void visitContinueStatement(ContinueStatementTree tree) {
    if (tree.argument() != null) {
      complexity.addComplexityWithoutNesting(tree.continueToken());
    }
    super.visitContinueStatement(tree);
  }

  @Override
  public void visitGotoStatement(GotoStatementTree tree) {
    complexity.addComplexityWithoutNesting(tree.gotoToken());
    super.visitGotoStatement(tree);
  }

  @Override
  public void visitBinaryExpression(BinaryExpressionTree tree) {
    if (tree.is(CONDITIONAL_AND, CONDITIONAL_OR)) {

      ExpressionTree leftChild = removeParenthesis(tree.leftOperand());
      ExpressionTree rightChild = removeParenthesis(tree.rightOperand());

      boolean leftChildOfSameKind = leftChild.is(tree.getKind());
      boolean rightChildOfSameKind = rightChild.is(tree.getKind());

      // For expressions with same-kind operators like "a && (b && c)" we want to have secondary location on leftmost operator
      // So we "ignore" right operand
      if (rightChildOfSameKind) {
        logicalOperationsToIgnore.add(rightChild);
      }

      // And we add complexity for leftmost operator
      if (!logicalOperationsToIgnore.contains(tree) && !leftChildOfSameKind) {
        complexity.addComplexityWithoutNesting(tree.operator());
      }

    }

    super.visitBinaryExpression(tree);
  }

  private void visitWithNesting(@Nullable Tree tree) {
    if (tree != null) {
      complexity.incNesting();
      tree.accept(this);
      complexity.decNesting();
    }
  }

  private void visitWithNesting(List<StatementTree> statements) {
    complexity.incNesting();
    statements.forEach(statementTree -> statementTree.accept(this));
    complexity.decNesting();
  }

  private void visit(@Nullable Tree tree) {
    if (tree != null) {
      tree.accept(this);
    }
  }

  public static class CognitiveComplexity {

    private List<ComplexityComponent> complexityComponents = new ArrayList<>();
    private int value = 0;
    private int level = 0;

    public List<ComplexityComponent> getComplexityComponents() {
      return complexityComponents;
    }

    public int getValue() {
      return value;
    }

    private void incNesting() {
      level++;
    }

    private void decNesting() {
      level--;
    }

    private void addComplexityWithNesting(SyntaxToken secondaryLocationToken) {
      addComplexityWithoutNesting(secondaryLocationToken, level + 1);
    }

    private void addComplexityWithoutNesting(SyntaxToken secondaryLocationToken) {
      addComplexityWithoutNesting(secondaryLocationToken, 1);
    }

    private void addComplexityWithoutNesting(SyntaxToken secondaryLocationToken, int addedComplexity) {
      value += addedComplexity;
      complexityComponents.add(new ComplexityComponent(secondaryLocationToken, addedComplexity));
    }



    public static class ComplexityComponent {

      private Tree tree;
      private int addedComplexity;

      private ComplexityComponent(Tree tree, int addedComplexity) {
        this.tree = tree;
        this.addedComplexity = addedComplexity;
      }

      public Tree tree() {
        return tree;
      }

      public int addedComplexity() {
        return addedComplexity;
      }
    }
  }

  private static ExpressionTree removeParenthesis(ExpressionTree expressionTree) {
    if (expressionTree.is(Tree.Kind.PARENTHESISED_EXPRESSION)) {
      return removeParenthesis(((ParenthesisedExpressionTree) expressionTree).expression());
    }
    return expressionTree;
  }

}
