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
package org.sonar.plugins.php.reports.phpunit.xml;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;

public final class TestCase {

  public enum Status {
    OK, SKIPPED, FAILURE, ERROR
  }

  private String className;

  private String name;

  private String error;

  private String failure;

  private String skipped;

  public TestCase(String className, String name, String error, String failure, String skipped) {
    this.className = className;
    this.name = name;
    this.error = error;
    this.failure = failure;
    this.skipped = skipped;
  }

  public TestCase(Status status) {
    if (status == Status.ERROR) {
      this.error = status.toString();
    }
    if (status == Status.FAILURE) {
      this.failure = status.toString();
    }
    if (status == Status.SKIPPED) {
      this.skipped = Status.SKIPPED.toString();
    }
  }

  public Status getStatus() {
    if (StringUtils.isNotBlank(error)) {
      return Status.ERROR;
    }
    if (StringUtils.isNotBlank(failure)) {
      return Status.FAILURE;
    }
    if (skipped != null) {
      return Status.SKIPPED;
    }
    return Status.OK;
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this)
      .append("className", className)
      .append("name", name)
      .append("status", getStatus())
      .toString();
  }

  String fullName() {
    if (className != null) {
      return className + "." + name;
    } else {
      return name;
    }
  }
}
