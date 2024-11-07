/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2024 SonarSource SA
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
import java.util.List;
import javax.annotation.Nullable;
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.php.tree.impl.lexical.InternalSyntaxToken;
import org.sonar.php.utils.collections.IteratorUtils;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.AttributeGroupTree;
import org.sonar.plugins.php.api.tree.declaration.ParameterListTree;
import org.sonar.plugins.php.api.tree.declaration.PropertyHookTree;
import org.sonar.plugins.php.api.tree.expression.NameIdentifierTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.visitors.VisitorCheck;

public class PropertyHookTreeImpl extends PHPTree implements PropertyHookTree {

  private static final Kind KIND = Kind.PROPERTY_HOOK_METHOD_DECLARATION;

  private final List<AttributeGroupTree> attributeGroups;
  private final List<SyntaxToken> modifiersToken;
  @Nullable
  private final InternalSyntaxToken referenceToken;
  private final NameIdentifierTree name;
  @Nullable
  private final ParameterListTree parameters;
  @Nullable
  private final InternalSyntaxToken doubleArrowToken;
  private final Tree body;

  public PropertyHookTreeImpl(
    List<AttributeGroupTree> attributeGroups,
    List<SyntaxToken> modifiersToken,
    @Nullable InternalSyntaxToken referenceToken,
    NameIdentifierTree name,
    @Nullable ParameterListTree parameters,
    @Nullable InternalSyntaxToken doubleArrowToken,
    Tree body) {
    this.attributeGroups = attributeGroups;
    this.modifiersToken = modifiersToken;
    this.referenceToken = referenceToken;
    this.name = name;
    this.parameters = parameters;
    this.doubleArrowToken = doubleArrowToken;
    this.body = body;
  }

  @Override
  public List<SyntaxToken> modifiers() {
    return modifiersToken;
  }

  @Override
  public List<AttributeGroupTree> attributeGroups() {
    return attributeGroups;
  }

  @Nullable
  @Override
  public SyntaxToken referenceToken() {
    return referenceToken;
  }

  @Override
  public NameIdentifierTree name() {
    return name;
  }

  @Override
  public ParameterListTree parameters() {
    return parameters;
  }

  @Nullable
  @Override
  public SyntaxToken doubleArrowToken() {
    return doubleArrowToken;
  }

  @Override
  public Tree body() {
    return body;
  }

  @Override
  public Kind getKind() {
    return KIND;
  }

  @Override
  public Iterator<Tree> childrenIterator() {
    return IteratorUtils.concat(
      attributeGroups.iterator(),
      modifiersToken.iterator(),
      IteratorUtils.nullableIterator(referenceToken),
      IteratorUtils.iteratorOf(name),
      IteratorUtils.nullableIterator(parameters),
      IteratorUtils.nullableIterator(doubleArrowToken),
      IteratorUtils.iteratorOf(body));
  }

  @Override
  public void accept(VisitorCheck visitor) {
    visitor.visitPropertyHook(this);
  }

}
