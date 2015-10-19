/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010 SonarSource and Akram Ben Aissi
 * sonarqube@googlegroups.com
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
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.php.checks;

import org.apache.commons.lang.ArrayUtils;

import java.lang.reflect.Field;

abstract public class FormattingStandardCheckTest {

  protected FormattingStandardCheck check = new FormattingStandardCheck();
  protected static final String TEST_DIR = "formattingCheck/";

  protected void activeOnly(String... fieldNames) throws IllegalAccessException {
    for (Field f : check.getClass().getFields()) {
      if (!f.getType().equals(boolean.class)) {
        continue;
      }
      if (ArrayUtils.contains(fieldNames, f.getName())) {
        f.setBoolean(check, true);
      } else {
        f.setBoolean(check, false);
      }
    }
  }

  protected void deactivateAll() throws IllegalAccessException {
    activeOnly();
  }
}
