/*
 * SonarQube JavaScript Plugin
 * Copyright (C) 2011-2017 SonarSource SA
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

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.mapper.MapperWrapper;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.sonar.plugins.php.phpunit.xml.TestCase;
import org.sonar.plugins.php.phpunit.xml.TestSuite;
import org.sonar.plugins.php.phpunit.xml.TestSuites;

/**
 * PHPUnit can generate test result logs that comply with JUnit's test results xml format.
 * This parser understands that xml format and produces a TestSuites object.
 *
 * A note on the JUnit test results xml format : it's extremely poorly documented; there's
 * in fact no authoritative xsd provided by the JUnit project itself and many xsd provided
 * by third parties are wrong or partial and don't cover all possible xml outputs generated
 * by JUnit or other libraries (like PHPUnit). Few of them support nested testsuite elements
 * at all, which is an extremely frequent output of PHPUnit.
 * The most complete one seems the one allegedly proposed by the Apache Software Foundation
 * and used by Apache Ant, but somehow it's not provided in an ASF repository, continuing
 * the tradition of fuzziness around this format : http://windyroad.com.au/dl/Open%20Source/JUnit.xsd
 * Even this format is not sufficient to represent the PHPUnit-generated report, namely it
 * lacks the file attribute, which plays a key role in storing the metrics.
 *
 * This parser was built to support the subset of sample xml files that are used in the
 * tests and not a particular xsd. It will not raise any parsing error except basic xml syntax issues.
 */
public class JUnitLogParserForPhpUnit {

  public TestSuites parse(File report) {
    try (InputStream inputStream = new FileInputStream(report)) {
      final Object parsedObject = xstream().fromXML(inputStream);
      TestSuites testSuites = (TestSuites) parsedObject;
      return testSuites;
    } catch (IOException e) {
      throw new IllegalStateException("Can't read PhpUnit report : " + report.getAbsolutePath(), e);
    }
  }

  private XStream xstream() {
    XStream xstream = new XStream() {
      // Trick to ignore unknown elements
      @Override
      protected MapperWrapper wrapMapper(MapperWrapper next) {
        return new MapperWrapper(next) {
          @Override
          public boolean shouldSerializeMember(Class definedIn, String fieldName) {
            return definedIn != Object.class && super.shouldSerializeMember(definedIn, fieldName);
          }
        };
      }
    };
    xstream.setClassLoader(this.getClass().getClassLoader());
    xstream.aliasSystemAttribute("fileName", "class");
    xstream.processAnnotations(TestSuites.class);
    xstream.processAnnotations(TestSuite.class);
    xstream.processAnnotations(TestCase.class);
    return xstream;
  }
}
