package org.sonar.php.symbols;

import org.sonar.plugins.php.api.symbols.QualifiedName;

public class UnknownMethodSymbol extends UnknownFunctionSymbol implements MethodSymbol {
  public UnknownMethodSymbol(QualifiedName qualifiedName) {
    super(qualifiedName);
  }

  @Override
  public String visibility() {
    return null;
  }

  @Override
  public QualifiedName className() {
    return qualifiedName;
  }
}
