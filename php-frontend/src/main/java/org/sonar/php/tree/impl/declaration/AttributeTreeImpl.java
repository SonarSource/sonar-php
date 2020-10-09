package org.sonar.php.tree.impl.declaration;

import com.google.common.collect.Iterators;
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.php.tree.impl.SeparatedListImpl;
import org.sonar.plugins.php.api.tree.SeparatedList;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.AttributeTree;
import org.sonar.plugins.php.api.tree.declaration.CallArgumentTree;
import org.sonar.plugins.php.api.tree.declaration.NamespaceNameTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.visitors.VisitorCheck;

import javax.annotation.Nullable;
import java.util.Iterator;

public class AttributeTreeImpl extends PHPTree implements AttributeTree {
  private final NamespaceNameTree name;
  @Nullable
  private final SyntaxToken openParenthesisToken;
  private final SeparatedListImpl<CallArgumentTree> arguments;
  @Nullable
  private final SyntaxToken closeParenthesisToken;

  public AttributeTreeImpl(NamespaceNameTree name, @Nullable SyntaxToken openParenthesisToken, SeparatedListImpl<CallArgumentTree> arguments, @Nullable SyntaxToken closeParenthesisToken) {
    this.name = name;
    this.openParenthesisToken = openParenthesisToken;
    this.arguments = arguments;
    this.closeParenthesisToken = closeParenthesisToken;
  }

  @Override
  public Iterator<Tree> childrenIterator() {
    return Iterators.concat(
      Iterators.forArray(name, openParenthesisToken),
      arguments.elementsAndSeparators(),
      Iterators.singletonIterator(closeParenthesisToken)
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
  public SyntaxToken openParenthesisToken() {
    return openParenthesisToken;
  }

  @Override
  public SeparatedList<CallArgumentTree> arguments() {
    return arguments;
  }

  @Override
  public SyntaxToken closeParenthesisToken() {
    return closeParenthesisToken;
  }
}
