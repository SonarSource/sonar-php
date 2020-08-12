package org.sonar.php.symbols;

import org.sonar.plugins.php.api.symbols.QualifiedName;

public interface MethodSymbol extends FunctionSymbol {
  String visibility();

  QualifiedName className();
}
