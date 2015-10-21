/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010 SonarSource and Akram Ben Aissi
 * sonarqube@googlegroups.com
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
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.php.tree.impl.statement;

import com.google.common.collect.Iterators;
import org.sonar.php.api.PHPKeyword;
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.php.tree.impl.SeparatedListImpl;
import org.sonar.php.tree.impl.lexical.InternalSyntaxToken;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.tree.statement.UseClauseTree;
import org.sonar.plugins.php.api.tree.statement.UseStatementTree;
import org.sonar.plugins.php.api.visitors.VisitorCheck;

import javax.annotation.Nullable;
import java.util.Iterator;

public class UseStatementTreeImpl extends PHPTree implements UseStatementTree {

  private final Kind kind;
  private InternalSyntaxToken useToken;
  private InternalSyntaxToken useTypeToken;
  private SeparatedListImpl<UseClauseTree> declarations;
  private InternalSyntaxToken eosToken;

  public UseStatementTreeImpl(
      InternalSyntaxToken useToken,
      @Nullable InternalSyntaxToken useTypeToken,
      SeparatedListImpl<UseClauseTree> declarations,
      InternalSyntaxToken eosToken
  ) {
    this.useToken = useToken;
    this.useTypeToken = useTypeToken;
    this.declarations = declarations;
    this.eosToken = eosToken;

    if (useTypeToken == null) {
      this.kind = Kind.USE_STATEMENT;
    } else if (useTypeToken().text().equals(PHPKeyword.CONST.getValue())) {
      this.kind = Kind.USE_CONST_STATEMENT;
    } else {
      this.kind = Kind.USE_FUNCTION_STATEMENT;
    }
  }

  @Override
  public SyntaxToken useToken() {
    return useToken;
  }

  @Override
  public SyntaxToken useTypeToken() {
    return useTypeToken;
  }

  @Override
  public SeparatedListImpl<UseClauseTree> clauses() {
    return declarations;
  }

  @Override
  public SyntaxToken eosToken() {
    return eosToken;
  }

  @Override
  public Kind getKind() {
    return kind;
  }

  @Override
  public Iterator<Tree> childrenIterator() {
    return Iterators.concat(
      Iterators.forArray(useToken, useTypeToken),
      declarations.elementsAndSeparators(),
      Iterators.singletonIterator(eosToken)
      );
  }

  @Override
  public void accept(VisitorCheck visitor) {
    visitor.visitUseStatement(this);
  }
}

