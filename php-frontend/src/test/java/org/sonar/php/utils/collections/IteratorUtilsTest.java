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

import java.util.Iterator;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class IteratorUtilsTest {

  @Test
  void testReturnSingletonIterator() {
    Iterator<String> singletonIterator = IteratorUtils.iteratorOf("element");

    assertThat(singletonIterator.hasNext()).isTrue();
    assertThat(singletonIterator.next()).isEqualTo("element");
    assertThat(singletonIterator.hasNext()).isFalse();

    assertThatExceptionOfType(NoSuchElementException.class).isThrownBy(singletonIterator::next);
  }

  @Test
  void testReturnIteratorOfMultipleElements() {
    Iterator<String> singletonIterator = IteratorUtils.iteratorOf("element1", "element2");

    assertThat(singletonIterator.hasNext()).isTrue();
    assertThat(singletonIterator.next()).isEqualTo("element1");
    assertThat(singletonIterator.hasNext()).isTrue();
    assertThat(singletonIterator.next()).isEqualTo("element2");
    assertThat(singletonIterator.hasNext()).isFalse();
    assertThatExceptionOfType(NoSuchElementException.class).isThrownBy(singletonIterator::next);
  }

  @Test
  void testReturnConcatenatedIterators() {
    Iterator<String> firstIterator = IteratorUtils.iteratorOf("firstElement");
    Iterator<String> secondIterator = IteratorUtils.iteratorOf("secondElement", "thirdElement");
    Iterator<String> iterator = IteratorUtils.concat(firstIterator, secondIterator);

    assertThat(iterator.hasNext()).isTrue();
    assertThat(iterator.next()).isEqualTo("firstElement");
    assertThat(iterator.next()).isEqualTo("secondElement");
    assertThat(iterator.next()).isEqualTo("thirdElement");
    assertThat(iterator.hasNext()).isFalse();
    assertThatExceptionOfType(NoSuchElementException.class).isThrownBy(iterator::next);
  }

  @Test
  void testIteratorConcatWithNull() {
    Iterator<String> iterator = IteratorUtils.concat((Iterator<String>) null);

    assertThat(iterator.hasNext()).isFalse();
    assertThatExceptionOfType(NoSuchElementException.class).isThrownBy(iterator::next);
  }

  @Test
  void testNullableIteratorWithExistingElement() {
    Iterator<String> iterator = IteratorUtils.nullableIterator("element");

    assertThat(iterator.hasNext()).isTrue();
    assertThat(iterator.next()).isEqualTo("element");
  }

  @Test
  void testNullableIteratorWithNull() {
    Iterator<String> iterator = IteratorUtils.nullableIterator(null);

    assertThat(iterator.hasNext()).isFalse();
    assertThatExceptionOfType(NoSuchElementException.class).isThrownBy(iterator::next);
  }
}
