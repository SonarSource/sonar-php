/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2023 SonarSource SA
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
import java.util.List;
import javax.annotation.Nullable;

import org.sonar.php.symbols.ClassSymbol;
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.php.tree.impl.SeparatedListImpl;
import org.sonar.php.tree.impl.lexical.InternalSyntaxToken;
import org.sonar.php.tree.symbols.HasClassSymbol;
import org.sonar.php.utils.collections.IteratorUtils;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.AttributeGroupTree;
import org.sonar.plugins.php.api.tree.declaration.ClassDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.ClassMemberTree;
import org.sonar.plugins.php.api.tree.declaration.MethodDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.NamespaceNameTree;
import org.sonar.plugins.php.api.tree.expression.NameIdentifierTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.visitors.VisitorCheck;

public class ClassDeclarationTreeImpl extends PHPTree implements ClassDeclarationTree, HasClassSymbol {

  private final Kind kind;

  private final List<AttributeGroupTree> attributeGroups;
  private final List<SyntaxToken> modifiersToken;
  private final SyntaxToken classEntryTypeToken;
  private final NameIdentifierTree name;
  private final SyntaxToken extendsToken;
  private final NamespaceNameTree superClass;
  private final SyntaxToken implementsToken;
  private final SeparatedListImpl<NamespaceNameTree> superInterfaces;
  private final SyntaxToken openCurlyBraceToken;
  private final List<ClassMemberTree> members;
  private final SyntaxToken closeCurlyBraceToken;
  private ClassSymbol symbol;

  protected ClassDeclarationTreeImpl(
      Kind kind,
      List<AttributeGroupTree> attributeGroups,
      List<SyntaxToken> modifiersToken, SyntaxToken classEntryTypeToken, NameIdentifierTree name,
      @Nullable SyntaxToken extendsToken, @Nullable NamespaceNameTree superClass,
      @Nullable SyntaxToken implementsToken, SeparatedListImpl<NamespaceNameTree> superInterfaces,
      SyntaxToken openCurlyBraceToken, List<ClassMemberTree> members, SyntaxToken closeCurlyBraceToken
  ) {
    this.kind = kind;
    this.attributeGroups = attributeGroups;
    this.modifiersToken = modifiersToken;
    this.classEntryTypeToken = classEntryTypeToken;
    this.name = name;
    this.extendsToken = extendsToken;
    this.superClass = superClass;
    this.implementsToken = implementsToken;
    this.superInterfaces = superInterfaces;
    this.openCurlyBraceToken = openCurlyBraceToken;
    this.members = members;
    this.closeCurlyBraceToken = closeCurlyBraceToken;
  }

  @Nullable
  @Override
  public SyntaxToken modifierToken() {
    return modifiersToken.stream()
            .filter(modifier -> modifier.text().equalsIgnoreCase("final")
              || modifier.text().equalsIgnoreCase("abstract"))
            .findFirst()
            .orElse(null);
  }

  @Override
  public List<SyntaxToken> modifiersToken() {
    return modifiersToken;
  }

  @Override
  public List<AttributeGroupTree> attributeGroups() {
    return attributeGroups;
  }

  @Override
  public SyntaxToken classToken() {
    return classEntryTypeToken;
  }

  @Override
  public NameIdentifierTree name() {
    return name;
  }

  @Nullable
  @Override
  public SyntaxToken extendsToken() {
    return extendsToken;
  }

  @Nullable
  @Override
  public NamespaceNameTree superClass() {
    return superClass;
  }

  @Nullable
  @Override
  public SyntaxToken implementsToken() {
    return implementsToken;
  }

  @Override
  public SeparatedListImpl<NamespaceNameTree> superInterfaces() {
    return superInterfaces;
  }

  @Override
  public SyntaxToken openCurlyBraceToken() {
    return openCurlyBraceToken;
  }

  @Override
  public List<ClassMemberTree> members() {
    return members;
  }

  @Override
  public SyntaxToken closeCurlyBraceToken() {
    return closeCurlyBraceToken;
  }

  @Override
  public boolean isAbstract() {
    return modifiersToken.stream().anyMatch(token -> token.text().equalsIgnoreCase("abstract"));
  }

