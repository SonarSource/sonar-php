/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2026 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.php.highlighter;

import org.sonar.api.batch.sensor.symbol.NewSymbol;
import org.sonar.api.batch.sensor.symbol.NewSymbolTable;
import org.sonar.php.tree.symbols.SymbolTableImpl;
import org.sonar.plugins.php.api.symbols.Symbol;
import org.sonar.plugins.php.api.symbols.SymbolTable;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;

public class SymbolHighlighter {

  private SymbolHighlighter() {
  }

  public static void highlight(SymbolTable symbolTable, NewSymbolTable newSymbolTable) {
    for (Symbol symbol : ((SymbolTableImpl) symbolTable).getSymbols()) {
      SyntaxToken token = symbol.declaration().token();
      NewSymbol newSymbol = newSymbolTable.newSymbol(token.line(), token.column(), token.endLine(), token.endColumn());
      for (SyntaxToken usageToken : symbol.usages()) {
        newSymbol.newReference(usageToken.line(), usageToken.column(), usageToken.endLine(), usageToken.endColumn());
      }
    }
  }

}
