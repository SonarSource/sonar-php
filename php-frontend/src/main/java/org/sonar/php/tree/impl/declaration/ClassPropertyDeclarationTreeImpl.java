/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2019 SonarSource SA
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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nullable;
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.php.tree.impl.SeparatedListImpl;
import org.sonar.php.tree.impl.lexical.InternalSyntaxToken;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.ClassPropertyDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.TypeTree;
import org.sonar.plugins.php.api.tree.declaration.VariableDeclarationTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.visitors.VisitorCheck;

public class ClassPropertyDeclarationTreeImpl extends PHPTree implements ClassPropertyDeclarationTree {

  private final Kind kind;
  private final List<SyntaxToken> modifierTokens;
  private final SeparatedListImpl<VariableDeclarationTree> declarations;
  private final InternalSyntaxToken eosToken;
  private final TypeTree typeAnnotation;

  private ClassPropertyDeclarationTreeImpl(
    Kind kind,
    List<SyntaxToken> modifierTokens,
    @Nullable TypeTree typeAnnotation,
    SeparatedListImpl<VariableDeclarationTree> declarations,
    InternalSyntaxToken eosToken
  ) {
    this.kind = kind;
    this.modifierTokens = modifierTokens;
    this.typeAnnotation = typeAnnotation;
    this.declarations = declarations;
    this.eosToken = eosToken;
  }

  public static ClassPropertyDeclarationTree variable(List<SyntaxToken> modifierTokens,
                                                      @Nullable TypeTree typeAnnotation,
                                                      SeparatedListImpl<VariableDeclarationTree> declarations,
                                                      InternalSyntaxToken eosToken) {
    return new ClassPropertyDeclarationTreeImpl(Kind.CLASS_PROPERTY_DECLARATION, modifierTokens, typeAnnotation,
      declarations, eosToken);
  }

  public static ClassPropertyDeclarationTree constant(@Nullable SyntaxToken visibility, SyntaxToken constToken,
                                                      SeparatedListImpl<VariableDeclarationTree> declarations,
                                                      InternalSyntaxToken eosToken) {

    List<SyntaxToken> modifierTokens;
    if (visibility != null) {
      modifierTokens = ImmutableList.of(visibility, constToken);
    } else {
      modifierTokens = ImmutableList.of(constToken);
    }
    return new ClassPropertyDeclarationTreeImpl(Kind.CLASS_CONSTANT_PROPERTY_DECLARATION, modifierTokens, null, declarations, eosToken);
  }

  @Override
  public List<SyntaxToken> modifierTokens() {
    return modifierTokens;
  }

  @Nullable
  @Override
  public TypeTree typeAnnotation() {
    return typeAnnotation;
  }

  @Override
  public SeparatedListImpl<VariableDeclarationTree> declarations() {
    return declarations;
  }

  @Override
  public SyntaxToken eosToken() {
    return eosToken;
  }

  @Override
  public boolean hasModifiers(String... modifiers) {
    int counter = 0;
    for (String modifier : modifiers) {
      if (hasModifier(modifier)) {
        counter++;
      }
    }
    return counter == modifiers.length;
  }

  public boolean hasModifier(String modifier) {
    for (SyntaxToken token : modifierTokens()) {
      if (token.text().equals(modifier)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public Kind getKind() {
    return kind;
  }

  @Override
  public Iterator<Tree> childrenIterator() {
    return Iterators.concat(
      modifierTokens.iterator(),
      nullableIterator(typeAnnotation),
      declarations.elementsAndSeparators(),
      Iterators.singletonIterator(eosToken));
  }

  private static Iterator<? extends Tree> nullableIterator(@Nullable Tree tree) {
    return tree == null ? Collections.emptyIterator() : Iterators.singletonIterator(tree);
  }

  @Override
  public void accept(VisitorCheck visitor) {
    visitor.visitClassPropertyDeclaration(this);
  }

}
