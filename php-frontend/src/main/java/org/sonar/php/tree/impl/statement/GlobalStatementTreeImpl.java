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
package org.sonar.php.tree.impl.statement;

import java.util.Iterator;
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.php.tree.impl.SeparatedListImpl;
import org.sonar.php.tree.impl.lexical.InternalSyntaxToken;
import org.sonar.php.utils.collections.IteratorUtils;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.expression.VariableTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.tree.statement.GlobalStatementTree;
import org.sonar.plugins.php.api.visitors.VisitorCheck;

public class GlobalStatementTreeImpl extends PHPTree implements GlobalStatementTree {

  private static final Kind KIND = Kind.GLOBAL_STATEMENT;

  private final InternalSyntaxToken globalToken;
  private final SeparatedListImpl<VariableTree> variables;
  private final InternalSyntaxToken eosToken;

  public GlobalStatementTreeImpl(InternalSyntaxToken globalToken, SeparatedListImpl<VariableTree> variables, InternalSyntaxToken eosToken) {
    this.globalToken = globalToken;
    this.variables = variables;
    this.eosToken = eosToken;
  }

  @Override
  public SyntaxToken globalToken() {
    return globalToken;
  }

  @Override
  public SeparatedListImpl<VariableTree> variables() {
    return variables;
  }

  @Override
  public SyntaxToken eosToken() {
    return eosToken;
  }

  @Override
  public Kind getKind() {
    return KIND;
  }

  @Override
  public Iterator<Tree> childrenIterator() {
    return IteratorUtils.concat(
      IteratorUtils.iteratorOf(globalToken),
      variables.elementsAndSeparators(),
      IteratorUtils.iteratorOf(eosToken));
  }

  @Override
  public void accept(VisitorCheck visitor) {
    visitor.visitGlobalStatement(this);
  }
}
