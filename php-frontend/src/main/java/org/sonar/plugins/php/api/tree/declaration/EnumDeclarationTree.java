package org.sonar.plugins.php.api.tree.declaration;

import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.expression.NameIdentifierTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;

public interface EnumDeclarationTree extends Tree {

  SyntaxToken enumToken();

  NameIdentifierTree name();

  SyntaxToken openCurlyBraceToken();

  SyntaxToken closeCurlyBraceToken();
}
