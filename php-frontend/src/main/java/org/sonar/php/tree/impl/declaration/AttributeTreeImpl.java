package org.sonar.php.tree.impl.declaration;

import com.google.common.collect.Iterators;
import com.sonar.sslr.api.typed.Optional;
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.php.tree.impl.SeparatedListImpl;
import org.sonar.plugins.php.api.tree.SeparatedList;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.AttributeTree;
import org.sonar.plugins.php.api.tree.declaration.NamespaceNameTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.visitors.VisitorCheck;

import java.util.Collections;
import java.util.Iterator;

public class AttributeTreeImpl extends PHPTree implements AttributeTree {
  private final NamespaceNameTree name;
  private final SeparatedList<ExpressionTree> arguments;

  public AttributeTreeImpl(NamespaceNameTree name, Optional<FunctionCallTree> arguments) {
    this.name = name;

    if (arguments.isPresent()) {
      this.arguments = arguments.get().arguments();
    } else {
      this.arguments = new SeparatedListImpl<>(Collections.emptyList(), Collections.emptyList());
    }
  }

  @Override
  public Iterator<Tree> childrenIterator() {
    return Iterators.concat(
      Iterators.forArray(name),
      arguments.elementsAndSeparators()
    );
  }

  @Override
  public void accept(VisitorCheck visitor) {

  }

  @Override
  public Kind getKind() {
    return Kind.ATTRIBUTE;
  }

  @Override
  public NamespaceNameTree name() {
    return name;
  }

  @Override
  public SeparatedList<ExpressionTree> arguments() {
    return arguments;
  }
}
