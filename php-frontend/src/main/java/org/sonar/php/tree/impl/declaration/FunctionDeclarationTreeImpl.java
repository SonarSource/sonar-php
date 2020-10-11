/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2020 SonarSource SA
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
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.sonar.php.symbols.FunctionSymbol;
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.php.tree.impl.lexical.InternalSyntaxToken;
import org.sonar.php.tree.symbols.HasFunctionSymbol;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.AttributeGroupTree;
import org.sonar.plugins.php.api.tree.declaration.AttributeTree;
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
  private final List<AttributeTree> attributes;
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
    BlockTree body
  ) {
    this.attributeGroups = attributeGroups;
    this.attributes = attributeGroups.stream().flatMap(g -> g.attributes().stream()).collect(Collectors.toList());
    this.functionToken = functionToken;
    this.referenceToken = referenceToken;
    this.name = name;
    this.parameters = parameters;
    this.returnTypeClause = returnTypeClause;
    this.body = body;
  }

  @Override
  public List<AttributeTree> attributes() {
    return attributes;
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
    return Iterators.concat(
      attributeGroups.iterator(),
      Iterators.forArray(functionToken, referenceToken, name, parameters, returnTypeClause, body)
    );
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
