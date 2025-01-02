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
package org.sonar.php.cache;

import java.util.List;
import org.sonar.php.metrics.CpdVisitor;

public class CpdSerializationInput {
  private final List<CpdVisitor.CpdToken> cpdTokens;
  private final String pluginVersion;

  public CpdSerializationInput(List<CpdVisitor.CpdToken> cpdTokens, String pluginVersion) {
    this.cpdTokens = cpdTokens;
    this.pluginVersion = pluginVersion;
  }

  public List<CpdVisitor.CpdToken> cpdTokens() {
    return cpdTokens;
  }

  public String pluginVersion() {
    return pluginVersion;
  }
}
