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

import javax.annotation.Nullable;

import org.sonar.php.tree.impl.PHPTree;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.NamespaceNameTree;
import org.sonar.plugins.php.api.tree.expression.NameIdentifierTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.tree.statement.TraitMethodReferenceTree;
import org.sonar.plugins.php.api.visitors.VisitorCheck;

import com.google.common.collect.Iterators;

public class TraitMethodReferenceTreeImpl extends PHPTree implements TraitMethodReferenceTree {

  private static final Kind KIND = Kind.TRAIT_METHOD_REFERENCE;

  private final NamespaceNameTree trait;
  private final SyntaxToken doubleColonToken;
  private final NameIdentifierTree method;

  public TraitMethodReferenceTreeImpl(NameIdentifierTree method) {
    this.trait = null;
    this.doubleColonToken = null;
    this.method = method;
  }

  public TraitMethodReferenceTreeImpl(NamespaceNameTree trait, SyntaxToken doubleColonToken, NameIdentifierTree method) {
    this.trait = trait;
    this.doubleColonToken = doubleColonToken;
    this.method = method;
  }

  @Nullable
  @Override
  public NamespaceNameTree trait() {
    return trait;
  }

  @Nullable
  @Override
  public SyntaxToken doubleColonToken() {
    return doubleColonToken;
  }

  @Override
  public NameIdentifierTree method() {
    return method;
  }

  @Override
  public Kind getKind() {
    return KIND;
  }

  @Override
  public Iterator<Tree> childrenIterator() {
    return Iterators.forArray(trait, doubleColonToken, method);
  }

  @Override
  public void accept(VisitorCheck visitor) {
    visitor.visitTraitMethodReference(this);
  }

}
