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
package org.sonar.php.tree.impl.declaration;

import java.util.Iterator;
import java.util.List;
import javax.annotation.Nullable;
import org.sonar.php.symbols.MethodSymbol;
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.php.tree.impl.lexical.InternalSyntaxToken;
import org.sonar.php.tree.symbols.HasMethodSymbol;
import org.sonar.php.utils.collections.IteratorUtils;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.AttributeGroupTree;
import org.sonar.plugins.php.api.tree.declaration.MethodDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.ParameterListTree;
import org.sonar.plugins.php.api.tree.declaration.ReturnTypeClauseTree;
import org.sonar.plugins.php.api.tree.expression.NameIdentifierTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.visitors.VisitorCheck;

public class MethodDeclarationTreeImpl extends PHPTree implements MethodDeclarationTree, HasMethodSymbol {

  private static final Kind KIND = Kind.METHOD_DECLARATION;

  private final List<AttributeGroupTree> attributeGroups;
  private final List<SyntaxToken> modifiersToken;
  private final InternalSyntaxToken functionToken;
  private final InternalSyntaxToken referenceToken;
  private final NameIdentifierTree name;
  private final ParameterListTree parameters;
  private final ReturnTypeClauseTree returnTypeClause;
  private final Tree body;
  private MethodSymbol symbol;

  public MethodDeclarationTreeImpl(
    List<AttributeGroupTree> attributeGroups,
    List<SyntaxToken> modifiersToken,
    InternalSyntaxToken functionToken,
    @Nullable InternalSyntaxToken referenceToken,
    NameIdentifierTree name,
    ParameterListTree parameters,
    @Nullable ReturnTypeClauseTree returnTypeClause,
    Tree body) {
    this.attributeGroups = attributeGroups;
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
      IteratorUtils.iteratorOf(functionToken, referenceToken, name, parameters, returnTypeClause, body));
  }

  @Override
  public void accept(VisitorCheck visitor) {
    visitor.visitMethodDeclaration(this);
  }

  public void setSymbol(MethodSymbol symbol) {
    this.symbol = symbol;
  }

  @Override
  public MethodSymbol symbol() {
    return symbol;
  }
}
