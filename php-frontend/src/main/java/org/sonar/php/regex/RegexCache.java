/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2024 SonarSource SA
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
package org.sonar.php.regex;

import java.util.HashMap;
import java.util.Map;
import org.sonar.plugins.php.api.tree.expression.LiteralTree;
import org.sonarsource.analyzer.commons.regex.RegexParseResult;
import org.sonarsource.analyzer.commons.regex.RegexParser;
import org.sonarsource.analyzer.commons.regex.ast.FlagSet;

public final class RegexCache {
  private final Map<LiteralTree, RegexParseResult> cache = new HashMap<>();

  public RegexParseResult getRegexForLiterals(FlagSet initialFlags, LiteralTree stringLiteral) {
    return cache.computeIfAbsent(stringLiteral,
      k -> new RegexParser(new PhpAnalyzerRegexSource(k), initialFlags).parse());
  }
}
