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

import java.util.Iterator;

import javax.annotation.Nullable;

import org.sonar.php.tree.impl.PHPTree;
import org.sonar.php.tree.impl.lexical.InternalSyntaxToken;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.FunctionDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.ParameterListTree;
import org.sonar.plugins.php.api.tree.declaration.ReturnTypeClauseTree;
import org.sonar.plugins.php.api.tree.expression.NameIdentifierTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.tree.statement.BlockTree;
import org.sonar.plugins.php.api.visitors.VisitorCheck;

import com.google.common.collect.Iterators;

public class FunctionDeclarationTreeImpl extends PHPTree implements FunctionDeclarationTree {

  private static final Kind KIND = Kind.FUNCTION_DECLARATION;

  private final InternalSyntaxToken functionToken;
  private final InternalSyntaxToken referenceToken;
  private final NameIdentifierTree name;
  private final ParameterListTree parameters;
  private final ReturnTypeClauseTree returnTypeClause;
  private final BlockTree body;

  public FunctionDeclarationTreeImpl(
    InternalSyntaxToken functionToken,
    @Nullable InternalSyntaxToken referenceToken,
    NameIdentifierTree name,
    ParameterListTree parameters,
    @Nullable ReturnTypeClauseTree returnTypeClause,
    BlockTree body
  ) {
    this.functionToken = functionToken;
    this.referenceToken = referenceToken;
    this.name = name;
    this.parameters = parameters;
    this.returnTypeClause = returnTypeClause;
    this.body = body;
  }

  @Override
  public SyntaxToken functionToken() {
    return functionToken;
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
  public ReturnTypeClauseTree returnTypeClause() {
    return returnTypeClause;
  }

  @Override
  public BlockTree body() {
    return body;
  }

  @Override
  public Kind getKind() {
    return KIND;
  }

  @Override
  public Iterator<Tree> childrenIterator() {
    return Iterators.forArray(functionToken, referenceToken, name, parameters, returnTypeClause, body);
  }

  @Override
  public void accept(VisitorCheck visitor) {
    visitor.visitFunctionDeclaration(this);
  }

}
