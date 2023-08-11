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
package org.sonar.php.symbols;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.sonar.plugins.php.api.symbols.QualifiedName;
import org.sonar.plugins.php.api.visitors.LocationInFile;

public class FunctionSymbolIndex {
  private final Map<QualifiedName, FunctionSymbol> symbolsByQualifiedName = new HashMap<>();
  private final Map<FunctionSymbolData, FunctionSymbol> symbolsByData = new HashMap<>();
  private final ProjectSymbolData projectSymbolData;

  public FunctionSymbolIndex(ProjectSymbolData projectSymbolData) {
    this.projectSymbolData = projectSymbolData;
  }

  public static FunctionSymbolIndex create(Set<FunctionSymbolData> fileDeclarations, ProjectSymbolData projectSymbolData) {
    FunctionSymbolIndex index = new FunctionSymbolIndex(projectSymbolData);
    index.init(fileDeclarations);
    return index;
  }

  private void init(Set<FunctionSymbolData> fileDeclarations) {
    for (FunctionSymbolData data : fileDeclarations) {
      FunctionSymbolIndex.FunctionSymbolImpl symbol = new FunctionSymbolIndex.FunctionSymbolImpl(data);
      symbolsByQualifiedName.put(symbol.qualifiedName(), symbol);
      symbolsByData.put(data, symbol);
    }
  }

  public FunctionSymbol get(QualifiedName qualifiedName) {
    return symbolsByQualifiedName.computeIfAbsent(qualifiedName, qn -> {
      List<FunctionSymbolData> all = projectSymbolData.functionSymbolData(qn);
      if (all.size() == 1) {
        return new FunctionSymbolImpl(all.get(0));
      } else {
        return new UnknownFunctionSymbol(qualifiedName);
      }
    });
  }

  public FunctionSymbol get(FunctionSymbolData symbolData) {
    return symbolsByData.get(symbolData);
  }

  protected static class FunctionSymbolImpl implements FunctionSymbol {

    private final FunctionSymbolData data;

    protected FunctionSymbolImpl(FunctionSymbolData data) {
      this.data = data;
    }

    @Override
    public LocationInFile location() {
      return data.location();
    }

    @Override
    public QualifiedName qualifiedName() {
      return data.qualifiedName();
    }

    @Override
    public boolean hasReturn() {
      return data.hasReturn();
    }

    @Override
    public boolean hasFuncGetArgs() {
      return data.hasFuncGetArgs();
    }

    @Override
    public List<Parameter> parameters() {
      return data.parameters();
    }

    @Override
    public boolean isUnknownSymbol() {
      return false;
    }
  }
}
