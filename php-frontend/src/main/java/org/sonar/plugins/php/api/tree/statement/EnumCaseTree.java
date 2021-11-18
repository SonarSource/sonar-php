package org.sonar.plugins.php.api.tree.statement;

import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.expression.NameIdentifierTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;

public interface EnumCaseTree extends Tree {

  SyntaxToken caseToken();

  NameIdentifierTree name();

  SyntaxToken eosToken();
}
