package org.sonar.plugins.php.api.tree.declaration;

import org.sonar.plugins.php.api.tree.SeparatedList;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;

public interface AttributeGroupTree extends Tree {

  SyntaxToken startToken();

  SeparatedList<AttributeTree> attributes();

  SyntaxToken endToken();

}
