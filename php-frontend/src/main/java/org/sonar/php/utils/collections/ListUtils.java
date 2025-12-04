/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource Sàrl
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;

public class ListUtils {

  private ListUtils() {
  }

  public static <T> T getLast(List<T> list) {
    return list.get(list.size() - 1);
  }

  @CheckForNull
  public static <T> T getLast(List<T> list, @Nullable T defaultValue) {
    return list.isEmpty() ? defaultValue : list.get(list.size() - 1);
  }

  public static <T> T getOnlyElement(List<T> list) {
    if (list.size() == 1) {
      return list.get(0);
    } else {
      throw new IllegalArgumentException(String.format("Expected list of size 1, but was list of size %d.", list.size()));
    }
  }

  @SafeVarargs
  public static <T> List<T> concat(Collection<? extends T>... collections) {
    List<T> concatenatedList = new ArrayList<>();
    for (Collection<? extends T> collection : collections) {
      concatenatedList.addAll(collection);
    }
    return concatenatedList;
  }

  @SafeVarargs
  public static <T> List<T> merge(Collection<? extends T>... collections) {
    return concat(collections).stream().distinct().toList();
  }

  public static <T> List<T> reverse(List<T> list) {
    List<T> reversed = new ArrayList<>(list);
    Collections.reverse(reversed);
    return reversed;
  }
}
