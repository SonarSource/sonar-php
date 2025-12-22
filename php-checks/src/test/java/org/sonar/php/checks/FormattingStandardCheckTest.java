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
package org.sonar.php.checks;

import java.lang.reflect.Field;
import java.util.Arrays;

public abstract class FormattingStandardCheckTest {

  protected FormattingStandardCheck check = new FormattingStandardCheck();
  protected static final String TEST_DIR = "formattingCheck/";

  protected void activeOnly(String... fieldNames) throws IllegalAccessException {
    for (Field f : check.getClass().getFields()) {
      if (!f.getType().equals(boolean.class)) {
        continue;
      }
      f.setBoolean(check, Arrays.asList(fieldNames).contains(f.getName()));
    }
  }

  protected void deactivateAll() throws IllegalAccessException {
    activeOnly();
  }
}
