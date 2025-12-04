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
package org.sonar.php.tree.symbols;

import java.util.ArrayList;
import java.util.List;
import org.sonar.plugins.php.api.symbols.MemberSymbol;
import org.sonar.plugins.php.api.symbols.Symbol;
import org.sonar.plugins.php.api.symbols.TypeSymbol;
import org.sonar.plugins.php.api.tree.expression.IdentifierTree;

public class TypeSymbolImpl extends SymbolImpl implements TypeSymbol {

  private Symbol superClass;
  private List<Symbol> interfaces = new ArrayList<>();
  private List<MemberSymbol> members = new ArrayList<>();

  public TypeSymbolImpl(IdentifierTree declaration, Scope scope, SymbolQualifiedName qualifiedName) {
    super(declaration, Symbol.Kind.CLASS, scope, qualifiedName);
  }

  void setSuperClass(Symbol superClass) {
    this.superClass = superClass;
  }

  void addInterface(Symbol iface) {
    interfaces.add(iface);
  }

  void addMember(MemberSymbol member) {
    members.add(member);
  }

  @Override
  public Symbol superClass() {
    return this.superClass;
  }

  @Override
  public List<Symbol> interfaces() {
    return interfaces;
  }

  @Override
  public List<MemberSymbol> members() {
    return members;
  }
}
