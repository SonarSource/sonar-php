/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2017 SonarSource SA
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

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamOmitField;
import org.apache.commons.lang.StringUtils;

/**
 * The Class TestCase.
 */
@XStreamAlias("testcase")
public final class TestCase {

  public static final String STATUS_ERROR = "error";
  public static final String STATUS_FAILURE = "failure";
  public static final String STATUS_OK = "ok";
  public static final String STATUS_SKIPPED = "skipped";

  @XStreamAsAttribute
  private int assertions;

  @XStreamAsAttribute
  @XStreamAlias("class")
  private String className;

  @XStreamAsAttribute
  private String errorMessage;

  @XStreamAsAttribute
  private String file;

  @XStreamAsAttribute
  private int line;

  @XStreamAsAttribute
  private String name;

  @XStreamOmitField
  private String status;

  @XStreamAsAttribute
  private double time;

  @XStreamAlias("error")
  private String error;

  @XStreamAlias("failure")
  private String failure;

  public int getAssertions() {
    return assertions;
  }

  public String getClassName() {
    return className;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public String getFile() {
    return file;
  }

  public int getLine() {
    return line;
  }

  public String getName() {
    return name;
  }

  public String getStackTrace() {
    if (STATUS_ERROR.equals(getStatus())) {
      return error;
    }
    if (STATUS_FAILURE.equals(getStatus())) {
      return failure;
    }
    return null;
  }

  public String getStatus() {
    if (StringUtils.isBlank(status)) {
      status = STATUS_OK;
    }
    if (StringUtils.isNotBlank(error)) {
      status = STATUS_ERROR;
    }
    if (StringUtils.isNotBlank(failure)) {
      status = STATUS_FAILURE;
    }
    return status;
  }

  public void setAssertions(final int assertions) {
    this.assertions = assertions;
  }

  public void setFile(final String file) {
    this.file = file;
  }

  public void setLine(final int line) {
    this.line = line;
  }

  public void setName(final String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("TestCase [assertions=").append(assertions).append(", fileName=").append(className).append(", errorMessage=")
      .append(errorMessage).append(", file=").append(file).append(", line=").append(line).append(", name=").append(name)
      .append(", status=").append(status).append(", time=").append(time).append(", error=").append(error).append(", failure=")
      .append(failure).append("]");
    return builder.toString();
  }

  String fullName() {
    if(getClassName() != null) {
      return getClassName() + "." + getName();
    } else {
      return getName();
    }
  }
}
