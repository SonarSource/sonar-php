package org.sonar.php.tree.impl.declaration;

import org.sonar.php.tree.impl.PHPTree;
import org.sonar.plugins.php.api.tree.SeparatedList;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.TypeTree;
import org.sonar.plugins.php.api.tree.declaration.UnionTypeTree;
import org.sonar.plugins.php.api.visitors.VisitorCheck;

import java.util.Iterator;

public class UnionTypeTreeImpl extends PHPTree implements UnionTypeTree {
  private static final Kind KIND = Kind.UNION_TYPE;

  public SeparatedList<TypeTree> types;

  public UnionTypeTreeImpl(SeparatedList<TypeTree> types) {
    this.types = types;
  }

  @Override
  public SeparatedList<TypeTree> types() {
    return types;
  }

  @Override
  public Kind getKind() {
    return KIND;
  }

  @Override
  public Iterator<Tree> childrenIterator() {
    return types.elementsAndSeparators();
  }

  @Override
  public void accept(VisitorCheck visitor) {
    visitor.visitUnionType(this);
  }

}
