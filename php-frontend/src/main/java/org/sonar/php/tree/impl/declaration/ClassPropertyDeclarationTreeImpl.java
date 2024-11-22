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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nullable;
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.php.tree.impl.SeparatedListImpl;
import org.sonar.php.tree.impl.lexical.InternalSyntaxToken;
import org.sonar.php.utils.collections.IteratorUtils;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.AttributeGroupTree;
import org.sonar.plugins.php.api.tree.declaration.ClassPropertyDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.DeclaredTypeTree;
import org.sonar.plugins.php.api.tree.declaration.PropertyHookListTree;
import org.sonar.plugins.php.api.tree.declaration.TypeTree;
import org.sonar.plugins.php.api.tree.declaration.UnionTypeTree;
import org.sonar.plugins.php.api.tree.declaration.VariableDeclarationTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.visitors.VisitorCheck;

public class ClassPropertyDeclarationTreeImpl extends PHPTree implements ClassPropertyDeclarationTree {

  private final Kind kind;
  private final List<AttributeGroupTree> attributeGroups;
  private final List<SyntaxToken> modifierTokens;
  private final SeparatedListImpl<VariableDeclarationTree> declarations;
  @Nullable
  private final PropertyHookListTree propertyHookList;
  @Nullable
  private final InternalSyntaxToken eosToken;
  @Nullable
  private final DeclaredTypeTree typeAnnotation;

  private ClassPropertyDeclarationTreeImpl(
    Kind kind,
    List<AttributeGroupTree> attributeGroups,
    List<SyntaxToken> modifierTokens,
    @Nullable DeclaredTypeTree typeAnnotation,
    SeparatedListImpl<VariableDeclarationTree> declarations,
    @Nullable PropertyHookListTree propertyHookList,
    @Nullable InternalSyntaxToken eosToken) {
    this.kind = kind;
    this.attributeGroups = attributeGroups;
    this.modifierTokens = modifierTokens;
    this.typeAnnotation = typeAnnotation;
    this.declarations = declarations;
    this.propertyHookList = propertyHookList;
    this.eosToken = eosToken;
  }

  public static ClassPropertyDeclarationTree variable(List<AttributeGroupTree> attributes,
    List<SyntaxToken> modifierTokens,
    @Nullable DeclaredTypeTree typeAnnotation,
    SeparatedListImpl<VariableDeclarationTree> declarations,
    @Nullable PropertyHookListTree propertyHook,
    @Nullable InternalSyntaxToken eosToken) {
    return new ClassPropertyDeclarationTreeImpl(Kind.CLASS_PROPERTY_DECLARATION,
      attributes,
      Collections.unmodifiableList(modifierTokens),
      typeAnnotation,
      declarations,
      propertyHook,
      eosToken);
  }

  public static ClassPropertyDeclarationTree constant(List<AttributeGroupTree> attributes,
    List<SyntaxToken> modifiers,
    SyntaxToken constToken,
    @Nullable DeclaredTypeTree typeAnnotation,
    SeparatedListImpl<VariableDeclarationTree> declarations,
    InternalSyntaxToken eosToken) {

    List<SyntaxToken> modifierTokens = new ArrayList<>(modifiers);
    modifierTokens.add(constToken);
    return new ClassPropertyDeclarationTreeImpl(Kind.CLASS_CONSTANT_PROPERTY_DECLARATION,
      attributes,
      Collections.unmodifiableList(modifierTokens),
      typeAnnotation,
      declarations,
      null,
      eosToken);
  }

  @Override
  public List<AttributeGroupTree> attributeGroups() {
    return attributeGroups;
  }

  @Override
  public List<SyntaxToken> modifierTokens() {
    return modifierTokens;
  }

  /**
   * @deprecated since 3.11 - use {@link #declaredType()} instead.
   */
  @Nullable
  @Override
  @Deprecated
  public TypeTree typeAnnotation() {
    if (typeAnnotation == null) {
      return null;
    }

    if (typeAnnotation.is(Kind.TYPE)) {
      return (TypeTree) typeAnnotation;
    } else {
      return ((UnionTypeTree) typeAnnotation).types().get(0);
    }
  }

  @Nullable
  @Override
  public DeclaredTypeTree declaredType() {
    return typeAnnotation;
  }

  @Override
  public SeparatedListImpl<VariableDeclarationTree> declarations() {
    return declarations;
  }

  @Nullable
  @Override
  public PropertyHookListTree propertyHookList() {
    return propertyHookList;
  }

  @Nullable
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
    return IteratorUtils.concat(
      attributeGroups.iterator(),
      modifierTokens.iterator(),
      IteratorUtils.nullableIterator(typeAnnotation),
      declarations.elementsAndSeparators(),
      IteratorUtils.nullableIterator(propertyHookList),
      IteratorUtils.nullableIterator(eosToken));
  }

  @Override
  public void accept(VisitorCheck visitor) {
    visitor.visitClassPropertyDeclaration(this);
  }

}
