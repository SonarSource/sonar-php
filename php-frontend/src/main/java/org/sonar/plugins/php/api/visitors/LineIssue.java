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
package org.sonar.plugins.php.api.visitors;

import javax.annotation.Nullable;

public class LineIssue implements PhpIssue {

  private final PHPCheck check;
  private Double cost = null;
  private final String message;
  private final int line;

  public LineIssue(PHPCheck check, int line, String message) {
    this.message = message;
    this.line = line;
    this.check = check;
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

  public String message() {
    return message;
  }

  public int line() {
    return line;
  }

  @Override
  public LineIssue cost(double cost) {
    this.cost = cost;
    return this;
  }
}
