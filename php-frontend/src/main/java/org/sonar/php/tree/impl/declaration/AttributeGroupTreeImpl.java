/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2023 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.php.tree.impl.declaration;

import java.util.Iterator;
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.php.utils.collections.IteratorUtils;
import org.sonar.plugins.php.api.tree.SeparatedList;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.AttributeGroupTree;
import org.sonar.plugins.php.api.tree.declaration.AttributeTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.visitors.VisitorCheck;

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
    return IteratorUtils.concat(
      IteratorUtils.iteratorOf(startToken),
      attributes.elementsAndSeparators(),
      IteratorUtils.iteratorOf(endToken));
  }

  @Override
  public void accept(VisitorCheck visitor) {
    visitor.visitAttributeGroup(this);
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
