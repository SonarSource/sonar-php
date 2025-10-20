/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.plugins.php.reports.phpunit;

import com.ctc.wstx.exc.WstxIOException;
import com.ctc.wstx.stax.WstxInputFactory;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import org.codehaus.staxmate.SMInputFactory;
import org.codehaus.staxmate.in.SMHierarchicCursor;
import org.codehaus.staxmate.in.SMInputCursor;
import org.sonar.plugins.php.reports.phpunit.xml.TestCase;
import org.sonar.plugins.php.reports.phpunit.xml.TestSuite;
import org.sonar.plugins.php.reports.phpunit.xml.TestSuites;

/**
 * PHPUnit can generate test result logs that comply with JUnit's test results xml format.
 * This parser understands that xml format and produces a TestSuites object.
 *
 * This parser was built to support the subset of sample xml files that are used in the
 * tests and not a particular xsd since an authoritative one is not available.
 * It will not raise any parsing error except basic xml syntax issues.
 */
public class JUnitLogParserForPhpUnit {

  public TestSuites parse(File report) throws ParseException, IOException {
    try {
      return processRoot(report, inputFactory());
    } catch (WstxIOException e) {
      throw new IOException(e.getMessage(), e.getCause());
    } catch (XMLStreamException e) {
      throw new ParseException(e);
    }
  }

  public static SMInputFactory inputFactory() {
    return new SMInputFactory(createXMLInputFactory());
  }

  public static XMLInputFactory createXMLInputFactory() {
    // Copied from sonar-analzer-commons: xml-parsing/src/main/java/org/sonarsource/analyzer/commons/xml/SafeStaxParserFactory.java
    XMLInputFactory factory = new WstxInputFactory();
    factory.setProperty("javax.xml.stream.supportDTD", false);
    factory.setProperty("javax.xml.stream.isReplacingEntityReferences", false);
    factory.setProperty("javax.xml.stream.isValidating", false);
    factory.setProperty("javax.xml.stream.isSupportingExternalEntities", false);
    return factory;
  }

  private static TestSuites processRoot(File file, SMInputFactory inputFactory) throws XMLStreamException {
    SMHierarchicCursor rootCursor = null;
    try {
      rootCursor = inputFactory.rootElementCursor(file);
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
    } finally {
      if (rootCursor != null) {
        rootCursor.getStreamReader().closeCompletely();
      }
    }
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

    SMInputCursor childCursor;
    if (file != null) {
      childCursor = cursor.descendantElementCursor("testcase");
    } else {
      childCursor = cursor.childCursor();
    }

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
    Map<String, String> childValues = new HashMap<>();
    while (childCursor.getNext() != null) {
      if (childCursor.getLocalName() != null) {
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
