/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2026 SonarSource Sàrl
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
import org.sonar.php.symbols.FunctionSymbol;
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.php.tree.impl.lexical.InternalSyntaxToken;
import org.sonar.php.tree.symbols.HasFunctionSymbol;
import org.sonar.php.utils.collections.IteratorUtils;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.AttributeGroupTree;
import org.sonar.plugins.php.api.tree.declaration.FunctionDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.ParameterListTree;
import org.sonar.plugins.php.api.tree.declaration.ReturnTypeClauseTree;
import org.sonar.plugins.php.api.tree.expression.NameIdentifierTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.tree.statement.BlockTree;
import org.sonar.plugins.php.api.visitors.VisitorCheck;

public class FunctionDeclarationTreeImpl extends PHPTree implements FunctionDeclarationTree, HasFunctionSymbol {

  private static final Kind KIND = Kind.FUNCTION_DECLARATION;

  private final List<AttributeGroupTree> attributeGroups;
  private final InternalSyntaxToken functionToken;
  private final InternalSyntaxToken referenceToken;
  private final NameIdentifierTree name;
  private final ParameterListTree parameters;
  private final ReturnTypeClauseTree returnTypeClause;
  private final BlockTree body;
  private FunctionSymbol symbol;

  public FunctionDeclarationTreeImpl(
    List<AttributeGroupTree> attributeGroups,
    InternalSyntaxToken functionToken,
    @Nullable InternalSyntaxToken referenceToken,
    NameIdentifierTree name,
    ParameterListTree parameters,
    @Nullable ReturnTypeClauseTree returnTypeClause,
    BlockTree body) {
    this.attributeGroups = attributeGroups;
    this.functionToken = functionToken;
    this.referenceToken = referenceToken;
    this.name = name;
    this.parameters = parameters;
    this.returnTypeClause = returnTypeClause;
    this.body = body;
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
    return IteratorUtils.concat(
      attributeGroups.iterator(),
      IteratorUtils.iteratorOf(functionToken, referenceToken, name, parameters, returnTypeClause, body));
  }

  @Override
  public void accept(VisitorCheck visitor) {
    visitor.visitFunctionDeclaration(this);
  }

  public FunctionSymbol symbol() {
    return symbol;
  }

  public void setSymbol(FunctionSymbol symbol) {
    this.symbol = symbol;
  }
}
