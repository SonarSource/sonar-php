/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2023 SonarSource SA
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
package org.sonar.php.checks.utils.argumentmatching;

import javax.annotation.Nullable;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;

public abstract class ArgumentMatcher {

  private final int position;
  @Nullable
  private final String name;

  <T extends ArgumentMatcherBuilder<T>> ArgumentMatcher(ArgumentMatcherBuilder<T> builder) {
    this.position = builder.position;
    this.name = builder.name;
  }

  abstract boolean matches(ExpressionTree argument);

  int getPosition() {
    return position;
  }

  @Nullable
  String getName() {
    return name;
  }

  abstract static class ArgumentMatcherBuilder<T extends ArgumentMatcherBuilder<T>> {

    private int position;

    private String name;

    public T position(int position) {
      this.position = position;
      return (T) this;
    }

    public T name(String name) {
      this.name = name;
      return (T) this;
    }

    public abstract ArgumentMatcher build();
  }
}
