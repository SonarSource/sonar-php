package org.sonar.plugins.php.api.tree.declaration;

import org.sonar.plugins.php.api.tree.SeparatedList;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;

import javax.annotation.Nullable;

public interface AttributeTree extends Tree {

  NamespaceNameTree name();

  @Nullable
  SyntaxToken openParenthesisToken();

  SeparatedList<CallArgumentTree> arguments();

  @Nullable
  SyntaxToken closeParenthesisToken();

}
