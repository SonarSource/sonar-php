package org.sonar.php.tree.impl.declaration;

import java.util.Iterator;
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.php.utils.collections.IteratorUtils;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.EnumDeclarationTree;
import org.sonar.plugins.php.api.tree.expression.NameIdentifierTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.visitors.VisitorCheck;

public class EnumDeclarationTreeImpl extends PHPTree implements EnumDeclarationTree {

  private final SyntaxToken enumToken;
  private final NameIdentifierTree name;
  private final SyntaxToken openCurlyBraceToken;
  private final SyntaxToken closeCurlyBraceToken;

  public EnumDeclarationTreeImpl(SyntaxToken enumToken, NameIdentifierTree name, SyntaxToken openCurlyBraceToken,
    SyntaxToken closeCurlyBraceToken) {
    this.enumToken = enumToken;
    this.name = name;
    this.openCurlyBraceToken = openCurlyBraceToken;
    this.closeCurlyBraceToken = closeCurlyBraceToken;
  }

  @Override
  public SyntaxToken enumToken() {
    return enumToken;
  }

  @Override
  public NameIdentifierTree name() {
    return name;
  }

  @Override
  public SyntaxToken openCurlyBraceToken() {
    return openCurlyBraceToken;
  }

  @Override
  public SyntaxToken closeCurlyBraceToken() {
    return closeCurlyBraceToken;
  }

  @Override
  public Iterator<Tree> childrenIterator() {
    return IteratorUtils.iteratorOf(enumToken, name, openCurlyBraceToken, closeCurlyBraceToken);
  }

  @Override
  public void accept(VisitorCheck visitor) {

  }

  @Override
  public Kind getKind() {
    return Kind.ENUM_DECLARATION;
  }
}
