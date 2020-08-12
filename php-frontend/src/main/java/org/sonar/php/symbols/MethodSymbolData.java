package org.sonar.php.symbols;

import org.sonar.plugins.php.api.symbols.QualifiedName;
import org.sonar.plugins.php.api.visitors.LocationInFile;

import java.util.List;

public class MethodSymbolData extends FunctionSymbolData {
  private String visibility;
  private QualifiedName className;

  public MethodSymbolData(LocationInFile location,
    QualifiedName qualifiedName,
    List<Parameter> parameters,
    boolean hasReturn,
    String visibility,
    QualifiedName className) {
    super(location, qualifiedName, parameters, hasReturn);
    this.visibility = visibility;
    this.className = className;
  }

  public String visibility() {
    return visibility;
  }

  public QualifiedName className() {
    return className;
  }
}
