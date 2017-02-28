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
import org.apache.commons.lang.builder.ToStringBuilder;
import org.sonar.api.internal.google.common.annotations.VisibleForTesting;
import org.sonar.plugins.php.api.Php;

@XStreamAlias("testcase")
public final class TestCase {

  public static final String STATUS_ERROR = "error";
  public static final String STATUS_FAILURE = "failure";
  public static final String STATUS_OK = "ok";
  public static final String STATUS_SKIPPED = "skipped";

  @XStreamAsAttribute
  @XStreamAlias("class")
  private String className;

  @XStreamAsAttribute
  private String file;

  @XStreamAsAttribute
  private String name;

  @XStreamAlias("error")
  private String error;

  @XStreamAlias("failure")
  private String failure;

  @XStreamAlias("skipped")
  private String skipped;

  @XStreamOmitField
  private String status;

  public TestCase() {
    // Zero parameters constructor is required by xstream
  }

  @VisibleForTesting
  public TestCase(String status) {
    if(STATUS_OK.equals(status)) {
      this.status = "";
    }
    if(STATUS_ERROR.equals(status)) {
      this.error = STATUS_ERROR;
    }
    if(STATUS_FAILURE.equals(status)) {
      this.failure = STATUS_FAILURE;
    }
    if(STATUS_SKIPPED.equals(status)) {
      this.skipped = STATUS_SKIPPED;
    }
    this.status = status;
  }

  public String getStatus() {
    if (StringUtils.isNotBlank(error)) {
      return STATUS_ERROR;
    }
    if (StringUtils.isNotBlank(failure)) {
      return STATUS_FAILURE;
    }
    if (skipped != null) {
      return STATUS_SKIPPED;
    }
    if (StringUtils.isBlank(status)) {
      return STATUS_OK;
    }
    return status;
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this)
      .append("className", className)
      .append("name", name)
      .append("status", status)
      .append("error", error)
      .append("failure", failure)
      .toString();
  }

  String fullName() {
    if(className != null) {
      return className + "." + name;
    } else {
      return name;
    }
  }
}
