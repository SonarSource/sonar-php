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

import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.php.utils.collections.SetUtils;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.LiteralTree;

public class ArgumentMatcherValueContainment extends ArgumentMatcher {

  private final Set<String> values;


  <T extends ArgumentMatcherValueContainmentBuilder<T>> ArgumentMatcherValueContainment(ArgumentMatcherValueContainmentBuilder<T> builder) {
    super(builder);
    this.values = builder.values;
  }

  @Override
  public boolean matches(ExpressionTree argumentValue) {
    boolean matchesValues = false;
    Optional<String> value = nameOf(argumentValue);

    if (value.isPresent()) {
      String quoteLessLowercaseValue = CheckUtils.trimQuotes(value.get()).toLowerCase(Locale.ENGLISH);
      matchesValues = this.values.contains(quoteLessLowercaseValue);
    }
    return matchesValues;
  }

  public Optional<String> nameOf(Tree tree) {
    String name;
    if (tree instanceof LiteralTree) {
      name = ((LiteralTree) tree).value();
    } else {
      name = CheckUtils.nameOf(tree);
    }
    return Optional.ofNullable(name);
  }

  Set<String> getValues() {
    return values;
  }

  public static <T extends ArgumentMatcherValueContainmentBuilder<T>> ArgumentMatcherValueContainmentBuilder<T> builder() {
    return new ArgumentMatcherValueContainmentBuilder<>();
  }

  public static class ArgumentMatcherValueContainmentBuilder<T extends ArgumentMatcherValueContainmentBuilder<T>> extends ArgumentMatcherBuilder<T> {
    private Set<String> values;

    public T values(String value) {
      return values(SetUtils.immutableSetOf(value));
    }

    public T values(Set<String> values) {
      this.values = values.stream()
        .map(value -> value.toLowerCase(Locale.ENGLISH))
        .collect(Collectors.toSet());
      return (T) this;
    }

    @Override
    public ArgumentMatcherValueContainment build() {
      return new ArgumentMatcherValueContainment(this);
    }
  }
}
