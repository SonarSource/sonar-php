package org.sonar.plugins.php.api.tree.declaration;

import org.sonar.plugins.php.api.tree.SeparatedList;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;

public interface AttributeTree extends Tree {

  NamespaceNameTree name();

  // TODO: replace with CallArgument once rebased

  SeparatedList<ExpressionTree> arguments();

}
