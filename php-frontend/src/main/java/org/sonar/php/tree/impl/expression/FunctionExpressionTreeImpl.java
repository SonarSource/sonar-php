/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2024 SonarSource SA
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
package org.sonar.php.tree.impl.expression;

import java.util.Iterator;
import java.util.List;
import javax.annotation.Nullable;
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.php.tree.impl.lexical.InternalSyntaxToken;
import org.sonar.php.utils.collections.IteratorUtils;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.AttributeGroupTree;
import org.sonar.plugins.php.api.tree.declaration.ParameterListTree;
import org.sonar.plugins.php.api.tree.declaration.ReturnTypeClauseTree;
import org.sonar.plugins.php.api.tree.expression.FunctionExpressionTree;
import org.sonar.plugins.php.api.tree.expression.LexicalVariablesTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.tree.statement.BlockTree;
import org.sonar.plugins.php.api.visitors.VisitorCheck;

public class FunctionExpressionTreeImpl extends PHPTree implements FunctionExpressionTree {

  private static final Kind KIND = Kind.FUNCTION_EXPRESSION;
  private final List<AttributeGroupTree> attributeGroups;
  private final InternalSyntaxToken staticToken;
  private final InternalSyntaxToken functionToken;
  private final InternalSyntaxToken referenceToken;
  private final ParameterListTree parameters;
  private final LexicalVariablesTree lexicalVars;
  private final ReturnTypeClauseTree returnTypeClause;
  private final BlockTree body;

  public FunctionExpressionTreeImpl(
    List<AttributeGroupTree> attributeGroups,
    @Nullable InternalSyntaxToken staticToken,
    InternalSyntaxToken functionToken,
    @Nullable InternalSyntaxToken referenceToken,
    ParameterListTree parameters,
    @Nullable LexicalVariablesTree lexicalVars,
    @Nullable ReturnTypeClauseTree returnTypeClause,
    BlockTree body) {
    this.attributeGroups = attributeGroups;
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
  public List<AttributeGroupTree> attributeGroups() {
    return attributeGroups;
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
    return IteratorUtils.concat(
      attributeGroups.listIterator(),
      IteratorUtils.iteratorOf(staticToken, functionToken, referenceToken, parameters, lexicalVars, returnTypeClause, body));
  }

  @Override
  public void accept(VisitorCheck visitor) {
    visitor.visitFunctionExpression(this);
  }

}
