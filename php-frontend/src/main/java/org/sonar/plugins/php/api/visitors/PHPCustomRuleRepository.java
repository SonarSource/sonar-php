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
package org.sonar.plugins.php.api.visitors;

import java.util.List;
import org.sonar.api.ExtensionPoint;
import org.sonar.api.batch.ScannerSide;

/**
 * Extension point to create a custom rule repository for PHP.
 */
@ScannerSide
@ExtensionPoint
public interface PHPCustomRuleRepository {

  /**
   * Key of the custom rule repository.
   */
  String repositoryKey();

  /**
   * List of the custom rules classes.
   */
  List<Class<?>> checkClasses();

}
