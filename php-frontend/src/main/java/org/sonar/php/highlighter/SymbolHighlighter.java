/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2017 SonarSource SA
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
package org.sonar.php.highlighter;

import org.sonar.api.batch.sensor.symbol.NewSymbol;
import org.sonar.api.batch.sensor.symbol.NewSymbolTable;
import org.sonar.php.tree.symbols.SymbolTableImpl;
import org.sonar.plugins.php.api.symbols.Symbol;
import org.sonar.plugins.php.api.symbols.SymbolTable;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;

public class SymbolHighlighter {

  public void highlight(SymbolTable symbolTable, NewSymbolTable newSymbolTable) {
    for (Symbol symbol : ((SymbolTableImpl) symbolTable).getSymbols()) {
      SyntaxToken token = symbol.declaration().token();
      NewSymbol newSymbol = newSymbolTable.newSymbol(token.line(), token.column(), token.endLine(), token.endColumn());
      for (SyntaxToken usageToken : symbol.usages()) {
        // we do not highlight cases like "${someVar}" as such usages are shorter and will cause incorrect highlighting
        if (usageToken.text().length() == token.text().length()) {
          newSymbol.newReference(usageToken.line(), usageToken.column(), usageToken.endLine(), usageToken.endColumn());
        }
      }
    }
  }

}
