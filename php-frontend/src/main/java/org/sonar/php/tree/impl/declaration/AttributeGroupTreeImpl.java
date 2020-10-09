package org.sonar.php.tree.impl.declaration;

import com.google.common.collect.Iterators;
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.plugins.php.api.tree.SeparatedList;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.AttributeGroupTree;
import org.sonar.plugins.php.api.tree.declaration.AttributeTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.visitors.VisitorCheck;

import java.util.Iterator;

public class AttributeGroupTreeImpl extends PHPTree implements AttributeGroupTree {
  private final SyntaxToken startToken;
  private final SeparatedList<AttributeTree> attributes;
  private final SyntaxToken endToken;

  public AttributeGroupTreeImpl(SyntaxToken startToken, SeparatedList<AttributeTree> attributes, SyntaxToken endToken) {
    this.startToken = startToken;
    this.attributes = attributes;
    this.endToken = endToken;
  }

  @Override
  public Iterator<Tree> childrenIterator() {
    return Iterators.concat(
      Iterators.singletonIterator(startToken),
      attributes.elementsAndSeparators(),
      Iterators.singletonIterator(endToken)
    );
  }

  @Override
  public void accept(VisitorCheck visitor) {

  }

  @Override
  public Kind getKind() {
    return Kind.ATTRIBUTE_GROUP;
  }

  @Override
  public SyntaxToken startToken() {
    return startToken;
  }

  @Override
  public SeparatedList<AttributeTree> attributes() {
    return attributes;
  }

  @Override
  public SyntaxToken endToken() {
    return endToken;
  }
}
