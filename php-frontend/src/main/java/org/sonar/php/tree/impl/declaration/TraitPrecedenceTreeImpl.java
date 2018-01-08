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
package org.sonar.php.tree.impl.declaration;

import java.util.Iterator;

import org.sonar.php.tree.impl.PHPTree;
import org.sonar.php.tree.impl.SeparatedListImpl;
import org.sonar.php.tree.impl.lexical.InternalSyntaxToken;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.NamespaceNameTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.tree.statement.TraitMethodReferenceTree;
import org.sonar.plugins.php.api.tree.statement.TraitPrecedenceTree;
import org.sonar.plugins.php.api.visitors.VisitorCheck;

import com.google.common.collect.Iterators;

public class TraitPrecedenceTreeImpl extends PHPTree implements TraitPrecedenceTree {

  private static final Kind KIND = Kind.TRAIT_PRECEDENCE;

  private final TraitMethodReferenceTree methodReference;
  private final InternalSyntaxToken insteadOfToken;
  private final SeparatedListImpl<NamespaceNameTree> traits;
  private final InternalSyntaxToken eosToken;

  public TraitPrecedenceTreeImpl(
    TraitMethodReferenceTree methodReference,
    InternalSyntaxToken insteadOfToken,
    SeparatedListImpl<NamespaceNameTree> traits,
    InternalSyntaxToken eosToken
    ) {
    this.methodReference = methodReference;
    this.insteadOfToken = insteadOfToken;
    this.traits = traits;
    this.eosToken = eosToken;
  }

  @Override
  public TraitMethodReferenceTree methodReference() {
    return methodReference;
  }

  @Override
  public SyntaxToken insteadOfToken() {
    return insteadOfToken;
  }

  @Override
  public SeparatedListImpl<NamespaceNameTree> traits() {
    return traits;
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
      Iterators.forArray(methodReference, insteadOfToken),
      traits.elementsAndSeparators(),
      Iterators.singletonIterator(eosToken));
  }

  @Override
  public void accept(VisitorCheck visitor) {
    visitor.visitTraitPrecedence(this);
  }

}
