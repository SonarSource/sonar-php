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
package org.sonar.php.utils.collections;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;
import javax.annotation.Nullable;

public class IteratorUtils {

  private IteratorUtils() {

  }

  public static <T> Iterator<T> iteratorOf(T element) {
    return Collections.singletonList(element).iterator();
  }

  @SafeVarargs
  public static <T> Iterator<T> iteratorOf(T... element) {
    return Arrays.asList(element).iterator();
  }

  public static <T> Iterator<T> nullableIterator(@Nullable T element) {
    if (element == null) {
      return Collections.emptyIterator();
    }
    return iteratorOf(element);
  }

  @SafeVarargs
  public static <T> Iterator<T> concat(Iterator<? extends T>... iterators) {
    return new IteratorIterator<>(iterators);
  }

  private static class IteratorIterator<T> implements Iterator<T> {
    private final Iterator<? extends T>[] iterables;
    private Iterator<? extends T> current;
    private int currentIndex;

    @SafeVarargs
    public IteratorIterator(Iterator<? extends T>... iterables) {
      this.iterables = iterables;
    }

    @Override
    public boolean hasNext() {
      checkNext();
      return current != null && current.hasNext();
    }

    @Override
    public T next() {
      checkNext();
      if (current == null || !current.hasNext()) {
        throw new NoSuchElementException();
      }
      return current.next();
    }

    private void checkNext() {
      while ((current == null || !current.hasNext()) && currentIndex < iterables.length) {
        current = iterables[currentIndex];
        currentIndex++;
      }
    }
  }
}
