package org.sonar.plugins.php.api.tree.declaration;

import java.util.List;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.expression.NameIdentifierTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.tree.statement.EnumCaseTree;

public interface EnumDeclarationTree extends Tree {

  SyntaxToken enumToken();

  NameIdentifierTree name();

  SyntaxToken openCurlyBraceToken();

  List<EnumCaseTree> cases();

  SyntaxToken closeCurlyBraceToken();
}
