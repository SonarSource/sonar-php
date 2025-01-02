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
package org.sonar.plugins.php.api.visitors;

import javax.annotation.Nullable;

public class FileIssue implements PhpIssue {

  private final PHPCheck check;
  private Double cost = null;
  private final String message;

  public FileIssue(PHPCheck check, String message) {
    this.check = check;
    this.message = message;
  }

  @Override
  public PHPCheck check() {
    return check;
  }

  @Nullable
  @Override
  public Double cost() {
    return cost;
  }

  @Override
  public FileIssue cost(double cost) {
    this.cost = cost;
    return this;
  }

  public String message() {
    return message;
  }
}
