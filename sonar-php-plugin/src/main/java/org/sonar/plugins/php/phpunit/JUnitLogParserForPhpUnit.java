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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import org.codehaus.staxmate.SMInputFactory;
import org.codehaus.staxmate.in.SMHierarchicCursor;
import org.codehaus.staxmate.in.SMInputCursor;
import org.sonar.plugins.php.phpunit.xml.TestCase;
import org.sonar.plugins.php.phpunit.xml.TestSuite;
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
      return processRoot(report, inputFactory());
    } catch (XMLStreamException e) {
      throw new IllegalStateException("Can't read PhpUnit report : " + report.getAbsolutePath(), e);
    }
  }

  public static SMInputFactory inputFactory() {
    XMLInputFactory xmlFactory = XMLInputFactory.newInstance();
    xmlFactory.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, Boolean.TRUE);
    xmlFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
    xmlFactory.setProperty(XMLInputFactory.IS_COALESCING, Boolean.TRUE);
    xmlFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, Boolean.FALSE);
    // just so it won't try to load DTD in if there's DOCTYPE
    xmlFactory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);
    xmlFactory.setProperty(XMLInputFactory.IS_VALIDATING, Boolean.FALSE);
    return new SMInputFactory(xmlFactory);
  }

  private static TestSuites processRoot(File file, SMInputFactory inputFactory) throws XMLStreamException {
    SMHierarchicCursor rootCursor = inputFactory.rootElementCursor(file);
    rootCursor.advance();
    if (!"testsuites".equals(rootCursor.getLocalName())) {
      throw new XMLStreamException("Report should start with <testsuites>");
    }
    SMInputCursor childCursor = rootCursor.childElementCursor("testsuite");
    List<TestSuite> testSuites = new ArrayList<>();
    while (childCursor.getNext() != null) {
      testSuites.add(processTestSuite(childCursor));
    }
    return new TestSuites(testSuites);
  }

  private static TestSuite processTestSuite(SMInputCursor cursor) throws XMLStreamException {
    String name = cursor.getAttrValue("name");
    String file = cursor.getAttrValue("file");
    double time = 0;
    String timeAttributeValue = cursor.getAttrValue("time");
    if (timeAttributeValue != null) {
      try {
        time = Double.parseDouble(timeAttributeValue);
      } catch (NumberFormatException ex) {
        // ignore
      }
    }

    List<TestCase> testCases = new ArrayList<>();
    List<TestSuite> nestedSuites = new ArrayList<>();
    SMInputCursor childCursor = cursor.childCursor();
    while (childCursor.getNext() != null) {
      String childName = childCursor.getLocalName();
      if ("testsuite".equals(childName)) {
        nestedSuites.add(processTestSuite(childCursor));
      } else if ("testcase".equals(childName)) {
        testCases.add(processTestCase(childCursor));
      }
    }
    TestSuite testSuite = new TestSuite(name, file, time, testCases);
    nestedSuites.forEach(testSuite::addNested);
    return testSuite;
  }

  private static TestCase processTestCase(SMInputCursor cursor) throws XMLStreamException {
    String className = cursor.getAttrValue("class");
    String name = cursor.getAttrValue("name");

    SMInputCursor childCursor = cursor.childCursor();
    Map<String,String> childValues = new HashMap<>();
    while (childCursor.getNext() != null) {
      if(childCursor.getLocalName() != null) {
        childValues.put(childCursor.getLocalName(), childCursor.collectDescendantText(false));
      }
    }

    return new TestCase(
      className,
      name,
      childValues.get("error"),
      childValues.get("failure"),
      childValues.get("skipped"));
  }
  
}
