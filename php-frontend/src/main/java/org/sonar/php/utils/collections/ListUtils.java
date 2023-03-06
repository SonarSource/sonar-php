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
package org.sonar.php.utils.collections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ListUtils {

  private ListUtils() {
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
    return concat(collections).stream().distinct().collect(Collectors.toList());
  }

  public static <T> List<T> reverse(List<T> list) {
    List<T> reversed = new ArrayList<>(list);
    Collections.reverse(reversed);
    return reversed;
  }
}
