/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2020 SonarSource SA
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
