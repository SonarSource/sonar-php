package org.sonar.php.checks.phpunit;

import org.sonar.check.Rule;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.php.checks.utils.PhpUnitCheck;
import org.sonar.plugins.php.api.symbols.Symbol;
import org.sonar.plugins.php.api.symbols.SymbolTable;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.expression.MemberAccessTree;
import org.sonar.plugins.php.api.tree.expression.VariableIdentifierTree;
import org.sonar.plugins.php.api.tree.statement.BlockTree;
import org.sonar.plugins.php.api.tree.statement.ExpressionStatementTree;
import org.sonar.plugins.php.api.tree.statement.StatementTree;
import org.sonar.plugins.php.api.tree.statement.TryStatementTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

import java.util.Optional;

@Rule(key = "S5935")
public class ExceptionTestingCheck extends PhpUnitCheck {
  @Override
  public void visitTryStatement(TryStatementTree tree) {
    if (!isPhpUnitTestMethod()) {
      return;
    }

    if (tree.catchBlocks().size() == 1 && containsCallToFail(tree.block())) {
      CatchBlockInspector catchBlockInspector = new CatchBlockInspector(tree.catchBlocks().get(0).variable(), context().symbolTable());
      tree.catchBlocks().get(0).block().accept(catchBlockInspector);

    }

    super.visitTryStatement(tree);
  }

  private boolean containsCallToFail(BlockTree block) {
    int numberOfStatements = block.statements().size();
    return numberOfStatements > 0 && isCallToFail(block.statements().get(numberOfStatements - 1));
  }

  private boolean isCallToFail(StatementTree statementTree) {
    if (!statementTree.is(Tree.Kind.EXPRESSION_STATEMENT)
      || !((ExpressionStatementTree) statementTree).expression().is(Tree.Kind.FUNCTION_CALL)) {
      return false;
    }

    FunctionCallTree functionCall = (FunctionCallTree) ((ExpressionStatementTree) statementTree).expression();
    return "fail".equals(getFunctionName(functionCall));
  }

  private static String getFunctionName(FunctionCallTree tree) {
    String functionName = CheckUtils.getLowerCaseFunctionName(tree);

    if (functionName != null && functionName.contains("::")) {
      functionName = functionName.substring(functionName.lastIndexOf("::") + 2);
    }

    return functionName;
  }

  private static class CatchBlockInspector extends PHPVisitorCheck {
    private final Symbol exceptionVariableSymbol;
    private final SymbolTable symbolTable;
    private boolean didFindOtherStatements = false;
    private FunctionCallTree foundMessageAssertions;
    private FunctionCallTree foundCodeAssertion;

    public CatchBlockInspector(VariableIdentifierTree variable, SymbolTable symbolTable) {
      this.symbolTable = symbolTable;
      exceptionVariableSymbol = symbolTable.getSymbol(variable);
    }

    @Override
    public void visitFunctionCall(FunctionCallTree tree) {
      String functionName = getFunctionName(tree);
      if ("assertEquals".equals(functionName) && isAssertion(tree) && tree.arguments().size() >= 2) {
        String exceptionMethodCall = getExceptionVariableMethodCall(tree.arguments().get(0)).orElse(getExceptionVariableMethodCall(tree.arguments().get(1)).orElse(null));

        if ("getmessage".equals(exceptionMethodCall)) {
          foundMessageAssertions = tree;
        } else if ("getcode".equals(exceptionMethodCall)) {
          foundCodeAssertion = tree;
        }
      } else {
        didFindOtherStatements = true;
      }
    }

    private Optional<String> getExceptionVariableMethodCall(ExpressionTree expressionTree) {
      if (!expressionTree.is(Tree.Kind.FUNCTION_CALL) ||
        ((FunctionCallTree) expressionTree).callee().is(Tree.Kind.OBJECT_MEMBER_ACCESS)) {
        return Optional.empty();
      }

      ExpressionTree object = ((MemberAccessTree) ((FunctionCallTree) expressionTree).callee()).object();

      if (!object.is(Tree.Kind.VARIABLE_IDENTIFIER) || symbolTable.getSymbol(object) != exceptionVariableSymbol) {
        return Optional.empty();
      }

      return Optional.ofNullable(getFunctionName(((FunctionCallTree) expressionTree)));
    }
  }
}
