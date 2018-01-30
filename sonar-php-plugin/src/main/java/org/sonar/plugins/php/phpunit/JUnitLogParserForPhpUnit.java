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
package org.sonar.plugins.php.phpunit;

import java.io.File;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import org.sonar.plugins.php.phpunit.xml.TestSuites;

/**
 * PHPUnit can generate test result logs that comply with JUnit's test results xml format.
 * This parser understands that xml format and produces a TestSuites object.
 *
 * This parser was built to support the subset of sample xml files that are used in the
 * tests and not a particular xsd since an authoritative one is not available.
 * It will not raise any parsing error except basic xml syntax issues.
 */
public class JUnitLogParserForPhpUnit {

  public TestSuites parse(File report) {
    try {
      return (TestSuites) JAXBContext.newInstance(TestSuites.class)
        .createUnmarshaller().unmarshal(report);
    } catch (JAXBException e) {
      throw new IllegalStateException("Can't read PhpUnit report : " + report.getAbsolutePath(), e);
    }
  }

}
