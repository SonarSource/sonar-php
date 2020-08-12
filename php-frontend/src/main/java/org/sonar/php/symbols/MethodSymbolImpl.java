package org.sonar.php.symbols;

import org.sonar.plugins.php.api.symbols.QualifiedName;
import org.sonar.plugins.php.api.visitors.LocationInFile;

import java.util.List;

public class MethodSymbolImpl implements MethodSymbol {
  private final MethodSymbolData data;

  public MethodSymbolImpl(MethodSymbolData data) {
    this.data = data;
  }

  @Override
  public String visibility() {
    return data.visibility();
  }

  @Override
  public QualifiedName className() {
    return data.className();
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
}
