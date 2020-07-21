package org.sonar.php.checks;

import org.sonar.check.Rule;
import org.sonar.php.symbols.Symbols;
import org.sonar.plugins.php.api.symbols.QualifiedName;
import org.sonar.plugins.php.api.tree.statement.CatchBlockTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

@Rule(key="S5708")
public class CatchThrowableCheck extends PHPVisitorCheck {
  private static final String MESSAGE = "Change this type to be a class deriving from \"Throwable\".";
  private static final QualifiedName THROWABLE_FQN = QualifiedName.qualifiedName("Throwable");

  @Override
  public void visitCatchBlock(CatchBlockTree tree) {
    tree.exceptionTypes().stream().
      filter(type -> Symbols.getClass(type).isSubTypeOf(THROWABLE_FQN).isFalse()).
      forEach(type -> context().newIssue(this, type, MESSAGE));
    super.visitCatchBlock(tree);
  }
}
