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
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.sonar.api.internal.google.common.annotations.VisibleForTesting;
import org.sonar.plugins.php.phpunit.PhpUnitTestFileReport;

@XStreamAlias("testsuites")
public final class TestSuites {

  @VisibleForTesting
  @XStreamImplicit(itemFieldName = "testsuite")
  List<TestSuite> suites = new ArrayList<>();

  public TestSuites() {
    // Zero parameters constructor is required by xstream
  }

  @VisibleForTesting
  TestSuites(TestSuite... suites) {
    this.suites = Arrays.asList(suites);
  }

  public List<PhpUnitTestFileReport> arrangeSuitesIntoTestFileReports() {
    List<PhpUnitTestFileReport> result = new ArrayList<>();
    for (TestSuite testSuite : suites) {
      result.addAll(testSuite.generateReports());
    }
    return result;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    TestSuites that = (TestSuites) o;

    return new EqualsBuilder()
      .append(suites, that.suites)
      .isEquals();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(17, 37)
      .append(suites)
      .toHashCode();
  }
}
