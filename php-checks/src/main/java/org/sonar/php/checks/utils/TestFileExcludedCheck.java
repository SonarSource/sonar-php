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
package org.sonar.php.checks.utils;

import org.sonar.plugins.php.api.visitors.PHPCheck;

/**
 * Marker interface for checks that should not raise issues on files that look like test files.
 * When a file resides in a test-like directory (e.g. {@code test/}, {@code tests/},
 * {@code __tests__/}) and the project has not explicitly configured {@code sonar.tests},
 * issues from checks implementing this interface are suppressed.
 *
 * <p>To exclude an additional check from test-like files, add {@code implements TestFileExcludedCheck}
 * to its class declaration.
 */
public interface TestFileExcludedCheck extends PHPCheck {
}
