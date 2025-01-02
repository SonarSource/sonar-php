/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource SA
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
package org.sonar.php.tree.impl;

import java.util.Iterator;
import org.sonar.php.tree.symbols.SymbolImpl;
import org.sonar.php.utils.collections.IteratorUtils;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.expression.IdentifierTree;
import org.sonar.plugins.php.api.tree.expression.VariableIdentifierTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.visitors.VisitorCheck;

public class VariableIdentifierTreeImpl extends PHPTree implements VariableIdentifierTree {

  private final SyntaxToken token;
  private static final Kind KIND = Kind.VARIABLE_IDENTIFIER;
  private SymbolImpl symbol;

  public VariableIdentifierTreeImpl(SyntaxToken token) {
    this.token = token;
  }

  @Override
  public Kind getKind() {
    return KIND;
  }

  @Override
  public Iterator<Tree> childrenIterator() {
    return IteratorUtils.iteratorOf(token);
  }

  @Override
  public void accept(VisitorCheck visitor) {
    visitor.visitVariableIdentifier(this);
  }

  @Override
  public SyntaxToken token() {
    return token;
  }

  @Override
  public String text() {
    return token.text();
  }

  @Override
  public IdentifierTree variableExpression() {
    return this;
  }

  public void setSymbol(SymbolImpl symbol) {
    this.symbol = symbol;
  }

  public SymbolImpl symbol() {
    return symbol;
  }
}
