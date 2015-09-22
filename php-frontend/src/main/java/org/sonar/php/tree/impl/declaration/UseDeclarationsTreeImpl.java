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
package org.sonar.php.tree.impl.declaration;

import java.util.Iterator;

import javax.annotation.Nullable;

import org.sonar.php.tree.impl.PHPTree;
import org.sonar.php.tree.impl.SeparatedList;
import org.sonar.php.tree.impl.lexical.InternalSyntaxToken;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.UseDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.UseDeclarationsTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.visitors.TreeVisitor;

import com.google.common.base.Functions;
import com.google.common.collect.Iterators;

public class UseDeclarationsTreeImpl extends PHPTree implements UseDeclarationsTree {

  private static final Kind KIND = Kind.USE_DECLARATIONS;
  private InternalSyntaxToken useToken;
  private InternalSyntaxToken useTypeToken;
  private SeparatedList<UseDeclarationTree> declarations;
  private InternalSyntaxToken eosToken;
  
  public UseDeclarationsTreeImpl(
    InternalSyntaxToken useToken, 
    @Nullable InternalSyntaxToken useTypeToken, 
    SeparatedList<UseDeclarationTree> declarations, 
    InternalSyntaxToken eosToken
    ) {
    this.useToken = useToken;
    this.useTypeToken = useTypeToken;
    this.declarations = declarations;
    this.eosToken = eosToken;
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
  public SeparatedList<UseDeclarationTree> declarations() {
    return declarations;
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
    return Iterators.concat(
      Iterators.forArray(useToken, useTypeToken),
      declarations.elementsAndSeparators(Functions.<UseDeclarationTree>identity()),
      Iterators.singletonIterator(eosToken)
      );
  }

  @Override
  public void accept(TreeVisitor visitor) {
    visitor.visitUseDeclarations(this);
  }
}

