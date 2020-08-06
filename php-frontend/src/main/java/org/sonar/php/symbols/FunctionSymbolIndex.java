package org.sonar.php.symbols;

import org.sonar.plugins.php.api.symbols.QualifiedName;
import org.sonar.plugins.php.api.visitors.LocationInFile;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class FunctionSymbolIndex {
  private final Map<QualifiedName, FunctionSymbol> symbolsByQualifiedName = new HashMap<>();
  private final Map<FunctionSymbolData, FunctionSymbolIndex.FunctionSymbolImpl> symbolsByData = new HashMap<>();
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

  public FunctionSymbol get(FunctionSymbolData symbolData) {
    return symbolsByData.get(symbolData);
  }

  private static class FunctionSymbolImpl implements FunctionSymbol{

    private final FunctionSymbolData data;

    public FunctionSymbolImpl(FunctionSymbolData data) {
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
    public boolean isUnknownSymbol() {
      return false;
    }
  }
}
