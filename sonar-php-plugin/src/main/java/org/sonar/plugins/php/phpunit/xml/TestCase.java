/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2018 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.plugins.php.phpunit.xml;

import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;

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

  @VisibleForTesting
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