  @Override
  public boolean isFinal() {
    return modifiersToken.stream().anyMatch(token -> token.text().equalsIgnoreCase("final"));
  }

  @Override
  public boolean isReadOnly() {
    return modifiersToken.stream().anyMatch(token -> token.text().equalsIgnoreCase("readonly"));
  }

  @Nullable
  @Override
  public MethodDeclarationTree fetchConstructor() {
    MethodDeclarationTree oldStyleConstructor = null;
    MethodDeclarationTree newStyleConstructor = null;

    for (ClassMemberTree member : members) {
      if (member.is(Kind.METHOD_DECLARATION)) {
        MethodDeclarationTree method = (MethodDeclarationTree) member;
        String methodName = method.name().text();

        if (name.text().equalsIgnoreCase(methodName)) {
          oldStyleConstructor = method;

        } else if (PHP5_CONSTRUCTOR_NAME.equalsIgnoreCase(methodName)) {
          newStyleConstructor = method;
        }
      }
    }
    return newStyleConstructor != null ? newStyleConstructor : oldStyleConstructor;
  }

  @Override
  public Kind getKind() {
    return kind;
  }

  @Override
  public Iterator<Tree> childrenIterator() {
    return IteratorUtils.concat(
      attributeGroups.iterator(),
      modifiersToken.iterator(),
      IteratorUtils.iteratorOf(classEntryTypeToken, name, extendsToken, superClass, implementsToken),
      superInterfaces.elementsAndSeparators(),
      IteratorUtils.iteratorOf(openCurlyBraceToken),
      members.iterator(),
      IteratorUtils.iteratorOf(closeCurlyBraceToken)
    );
  }

  @Override
  public void accept(VisitorCheck visitor) {
    visitor.visitClassDeclaration(this);
  }

  public static ClassDeclarationTree createInterface(
      List<AttributeGroupTree> attributes,
      InternalSyntaxToken interfaceToken, NameIdentifierTree name,
      @Nullable InternalSyntaxToken extendsToken, SeparatedListImpl<NamespaceNameTree> interfaceList,
      InternalSyntaxToken openCurlyBraceToken, List<ClassMemberTree> members, InternalSyntaxToken closeCurlyBraceToken
  ) {
    return new ClassDeclarationTreeImpl(
        Kind.INTERFACE_DECLARATION,
        attributes,
        List.of(),
        interfaceToken,
        name,
        extendsToken,
        null,
        null,
        interfaceList,
        openCurlyBraceToken,
        members,
        closeCurlyBraceToken
    );
  }

  public static ClassDeclarationTree createTrait(
      List<AttributeGroupTree> attributes,
      InternalSyntaxToken traitToken, NameIdentifierTree name,
      InternalSyntaxToken openCurlyBraceToken, List<ClassMemberTree> members, InternalSyntaxToken closeCurlyBraceToken
  ) {
    return new ClassDeclarationTreeImpl(
        Kind.TRAIT_DECLARATION,
        attributes,
        List.of(),
        traitToken,
        name,
        null,
        null,
        null,
        SeparatedListImpl.empty(),
        openCurlyBraceToken,
        members,
        closeCurlyBraceToken
    );
  }

  public static ClassDeclarationTree createClass(
      List<AttributeGroupTree> attributes, List<SyntaxToken> modifiersToken,
      InternalSyntaxToken classToken, NameIdentifierTree name,
      @Nullable InternalSyntaxToken extendsToken, @Nullable NamespaceNameTree superClass,
      @Nullable InternalSyntaxToken implementsToken, SeparatedListImpl<NamespaceNameTree> superInterfaces,
      InternalSyntaxToken openCurlyBraceToken, List<ClassMemberTree> members, InternalSyntaxToken closeCurlyBraceToken) {
    return new ClassDeclarationTreeImpl(
        Kind.CLASS_DECLARATION,
        attributes,
        modifiersToken,
        classToken,
        name,
        extendsToken,
        superClass,
        implementsToken,
        superInterfaces,
        openCurlyBraceToken,
        members,
        closeCurlyBraceToken
    );
  }

  public ClassSymbol symbol() {
    return symbol;
  }

  public void setSymbol(ClassSymbol symbol) {
    this.symbol = symbol;
  }
}
