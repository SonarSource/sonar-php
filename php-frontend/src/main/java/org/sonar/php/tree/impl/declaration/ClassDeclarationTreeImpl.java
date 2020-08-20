/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2020 SonarSource SA
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

import com.google.common.collect.Iterators;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nullable;
import org.sonar.php.symbols.ClassSymbol;
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.php.tree.impl.SeparatedListImpl;
import org.sonar.php.tree.impl.lexical.InternalSyntaxToken;
import org.sonar.php.tree.symbols.HasClassSymbol;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.ClassDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.ClassMemberTree;
import org.sonar.plugins.php.api.tree.declaration.ClassNamespaceNameTree;
import org.sonar.plugins.php.api.tree.declaration.MethodDeclarationTree;
import org.sonar.plugins.php.api.tree.expression.NameIdentifierTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.visitors.VisitorCheck;

public class ClassDeclarationTreeImpl extends PHPTree implements ClassDeclarationTree, HasClassSymbol {

  private final Kind kind;

  private final SyntaxToken modifierToken;
  private final SyntaxToken classEntryTypeToken;
  private final NameIdentifierTree name;
  private final SyntaxToken extendsToken;
  private final ClassNamespaceNameTree superClass;
  private final SyntaxToken implementsToken;
  private final SeparatedListImpl<ClassNamespaceNameTree> superInterfaces;
  private final SyntaxToken openCurlyBraceToken;
  private final List<ClassMemberTree> members;
  private final SyntaxToken closeCurlyBraceToken;
  private ClassSymbol symbol;

  private ClassDeclarationTreeImpl(
      Kind kind,
      @Nullable SyntaxToken modifierToken, SyntaxToken classEntryTypeToken, NameIdentifierTree name,
      @Nullable SyntaxToken extendsToken, @Nullable ClassNamespaceNameTree superClass,
      @Nullable SyntaxToken implementsToken, SeparatedListImpl<ClassNamespaceNameTree> superInterfaces,
      SyntaxToken openCurlyBraceToken, List<ClassMemberTree> members, SyntaxToken closeCurlyBraceToken
  ) {
    this.kind = kind;
    this.modifierToken = modifierToken;
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
    return modifierToken;
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
  public ClassNamespaceNameTree superClass() {
    return superClass;
  }

  @Nullable
  @Override
  public SyntaxToken implementsToken() {
    return implementsToken;
  }

  @Override
  public SeparatedListImpl<ClassNamespaceNameTree> superInterfaces() {
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
    return Iterators.concat(
        Iterators.forArray(modifierToken, classEntryTypeToken, name, extendsToken, superClass, implementsToken),
        superInterfaces.elementsAndSeparators(),
        Iterators.singletonIterator(openCurlyBraceToken),
        members.iterator(),
        Iterators.singletonIterator(closeCurlyBraceToken)
    );
  }

  @Override
  public void accept(VisitorCheck visitor) {
    visitor.visitClassDeclaration(this);
  }

  public static ClassDeclarationTree createInterface(
      InternalSyntaxToken interfaceToken, NameIdentifierTree name,
      @Nullable InternalSyntaxToken extendsToken, SeparatedListImpl<ClassNamespaceNameTree> interfaceList,
      InternalSyntaxToken openCurlyBraceToken, List<ClassMemberTree> members, InternalSyntaxToken closeCurlyBraceToken
  ) {
    return new ClassDeclarationTreeImpl(
        Kind.INTERFACE_DECLARATION,
        null,
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
      InternalSyntaxToken traitToken, NameIdentifierTree name,
      InternalSyntaxToken openCurlyBraceToken, List<ClassMemberTree> members, InternalSyntaxToken closeCurlyBraceToken
  ) {
    return new ClassDeclarationTreeImpl(
        Kind.TRAIT_DECLARATION,
        null,
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
      @Nullable InternalSyntaxToken modifierToken, InternalSyntaxToken classToken, NameIdentifierTree name,
      @Nullable InternalSyntaxToken extendsToken, @Nullable ClassNamespaceNameTree superClass,
      @Nullable InternalSyntaxToken implementsToken, SeparatedListImpl<ClassNamespaceNameTree> superInterfaces,
      InternalSyntaxToken openCurlyBraceToken, List<ClassMemberTree> members, InternalSyntaxToken closeCurlyBraceToken) {
    return new ClassDeclarationTreeImpl(
        Kind.CLASS_DECLARATION,
        modifierToken,
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
