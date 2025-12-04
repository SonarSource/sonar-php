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
package com.sonar.plugins.security.api;

import java.util.HashSet;
import java.util.Set;

public class PhpRules {

  private static final String REPO_KEY = "phpsecurity";

  private static final Set<String> RULE_KEYS = new HashSet<>();

  private PhpRules() {
  }

  public static Set<String> getRuleKeys() {
    return RULE_KEYS;
  }

  public static String getRepositoryKey() {
    return REPO_KEY;
  }
}
