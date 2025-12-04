/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource Sàrl
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

import java.util.Iterator;
import java.util.List;
import javax.annotation.Nullable;
import org.sonar.php.tree.impl.SeparatedListImpl;
import org.sonar.php.tree.impl.lexical.InternalSyntaxToken;
import org.sonar.php.utils.collections.IteratorUtils;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.AttributeGroupTree;
import org.sonar.plugins.php.api.tree.declaration.ClassMemberTree;
import org.sonar.plugins.php.api.tree.declaration.EnumDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.NamespaceNameTree;
import org.sonar.plugins.php.api.tree.declaration.TypeTree;
import org.sonar.plugins.php.api.tree.expression.NameIdentifierTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.tree.statement.EnumCaseTree;

public class EnumDeclarationTreeImpl extends ClassDeclarationTreeImpl implements EnumDeclarationTree {

  private final SyntaxToken typeColonToken;
  private final TypeTree backingType;
  private final List<EnumCaseTree> cases;

  public EnumDeclarationTreeImpl(List<AttributeGroupTree> attributeGroups, SyntaxToken enumToken, NameIdentifierTree name,
    @Nullable SyntaxToken typeColonToken, @Nullable TypeTree backingType,
    @Nullable InternalSyntaxToken implementsToken, SeparatedListImpl<NamespaceNameTree> superInterfaces, SyntaxToken openCurlyBraceToken,
    List<ClassMemberTree> members, SyntaxToken closeCurlyBraceToken) {
    super(Kind.ENUM_DECLARATION, attributeGroups, List.of(), enumToken, name, null, null,
      implementsToken, superInterfaces, openCurlyBraceToken, members, closeCurlyBraceToken);
    this.typeColonToken = typeColonToken;
    this.backingType = backingType;
    this.cases = members.stream().filter(m -> m.is(Kind.ENUM_CASE)).map(EnumCaseTree.class::cast).toList();
  }

  @Nullable
  @Override
  public SyntaxToken typeColonToken() {
    return typeColonToken;
  }

  @Nullable
  @Override
  public TypeTree backingType() {
    return backingType;
  }

  @Override
  public List<EnumCaseTree> cases() {
    return cases;
  }

  @Override
  public Iterator<Tree> childrenIterator() {
    return IteratorUtils.concat(attributeGroups().iterator(),
      IteratorUtils.iteratorOf(classToken(), name(), typeColonToken, backingType, implementsToken()),
      superInterfaces().elementsAndSeparators(),
      IteratorUtils.iteratorOf(openCurlyBraceToken()),
      members().iterator(),
      IteratorUtils.iteratorOf(closeCurlyBraceToken()));
  }
}
