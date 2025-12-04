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

import org.sonar.plugins.php.api.symbols.MemberSymbol;
import org.sonar.plugins.php.api.symbols.TypeSymbol;
import org.sonar.plugins.php.api.tree.expression.IdentifierTree;

public class MemberSymbolImpl extends SymbolImpl implements MemberSymbol {

  private final TypeSymbol owner;

  MemberSymbolImpl(IdentifierTree declaration, Kind kind, Scope scope, TypeSymbol owner) {
    super(declaration, kind, scope, new MemberQualifiedName(owner.qualifiedName(), declaration.text(), kind));
    this.owner = owner;
  }

  @Override
  public TypeSymbol owner() {
    return owner;
  }

}
