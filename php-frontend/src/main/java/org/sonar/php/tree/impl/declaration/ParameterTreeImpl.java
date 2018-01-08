/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2018 SonarSource SA
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
import java.util.Iterator;
import javax.annotation.Nullable;
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.php.tree.impl.lexical.InternalSyntaxToken;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.ParameterTree;
import org.sonar.plugins.php.api.tree.declaration.TypeTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.VariableIdentifierTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.visitors.VisitorCheck;

public class ParameterTreeImpl extends PHPTree implements ParameterTree {

  private static final Kind KIND = Kind.PARAMETER;

  private final TypeTree type;
  private final InternalSyntaxToken referenceToken;
  private final InternalSyntaxToken ellipsisToken;
  private final VariableIdentifierTree variableIdentifier;
  private final InternalSyntaxToken equalToken;
  private final ExpressionTree initValue;

  public ParameterTreeImpl(
    @Nullable TypeTree type,
    @Nullable InternalSyntaxToken referenceToken,
    @Nullable InternalSyntaxToken ellipsisToken,
    VariableIdentifierTree variableIdentifier,
    @Nullable InternalSyntaxToken equalToken,
    @Nullable ExpressionTree initValue
   ) {
    this.type = type;
    this.referenceToken = referenceToken;
    this.ellipsisToken = ellipsisToken;
    this.variableIdentifier = variableIdentifier;
    this.equalToken = equalToken;
    this.initValue = initValue;
  }

  @Nullable
  @Override
  public TypeTree type() {
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

  @Override
  public Kind getKind() {
    return KIND;
  }

  @Override
  public Iterator<Tree> childrenIterator() {
    return Iterators.forArray(type, referenceToken, ellipsisToken, variableIdentifier, equalToken, initValue);
  }

  @Override
  public void accept(VisitorCheck visitor) {
    visitor.visitParameter(this);
  }

}
