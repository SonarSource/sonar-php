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
package org.sonar.php.tree.symbols;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import org.sonar.api.utils.Preconditions;
import org.sonar.plugins.php.api.symbols.QualifiedName;
import org.sonar.plugins.php.api.symbols.Symbol;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.IdentifierTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;

public class SymbolImpl implements Symbol {

  private final String name;
  private final IdentifierTree declaration;
  private QualifiedName qualifiedName;
  private Kind kind;
  private Scope scope;
  private List<SyntaxToken> usages = new LinkedList<>();
  private List<SyntaxToken> modifiers = new LinkedList<>();
  private List<ExpressionTree> assignedValues = new LinkedList<>();
  private boolean assignedUnknown = false;

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
  public List<SyntaxToken> modifiers() {
    return modifiers;
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
    if (kind == Kind.VARIABLE || kind == Kind.PARAMETER || kind == Kind.FIELD) {
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

  public void assignValue(ExpressionTree value) {
    assignedValues.add(value);
  }

  public void assignUnknown() {
    assignedUnknown = true;
  }

  public Optional<ExpressionTree> uniqueAssignedValue() {
    return !assignedUnknown && assignedValues.size() == 1 ? Optional.of(assignedValues.get(0)) : Optional.empty();
  }
}
