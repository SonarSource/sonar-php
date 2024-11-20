/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2024 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.php.symbols;

import java.util.Locale;
import org.assertj.core.api.AbstractAssert;

public class FunctionSymbolAssert extends AbstractAssert<FunctionSymbolAssert, FunctionSymbol> {

  public FunctionSymbolAssert(FunctionSymbol actual) {
    super(actual, FunctionSymbolAssert.class);
  }

  public static FunctionSymbolAssert assertThat(FunctionSymbol actual) {
    return new FunctionSymbolAssert(actual);
  }

  public FunctionSymbolAssert isKnown(String qualifiedName) {
    isNotNull();
    if (actual.isUnknownSymbol()) {
      failWithMessage("Expected function symbol to be known but was unknown");
    }
    if (!actual.qualifiedName().toString().equals(qualifiedName.toLowerCase(Locale.ROOT))) {
      failWithMessage("Expected function symbol to have qualified name %s but was %s", qualifiedName, actual.qualifiedName());
    }
    return this;
  }

  public FunctionSymbolAssert isUnknown() {
    isNotNull();
    if (!actual.isUnknownSymbol()) {
      failWithMessage("Expected function symbol to be unknown but was known (%s)", actual.qualifiedName());
    }
    return this;
  }

}
