/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.php.tree.impl.declaration;

import java.util.Iterator;
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.php.utils.collections.IteratorUtils;
import org.sonar.plugins.php.api.tree.SeparatedList;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.DnfIntersectionTypeTree;
import org.sonar.plugins.php.api.tree.declaration.TypeTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.visitors.VisitorCheck;

public class DnfIntersectionTypeTreeImpl extends PHPTree implements DnfIntersectionTypeTree {
  private final SyntaxToken openParenthesisToken;
  private final SeparatedList<TypeTree> types;
  private final SyntaxToken closedParenthesisToken;

  public DnfIntersectionTypeTreeImpl(SyntaxToken openParenthesisToken, SeparatedList<TypeTree> types, SyntaxToken closedParenthesisToken) {
    this.openParenthesisToken = openParenthesisToken;
    this.types = types;
    this.closedParenthesisToken = closedParenthesisToken;
  }

  @Override
  public Iterator<Tree> childrenIterator() {
    return IteratorUtils.concat(
      IteratorUtils.iteratorOf(openParenthesisToken),
      types.elementsAndSeparators(),
      IteratorUtils.iteratorOf(closedParenthesisToken));
  }

  @Override
  public SyntaxToken openParenthesisToken() {
    return openParenthesisToken;
  }

  @Override
  public SeparatedList<TypeTree> types() {
    return types;
  }

  @Override
  public SyntaxToken closedParenthesisToken() {
    return closedParenthesisToken;
  }

  @Override
  public boolean isSimple() {
    return false;
  }

  @Override
  public void accept(VisitorCheck visitor) {
    visitor.visitDnfIntersectionType(this);
  }

  @Override
  public Kind getKind() {
    return Kind.DNF_INTERSECTION_TYPE;
  }
}
