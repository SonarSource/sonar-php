/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2024 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
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
