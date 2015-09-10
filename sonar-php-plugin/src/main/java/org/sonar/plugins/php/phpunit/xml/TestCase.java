/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010 SonarSource and Akram Ben Aissi
 * sonarqube@googlegroups.com
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
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
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

  /**
   * The Constant STATUS_ERROR.
   */
  public static final String STATUS_ERROR = "error";

  /**
   * The Constant STATUS_FAILURE.
   */
  public static final String STATUS_FAILURE = "failure";

  /**
   * The Constant STATUS_OK.
   */
  public static final String STATUS_OK = "ok";

  /**
   * The Constant STATUS_SKIPPED.
   */
  public static final String STATUS_SKIPPED = "skipped";

  /**
   * The assertions.
   */
  @XStreamAsAttribute
  private int assertions;

  /**
   * The class name.
   */
  @XStreamAsAttribute
  @XStreamAlias("class")
  private String className;

  /**
   * The error message.
   */
  @XStreamAsAttribute
  private String errorMessage;

  /**
   * The file.
   */
  @XStreamAsAttribute
  private String file;

  /**
   * The line.
   */
  @XStreamAsAttribute
  private int line;

  /**
   * The name.
   */
  @XStreamAsAttribute
  private String name;

  /**
   * The status.
   */
  @XStreamOmitField
  private String status;

  /**
   * The time.
   */
  @XStreamAsAttribute
  private double time;

  /**
   * The error.
   */
  @XStreamAlias("error")
  private String error;

  /**
   * The failure.
   */
  @XStreamAlias("failure")
  private String failure;

  /**
   * Gets the assertions.
   *
   * @return the assertions
   */
  public int getAssertions() {
    return assertions;
  }

  /**
   * Gets the class name.
   *
   * @return the class name
   */
  public String getClassName() {
    return className;
  }

  /**
   * Gets the error message.
   *
   * @return the error message
   */
  public String getErrorMessage() {
    return errorMessage;
  }

  /**
   * Gets the file.
   *
   * @return the file
   */
  public String getFile() {
    return file;
  }

  /**
   * Gets the line.
   *
   * @return the line
   */
  public int getLine() {
    return line;
  }

  /**
   * Gets the name.
   *
   * @return the name
   */
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

  /**
   * Gets the status. Computes the status depending on the presence of field failure and error
   *
   * @return the status
   */
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

  /**
   * Gets the time.
   *
   * @return the time
   */
  public double getTime() {
    return time;
  }

  /**
   * Sets the assertions.
   *
   * @param assertions the new assertions
   */
  public void setAssertions(final int assertions) {
    this.assertions = assertions;
  }

  /**
   * Sets the file.
   *
   * @param file the new file
   */
  public void setFile(final String file) {
    this.file = file;
  }

  /**
   * Sets the line.
   *
   * @param line the new line
   */
  public void setLine(final int line) {
    this.line = line;
  }

  /**
   * Sets the name.
   *
   * @param name the new name
   */
  public void setName(final String name) {
    this.name = name;
  }

  /*
   * (non-Javadoc)
   *
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("TestCase [assertions=").append(assertions).append(", fileName=").append(className).append(", errorMessage=")
      .append(errorMessage).append(", file=").append(file).append(", line=").append(line).append(", name=").append(name)
      .append(", status=").append(status).append(", time=").append(time).append(", error=").append(error).append(", failure=")
      .append(failure).append("]");
    return builder.toString();
  }

}
