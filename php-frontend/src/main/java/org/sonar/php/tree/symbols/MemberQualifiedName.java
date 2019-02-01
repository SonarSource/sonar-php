/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2019 SonarSource SA
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
package org.sonar.php.tree.symbols;

import com.google.common.base.Preconditions;
import java.util.Locale;
import java.util.Objects;
import javax.annotation.Nullable;
import org.sonar.plugins.php.api.symbols.QualifiedName;

class MemberQualifiedName implements QualifiedName {

  private final QualifiedName owner;
  private final String name;

  MemberQualifiedName(QualifiedName owner, String name) {
    this.owner = Preconditions.checkNotNull(owner);
    this.name = Preconditions.checkNotNull(name).toLowerCase(Locale.ROOT);
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
