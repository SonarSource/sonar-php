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
package org.sonar.php.tree.impl.expression;

import com.google.common.collect.Iterators;
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.php.tree.impl.lexical.InternalSyntaxToken;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.ParameterListTree;
import org.sonar.plugins.php.api.tree.declaration.ReturnTypeClauseTree;
import org.sonar.plugins.php.api.tree.expression.FunctionExpressionTree;
import org.sonar.plugins.php.api.tree.expression.LexicalVariablesTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.tree.statement.BlockTree;
import org.sonar.plugins.php.api.visitors.VisitorCheck;

import javax.annotation.Nullable;
import java.util.Iterator;

public class FunctionExpressionTreeImpl extends PHPTree implements FunctionExpressionTree {

  private static final Kind KIND = Kind.FUNCTION_EXPRESSION;
  private final InternalSyntaxToken staticToken;
  private final InternalSyntaxToken functionToken;
  private final InternalSyntaxToken referenceToken;
  private final ParameterListTree parameters;
  private final LexicalVariablesTree lexicalVars;
  private final ReturnTypeClauseTree returnTypeClause;
  private final BlockTree body;

  public FunctionExpressionTreeImpl(
    @Nullable InternalSyntaxToken staticToken,
    InternalSyntaxToken functionToken,
    @Nullable InternalSyntaxToken referenceToken,
    ParameterListTree parameters,
    @Nullable LexicalVariablesTree lexicalVars,
    @Nullable ReturnTypeClauseTree returnTypeClause,
    BlockTree body
  ) {
    this.staticToken = staticToken;
    this.functionToken = functionToken;
    this.referenceToken = referenceToken;
    this.parameters = parameters;
    this.lexicalVars = lexicalVars;
    this.returnTypeClause = returnTypeClause;
    this.body = body;
  }

  @Nullable
  @Override
  public SyntaxToken staticToken() {
    return staticToken;
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
  public ParameterListTree parameters() {
    return parameters;
  }

  @Nullable
  @Override
  public ReturnTypeClauseTree returnTypeClause() {
    return returnTypeClause;
  }

  @Nullable
  @Override
  public LexicalVariablesTree lexicalVars() {
    return lexicalVars;
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
    return Iterators.forArray(staticToken, functionToken, referenceToken, parameters, lexicalVars, returnTypeClause, body);
  }

  @Override
  public void accept(VisitorCheck visitor) {
    visitor.visitFunctionExpression(this);
  }

}
