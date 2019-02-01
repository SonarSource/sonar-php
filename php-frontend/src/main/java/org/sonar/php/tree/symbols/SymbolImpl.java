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
package org.sonar.php.tree.symbols;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import java.util.LinkedList;
import java.util.List;
import org.sonar.plugins.php.api.symbols.QualifiedName;
import org.sonar.plugins.php.api.symbols.Symbol;
import org.sonar.plugins.php.api.tree.expression.IdentifierTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;

@Beta
public class SymbolImpl implements Symbol {

  private final String name;
  private QualifiedName qualifiedName;
  private final IdentifierTree declaration;
  private Kind kind;
  private Scope scope;
  private List<SyntaxToken> usages = new LinkedList<>();
  private List<SyntaxToken> modifiers = new LinkedList<>();

  SymbolImpl(IdentifierTree declaration, Kind kind, Scope scope) {
    Preconditions.checkState(!kind.hasQualifiedName(), "Declaration of %s should provide qualified name", declaration);
    this.declaration = declaration;
    this.name = declaration.text();
    this.kind = kind;
    this.scope = scope;
  }

  SymbolImpl(IdentifierTree declaration, Kind kind, Scope scope, QualifiedName qualifiedName) {
    Preconditions.checkState(kind.hasQualifiedName(), "Declaration %s can not have qualified name %s", declaration, qualifiedName);
    this.declaration = declaration;
    this.name = qualifiedName.simpleName();
    this.qualifiedName = qualifiedName;
    this.kind = kind;
    this.scope = scope;
  }

  SymbolImpl(QualifiedName qualifiedName, Kind kind) {
    this.name = qualifiedName.simpleName();
    this.qualifiedName = qualifiedName;
    this.kind = kind;
    this.declaration = null;
  }

  @Override
  public ImmutableList<SyntaxToken> modifiers() {
    return ImmutableList.copyOf(modifiers);
  }

  @Override
  public boolean hasModifier(String modifier) {
    for (SyntaxToken syntaxToken : modifiers) {
      if (syntaxToken.text().equalsIgnoreCase(modifier)) {
        return true;
      }
    }
    return false;
  }

  void addModifiers(List<SyntaxToken> modifiers) {
    this.modifiers.addAll(modifiers);
  }

  void addUsage(SyntaxToken usage) {
    usages.add(usage);
  }

  void addUsage(IdentifierTree usage) {
    addUsage(usage.token());
  }

  @Override
  public List<SyntaxToken> usages() {
    return usages;
  }

  public Scope scope() {
    return scope;
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public QualifiedName qualifiedName() {
    return qualifiedName;
  }

  @Override
  public IdentifierTree declaration() {
    return declaration;
  }

  @Override
  public boolean is(Symbol.Kind kind) {
    return kind.equals(this.kind);
  }

  @Override
  public boolean called(String name) {
    if (kind == Kind.VARIABLE || kind == Kind.PARAMETER) {
      return name.equals(this.name);
    } else {
      return name.equalsIgnoreCase(this.name);
    }
  }

  @Override
  public Kind kind() {
    return kind;
  }

  @Override
  public String toString() {
    return "SymbolImpl{" +
      "name='" + name + '\'' +
      ", qualifiedName='" + qualifiedName() + '\'' +
      ", kind=" + kind +
      ", scope=" + scope +
      '}';
  }
}
