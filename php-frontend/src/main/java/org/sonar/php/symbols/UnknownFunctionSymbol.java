package org.sonar.php.symbols;

import org.sonar.plugins.php.api.symbols.QualifiedName;
import org.sonar.plugins.php.api.visitors.LocationInFile;

public class UnknownFunctionSymbol implements FunctionSymbol {
  private final QualifiedName qualifiedName;

  public UnknownFunctionSymbol(QualifiedName qualifiedName) {
    this.qualifiedName = qualifiedName;
  }

  @Override
  public LocationInFile location() {
    return UnknownLocationInFile.UNKNOWN_LOCATION;
  }

  @Override
  public QualifiedName qualifiedName() {
    return qualifiedName;
  }

  @Override
  public boolean isUnknownSymbol() {
    return true;
  }
}
