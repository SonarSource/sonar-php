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
import java.util.List;

import javax.annotation.Nullable;

import org.sonar.php.tree.impl.PHPTree;
import org.sonar.php.tree.impl.SeparatedList;
import org.sonar.php.tree.impl.lexical.InternalSyntaxToken;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.NamespaceNameTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.tree.statement.TraitAdaptationStatementTree;
import org.sonar.plugins.php.api.tree.statement.TraitUseStatementTree;
import org.sonar.plugins.php.api.visitors.TreeVisitor;

import com.google.common.base.Functions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;

public class TraitUseStatementTreeImpl extends PHPTree implements TraitUseStatementTree {

  private static final Kind KIND = Kind.TRAIT_USE_STATEMENT;

  private final InternalSyntaxToken useToken;
  private final SeparatedList<NamespaceNameTree> traits;
  private final InternalSyntaxToken openCurlyBraceToken;
  private final List<TraitAdaptationStatementTree> adaptations;
  private final InternalSyntaxToken closeCurlyBraceToken;
  private final InternalSyntaxToken eosToken;

  public TraitUseStatementTreeImpl(
    InternalSyntaxToken useToken,
    SeparatedList<NamespaceNameTree> traits,
    InternalSyntaxToken eosToken
    ) {
    this.useToken = useToken;
    this.traits = traits;
    this.openCurlyBraceToken = null;
    this.adaptations = ImmutableList.of();
    this.closeCurlyBraceToken = null;
    this.eosToken = eosToken;
  }

  public TraitUseStatementTreeImpl(
    InternalSyntaxToken useToken,
    SeparatedList<NamespaceNameTree> traits,
    InternalSyntaxToken openCurlyBrace,
    List<TraitAdaptationStatementTree> adaptations,
    InternalSyntaxToken closeCurlyBrace
    ) {
    this.useToken = useToken;
    this.traits = traits;
    this.openCurlyBraceToken = openCurlyBrace;
    this.adaptations = adaptations;
    this.closeCurlyBraceToken = closeCurlyBrace;
    this.eosToken = null;
  }

  @Override
  public SyntaxToken useToken() {
    return useToken;
  }

  @Override
  public SeparatedList<NamespaceNameTree> traits() {
    return traits;
  }

  @Nullable
  @Override
  public SyntaxToken openCurlyBraceToken() {
    return openCurlyBraceToken;
  }

  @Nullable
  @Override
  public List<TraitAdaptationStatementTree> adaptations() {
    return adaptations;
  }

  @Nullable
  @Override
  public SyntaxToken closeCurlyBraceToken() {
    return closeCurlyBraceToken;
  }

  @Nullable
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
      Iterators.singletonIterator(useToken),
      traits.elementsAndSeparators(Functions.<NamespaceNameTree>identity()),
      Iterators.singletonIterator(openCurlyBraceToken),
      adaptations.iterator(),
      Iterators.forArray(closeCurlyBraceToken, eosToken));
  }

  @Override
  public void accept(TreeVisitor visitor) {
    visitor.visitTraitUseStatement(this);
  }

}
