package org.sonar.php.checks;

import org.sonar.check.Rule;
import org.sonar.php.symbols.Symbols;
import org.sonar.plugins.php.api.symbols.QualifiedName;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.NamespaceNameTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.expression.NewExpressionTree;
import org.sonar.plugins.php.api.tree.statement.ThrowStatementTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

import java.util.Optional;

@Rule(key = "S5632")
public class ThrowThrowableCheck extends PHPVisitorCheck {
  private static final String MESSAGE = "Throw an object derived from \"Throwable\".";
  private static final QualifiedName THROWABLE_FQN = QualifiedName.qualifiedName("Throwable");

  @Override
  public void visitThrowStatement(ThrowStatementTree tree) {
    if (tree.expression().is(Tree.Kind.NEW_EXPRESSION)) {
      Optional<NamespaceNameTree> namespaceNameTree = extractNamespaceTree(((NewExpressionTree) tree.expression()).expression());
      namespaceNameTree.ifPresent(n -> verifyClass(n, tree));
    }
    super.visitThrowStatement(tree);
  }

  private static Optional<NamespaceNameTree> extractNamespaceTree(ExpressionTree expression) {
    if (expression.is(Tree.Kind.FUNCTION_CALL) && ((FunctionCallTree) expression).callee().is(Tree.Kind.NAMESPACE_NAME)) {
      return Optional.of((NamespaceNameTree) ((FunctionCallTree) expression).callee());
    } else if (expression.is(Tree.Kind.NAMESPACE_NAME)) {
      return Optional.of((NamespaceNameTree) expression);
    }

    return Optional.empty();
  }

  private void verifyClass(NamespaceNameTree namespaceNameTree, ThrowStatementTree tree) {
    if (Symbols.getClass(namespaceNameTree).isSubTypeOf(THROWABLE_FQN).isFalse()) {
      context().newIssue(this, tree, MESSAGE);
    }
  }
}
