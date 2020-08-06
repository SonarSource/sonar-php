package org.sonar.php.symbols;

import org.sonar.plugins.php.api.symbols.QualifiedName;
import org.sonar.plugins.php.api.visitors.LocationInFile;

public interface FunctionSymbol extends Symbol {
  LocationInFile location();

  QualifiedName qualifiedName();

}
