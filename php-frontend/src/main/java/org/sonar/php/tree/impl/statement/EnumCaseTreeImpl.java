package org.sonar.php.tree.impl.statement;

import java.util.Iterator;
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.php.utils.collections.IteratorUtils;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.expression.NameIdentifierTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.tree.statement.EnumCaseTree;
import org.sonar.plugins.php.api.visitors.VisitorCheck;

public class EnumCaseTreeImpl extends PHPTree implements EnumCaseTree {

  private final SyntaxToken caseToken;
  private final NameIdentifierTree name;
  private final SyntaxToken eosToken;

  public EnumCaseTreeImpl(SyntaxToken caseToken, NameIdentifierTree name, SyntaxToken eosToken) {
    this.caseToken = caseToken;
    this.name = name;
    this.eosToken = eosToken;
  }

  @Override
  public SyntaxToken caseToken() {
    return caseToken;
  }

  @Override
  public NameIdentifierTree name() {
    return name;
  }

  @Override
  public SyntaxToken eosToken() {
    return eosToken;
  }

  @Override
  public Iterator<Tree> childrenIterator() {
    return IteratorUtils.iteratorOf(caseToken, name, eosToken);
  }

  @Override
  public void accept(VisitorCheck visitor) {
  }

  @Override
  public Kind getKind() {
    return Kind.ENUM_CASE;
  }
}
