/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
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
import org.sonar.plugins.php.api.tree.declaration.DeclaredTypeTree;
import org.sonar.plugins.php.api.tree.declaration.ParameterTree;
import org.sonar.plugins.php.api.tree.declaration.PropertyHookListTree;
import org.sonar.plugins.php.api.tree.declaration.TypeTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.VariableIdentifierTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.visitors.VisitorCheck;

public class ParameterTreeImpl extends PHPTree implements ParameterTree {

  private static final Kind KIND = Kind.PARAMETER;

  private final List<AttributeGroupTree> attributeGroups;
  private final List<SyntaxToken> visibilityAndReadonly;
  private final DeclaredTypeTree type;
  private final InternalSyntaxToken referenceToken;
  private final InternalSyntaxToken ellipsisToken;
  private final VariableIdentifierTree variableIdentifier;
  private final InternalSyntaxToken equalToken;
  private final ExpressionTree initValue;
  @Nullable
  private final PropertyHookListTree propertyHookList;
  private SyntaxToken readonlyToken;
  private SyntaxToken visibility;

  public ParameterTreeImpl(
    List<AttributeGroupTree> attributeGroups,
    List<SyntaxToken> visibilityAndReadonly,
    @Nullable DeclaredTypeTree type,
    @Nullable InternalSyntaxToken referenceToken,
    @Nullable InternalSyntaxToken ellipsisToken,
    VariableIdentifierTree variableIdentifier,
    @Nullable InternalSyntaxToken equalToken,
    @Nullable ExpressionTree initValue,
    @Nullable PropertyHookListTree propertyHookList) {
    this.attributeGroups = attributeGroups;
    this.visibilityAndReadonly = visibilityAndReadonly;

    for (SyntaxToken token : visibilityAndReadonly) {
      if ("readonly".equals(token.text())) {
        this.readonlyToken = token;
      } else {
        this.visibility = token;
      }
    }

    this.type = type;
    this.referenceToken = referenceToken;
    this.ellipsisToken = ellipsisToken;
    this.variableIdentifier = variableIdentifier;
    this.equalToken = equalToken;
    this.initValue = initValue;
    this.propertyHookList = propertyHookList;
  }

  @Override
  public List<AttributeGroupTree> attributeGroups() {
    return attributeGroups;
  }

  @Nullable
  @Override
  public SyntaxToken visibility() {
    return visibility;
  }

  /**
   * @deprecated since 3.11 - Use {@link #declaredType()} instead.
   */
  @Nullable
  @Override
  @Deprecated
  public TypeTree type() {
    if (type == null) {
      return null;
    }

    if (type.is(Kind.TYPE)) {
      return (TypeTree) type;
    } else {
      return ((CombinedTypeTreeImpl) type).types().get(0);
    }
  }

  public DeclaredTypeTree declaredType() {
    return type;
  }

  @Nullable
  @Override
  public SyntaxToken referenceToken() {
    return referenceToken;
  }

  @Nullable
  @Override
  public SyntaxToken ellipsisToken() {
    return ellipsisToken;
  }

  @Override
  public VariableIdentifierTree variableIdentifier() {
    return variableIdentifier;
  }

  @Nullable
  @Override
  public SyntaxToken equalToken() {
    return equalToken;
  }

  @Nullable
  @Override
  public ExpressionTree initValue() {
    return initValue;
  }

  @Nullable
  @Override
  public PropertyHookListTree propertyHookList() {
    return propertyHookList;
  }

  @Nullable
  @Override
  public SyntaxToken readonlyToken() {
    return readonlyToken;
  }

  @Override
  public Kind getKind() {
    return KIND;
  }

  @Override
  public Iterator<Tree> childrenIterator() {
    return IteratorUtils.concat(
      attributeGroups.iterator(),
      visibilityAndReadonly.iterator(),
      IteratorUtils.iteratorOf(type, referenceToken, ellipsisToken, variableIdentifier, equalToken, initValue),
      IteratorUtils.nullableIterator(propertyHookList));
  }

  @Override
  public void accept(VisitorCheck visitor) {
    visitor.visitParameter(this);
  }

}
