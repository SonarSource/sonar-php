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

public class CpdDeserializationInput {
  private final byte[] cpdTokensBytes;
  private final byte[] stringTable;
  private final String pluginVersion;

  public CpdDeserializationInput(byte[] cpdTokensBytes, byte[] stringTable, String pluginVersion) {
    this.cpdTokensBytes = cpdTokensBytes;
    this.stringTable = stringTable;
    this.pluginVersion = pluginVersion;
  }

  public byte[] cpdTokensBytes() {
    return cpdTokensBytes;
  }

  public byte[] stringTable() {
    return stringTable;
  }

  public String pluginVersion() {
    return pluginVersion;
  }
}
