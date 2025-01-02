/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource SA
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

import org.sonar.php.symbols.ClassSymbol;
import org.sonar.plugins.php.api.tree.declaration.NamespaceNameTree;

public class ClassNamespaceNameTreeImpl extends NamespaceNameTreeImpl {

  private ClassSymbol symbol;

  public ClassNamespaceNameTreeImpl(NamespaceNameTree tree) {
    super(tree.absoluteSeparator(), tree.namespaces(), tree.name());
  }

  public ClassSymbol symbol() {
    return symbol;
  }

  public void setSymbol(ClassSymbol symbol) {
    this.symbol = symbol;
  }
}
