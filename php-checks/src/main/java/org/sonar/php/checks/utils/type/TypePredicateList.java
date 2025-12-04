/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.php.checks.utils.type;

import java.util.function.Predicate;

public class TypePredicateList implements Predicate<TreeValues> {

  private final Predicate<TreeValues>[] predicateList;

  public TypePredicateList(Predicate<TreeValues>... predicateList) {
    this.predicateList = predicateList;
  }

  @Override
  public boolean test(TreeValues possibleValues) {
    for (Predicate<TreeValues> predicate : this.predicateList) {
      if (predicate.test(possibleValues)) {
        return true;
      }
    }
    return false;
  }

}
