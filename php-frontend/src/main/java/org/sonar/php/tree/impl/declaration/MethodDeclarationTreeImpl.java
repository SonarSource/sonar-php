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
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.php.tree.impl.lexical.InternalSyntaxToken;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.MethodDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.ParameterListTree;
import org.sonar.plugins.php.api.tree.declaration.ReturnTypeClauseTree;
import org.sonar.plugins.php.api.tree.expression.NameIdentifierTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.visitors.VisitorCheck;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.List;

public class MethodDeclarationTreeImpl extends PHPTree implements MethodDeclarationTree {

  private static final Kind KIND = Kind.METHOD_DECLARATION;

  private final List<SyntaxToken> modifiersToken;
  private final InternalSyntaxToken functionToken;
  private final InternalSyntaxToken referenceToken;
  private final NameIdentifierTree name;
  private final ParameterListTree parameters;
  private final ReturnTypeClauseTree returnTypeClause;
  private final Tree body;

  public MethodDeclarationTreeImpl(
    List<SyntaxToken> modifiersToken,
    InternalSyntaxToken functionToken,
    @Nullable InternalSyntaxToken referenceToken,
    NameIdentifierTree name,
    ParameterListTree parameters,
    @Nullable ReturnTypeClauseTree returnTypeClause,
    Tree body
    ) {
    this.modifiersToken = modifiersToken;
    this.functionToken = functionToken;
    this.referenceToken = referenceToken;
    this.name = name;
    this.parameters = parameters;
    this.returnTypeClause = returnTypeClause;
    this.body = body;
  }

  @Override
  public List<SyntaxToken> modifiers() {
    return modifiersToken;
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
  public Tree body() {
    return body;
  }

  @Override
  public Kind getKind() {
    return KIND;
  }

  @Override
  public Iterator<Tree> childrenIterator() {
    return Iterators.concat(
      modifiersToken.iterator(),
      Iterators.forArray(functionToken, referenceToken, name, parameters, returnTypeClause, body));
  }

  @Override
  public void accept(VisitorCheck visitor) {
    visitor.visitMethodDeclaration(this);
  }

}
