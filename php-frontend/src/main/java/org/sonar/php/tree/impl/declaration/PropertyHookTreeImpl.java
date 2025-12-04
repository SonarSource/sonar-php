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
  @Nullable
  private final SyntaxToken modifierToken;
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
    @Nullable SyntaxToken modifierToken,
    @Nullable InternalSyntaxToken referenceToken,
    NameIdentifierTree name,
    @Nullable ParameterListTree parameters,
    @Nullable InternalSyntaxToken doubleArrowToken,
    Tree body) {
    this.attributeGroups = attributeGroups;
    this.modifierToken = modifierToken;
    this.referenceToken = referenceToken;
    this.name = name;
    this.parameters = parameters;
    this.doubleArrowToken = doubleArrowToken;
    this.body = body;
  }

  @Override
  public SyntaxToken modifierToken() {
    return modifierToken;
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
      IteratorUtils.nullableIterator(modifierToken),
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
