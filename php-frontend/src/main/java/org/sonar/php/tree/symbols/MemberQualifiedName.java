/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource SA
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
package org.sonar.php.tree.symbols;

import java.util.Locale;
import java.util.Objects;
import javax.annotation.Nullable;
import org.sonar.plugins.php.api.symbols.QualifiedName;
import org.sonar.plugins.php.api.symbols.Symbol.Kind;

class MemberQualifiedName implements QualifiedName {

  private final QualifiedName owner;
  private final String name;

  MemberQualifiedName(QualifiedName owner, String name, @Nullable Kind kind) {
    this.owner = owner;
    this.name = Kind.FIELD == kind ? name : name.toLowerCase(Locale.ROOT);
  }

  @Override
  public String toString() {
    return owner.toString() + "::" + name;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    MemberQualifiedName that = (MemberQualifiedName) o;
    return owner.equals(that.owner) &&
      name.equals(that.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(owner, name);
  }

  @Override
  public String simpleName() {
    return name;
  }
}
