/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2026 SonarSource Sàrl
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
package org.sonar.php.cache;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

public class StringTable {
  private final Map<String, Integer> table;
  private final List<String> byIndex;

  public StringTable() {
    this.table = new HashMap<>();
    this.byIndex = new ArrayList<>();
  }

  public StringTable(List<String> byIndex) {
    this.table = new HashMap<>();
    this.byIndex = byIndex;
    for (int i = 0; i < byIndex.size(); i++) {
      table.put(byIndex.get(i), i);
    }
  }

  public int getIndex(@Nullable String string) {
    return table.computeIfAbsent(string, s -> {
      byIndex.add(s);
      return byIndex.size() - 1;
    });
  }

  public String getString(int index) {
    return byIndex.get(index);
  }

  public List<String> getStringList() {
    return Collections.unmodifiableList(byIndex);
  }
}
