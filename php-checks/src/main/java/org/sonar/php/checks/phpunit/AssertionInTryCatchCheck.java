package org.sonar.php.checks.phpunit;

import com.google.common.collect.ImmutableSet;
import org.sonar.check.Rule;
import org.sonar.php.checks.utils.PhpUnitCheck;
import org.sonar.php.symbols.Symbols;
import org.sonar.plugins.php.api.tree.declaration.NamespaceNameTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.expression.VariableIdentifierTree;
import org.sonar.plugins.php.api.tree.statement.CatchBlockTree;
import org.sonar.plugins.php.api.tree.statement.TryStatementTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;
import org.sonar.plugins.php.api.visitors.PreciseIssue;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Rule(key = "S5779")
public class AssertionInTryCatchCheck extends PhpUnitCheck {
  private static final String MESSAGE = "Don't use this assertion inside a try-catch catching an assertion exception.";
  private static final String SECONDARY_MESSAGE = "Exception type that catches assertion exceptions.";

  private static final Set<String> RELEVANT_EXCEPTIONS = ImmutableSet.of(
    "exception",
    "phpunit\\framework\\expectationfailedexception",
    "phpunit\\framework\\assertionfailederror"
  );

  @Override
  public void visitTryStatement(TryStatementTree tree) {
    if (!isPhpUnitTestCase()) {
      return;
    }

    List<NamespaceNameTree> caughtRelevantExceptionTypes = getCaughtRelevantExceptionTypes(tree.catchBlocks());
    if (!caughtRelevantExceptionTypes.isEmpty()) {
      AssertionsFindVisitor assertionsFindVisitor = new AssertionsFindVisitor();
      tree.block().accept(assertionsFindVisitor);
      assertionsFindVisitor.foundAssertions.forEach(a -> raiseIssue(a, caughtRelevantExceptionTypes));
    }

    super.visitTryStatement(tree);
  }

  private void raiseIssue(FunctionCallTree assertion, List<NamespaceNameTree> caughtRelevantExceptionTypes) {
    PreciseIssue issue = context().newIssue(this, assertion, MESSAGE);
    caughtRelevantExceptionTypes.forEach(e -> issue.secondary(e, SECONDARY_MESSAGE));
  }

  private List<NamespaceNameTree> getCaughtRelevantExceptionTypes(List<CatchBlockTree> catchBlocks) {
    List<NamespaceNameTree> result = new ArrayList<>();
    for(CatchBlockTree catchBlockTree : catchBlocks) {
      if (exceptionVariableIsUsed(catchBlockTree.variable())) {
        continue;
      }

      result.addAll(
        catchBlockTree.exceptionTypes().stream().filter(this::isRelevantExceptionType).collect(Collectors.toList())
      );
    }

    return result;
  }

  private boolean exceptionVariableIsUsed(VariableIdentifierTree variable) {
    return !context().symbolTable().getSymbol(variable).usages().isEmpty();
  }

  private boolean isRelevantExceptionType(NamespaceNameTree tree) {
    return RELEVANT_EXCEPTIONS.contains(Symbols.getClass(tree).qualifiedName().toString());
  }

  private static class AssertionsFindVisitor extends PHPVisitorCheck {
    private final List<FunctionCallTree> foundAssertions = new ArrayList<>();

    @Override
    public void visitTryStatement(TryStatementTree tree) {
      // Avoid visiting nested try-catch statement.
    }

    @Override
    public void visitFunctionCall(FunctionCallTree tree) {
      if (isAssertion(tree)) {
        foundAssertions.add(tree);
      }

      super.visitFunctionCall(tree);
    }
  }
}
