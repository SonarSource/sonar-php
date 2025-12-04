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
package org.sonar.php.checks;

import java.util.Arrays;
import java.util.List;
import org.sonar.check.Rule;
import org.sonar.php.checks.security.ClearTextProtocolsCheckPart;
import org.sonar.php.checks.wordpress.WordPressForceSslCheckPart;

@Rule(key = "S5332")
public class ClearTextProtocolsCheck extends CheckBundle {

  @Override
  protected List<CheckBundlePart> checks() {
    return Arrays.asList(
      new ClearTextProtocolsCheckPart(),
      new WordPressForceSslCheckPart());
  }
}
