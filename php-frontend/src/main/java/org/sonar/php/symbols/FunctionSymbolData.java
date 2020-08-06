package org.sonar.php.symbols;

import org.sonar.plugins.php.api.symbols.QualifiedName;
import org.sonar.plugins.php.api.visitors.LocationInFile;

public class FunctionSymbolData {
  private final LocationInFile location;
  private final QualifiedName qualifiedName;

  public FunctionSymbolData(LocationInFile location, QualifiedName qualifiedName) {
    this.location = location;
    this.qualifiedName = qualifiedName;
  }

  public LocationInFile location() {
    return location;
  }

  public QualifiedName qualifiedName() {
    return qualifiedName;
  }
}
