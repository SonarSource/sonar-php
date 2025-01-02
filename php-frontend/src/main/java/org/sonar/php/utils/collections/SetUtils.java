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
package org.sonar.php.utils.collections;

import java.util.HashSet;
import java.util.Set;

public class SetUtils {

  private SetUtils() {
  }

  @SafeVarargs
  public static <T> Set<T> concat(Set<? extends T>... sets) {
    Set<T> concatenatedSet = new HashSet<>();
    for (Set<? extends T> set : sets) {
      concatenatedSet.addAll(set);
    }
    return concatenatedSet;
  }

  public static <T> Set<T> difference(Set<T> set1, Set<T> set2) {
    Set<T> newSet1 = new HashSet<>(set1);
    newSet1.removeAll(set2);
    return newSet1;
  }
}
