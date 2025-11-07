/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource Sàrl
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
package org.sonar.plugins.php.reports.phpunit.xml;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.sonar.plugins.php.reports.phpunit.TestFileReport;

public final class TestSuites {

  List<TestSuite> suites = new ArrayList<>();

  public TestSuites(List<TestSuite> suites) {
    this.suites = suites;
  }

  public List<TestFileReport> arrangeSuitesIntoTestFileReports() {
    List<TestFileReport> result = new ArrayList<>();
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
