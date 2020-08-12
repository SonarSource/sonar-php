package org.sonar.php.symbols;

import org.sonar.plugins.php.api.symbols.QualifiedName;
import org.sonar.plugins.php.api.visitors.LocationInFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MethodSymbolIndex {
  private final Map<QualifiedName, MethodSymbol> symbolsByQualifiedName = new HashMap<>();
  private final Map<MethodSymbolData, MethodSymbol> symbolsByData = new HashMap<>();
  private final ProjectSymbolData projectSymbolData;

  public MethodSymbolIndex(ProjectSymbolData projectSymbolData) {
    this.projectSymbolData = projectSymbolData;
  }

  public static MethodSymbolIndex create(Set<MethodSymbolData> fileDeclarations, ProjectSymbolData projectSymbolData) {
    MethodSymbolIndex index = new MethodSymbolIndex(projectSymbolData);
    index.init(fileDeclarations);
    return index;
  }

  private void init(Set<MethodSymbolData> fileDeclarations) {
    for (MethodSymbolData data : fileDeclarations) {
      MethodSymbolIndex.MethodSymbolImpl symbol = new MethodSymbolIndex.MethodSymbolImpl(data);
      symbolsByQualifiedName.put(symbol.qualifiedName(), symbol);
      symbolsByData.put(data, symbol);
    }
  }

  public MethodSymbol get(QualifiedName qualifiedName) {
    return symbolsByQualifiedName.computeIfAbsent(qualifiedName, qn -> projectSymbolData.methodSymbolData(qn)
      .<MethodSymbol>map(MethodSymbolIndex.MethodSymbolImpl::new)
      .orElse(new UnknownMethodSymbol(qualifiedName)));
  }

  public MethodSymbol get(MethodSymbolData symbolData) {
    return symbolsByData.get(symbolData);
  }

  private static class MethodSymbolImpl implements MethodSymbol {

    private final MethodSymbolData data;

    private MethodSymbolImpl(MethodSymbolData data) {
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
    public List<Parameter> parameters() {
      return data.parameters();
    }

    @Override
    public boolean isUnknownSymbol() {
      return false;
    }

    @Override
    public String visibility() {
      return data.visibility();
    }

    @Override
    public QualifiedName className() {
      return data.className();
    }
  }
}
