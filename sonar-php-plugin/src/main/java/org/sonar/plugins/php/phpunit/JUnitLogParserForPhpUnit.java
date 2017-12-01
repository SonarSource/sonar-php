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
package org.sonar.plugins.php.phpunit;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import org.sonar.plugins.php.phpunit.xml.TestCase;
import org.sonar.plugins.php.phpunit.xml.TestSuite;
import org.sonar.plugins.php.phpunit.xml.TestSuites;
import org.sonar.plugins.php.phpunit.xml.XmlUtils;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

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
      return processRoot(report, XmlUtils.documentBuilder());
    } catch (IOException | SAXException | ParserConfigurationException e) {
      throw new IllegalStateException("Can't read PhpUnit report : " + report.getAbsolutePath(), e);
    }
  }

  private static TestSuites processRoot(File file, DocumentBuilder documentBuilder) throws IOException, SAXException {
    Element root = documentBuilder.parse(file).getDocumentElement();
    if (root == null || !"testsuites".equals(root.getNodeName())) {
      throw new IOException("Report should start with <testsuites>");
    }
    List<TestSuite> testSuites = new ArrayList<>();
    XmlUtils.elements(root, "testsuite")
      .forEach(testSuiteNode -> testSuites.add(processTestSuite(testSuiteNode)));
    return new TestSuites(testSuites);
  }

  private static TestSuite processTestSuite(Element testSuiteNode) {
    String name = testSuiteNode.getAttribute("name");
    String file = testSuiteNode.getAttribute("file");
    double time = Double.parseDouble(testSuiteNode.getAttribute("time"));

    TestSuite testSuite = new TestSuite(name, file, time);
    XmlUtils.elements(testSuiteNode, "testsuite")
      .forEach(child -> testSuite.addNested(processTestSuite(child)));
    XmlUtils.elements(testSuiteNode, "testcase")
      .forEach(child -> testSuite.addTestCase(processTestCase(child)));
    return testSuite;
  }

  private static TestCase processTestCase(Element testCase) {
    String className = testCase.getAttribute("class");
    String name = testCase.getAttribute("name");
    return new TestCase(
      className,
      name,
      XmlUtils.elementText(testCase, "error"),
      XmlUtils.elementText(testCase, "failure"),
      XmlUtils.elementText(testCase, "skipped"));
  }
  
}
