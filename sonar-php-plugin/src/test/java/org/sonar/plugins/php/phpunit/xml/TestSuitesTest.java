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
/**
 * @author gennadiyl
 */
package org.sonar.plugins.php.phpunit.xml;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.both;
import static org.junit.matchers.JUnitMatchers.hasItem;

/**
 * Test for TestSuites.
 *
 * @author gennadiyl
 */
public class TestSuitesTest {

  private TestSuites testSuites;
  private static final Logger LOG = LoggerFactory.getLogger(TestSuitesTest.class);

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    testSuites = new TestSuites();
  }

  /**
   * Test method for {@link org.sonar.plugins.php.phpunit.xml.TestSuites#setTestSuites(java.util.List)}.
   */
  @Test
  public void testSetTestSuites() {
    List<TestSuite> testSuitesList = new ArrayList<TestSuite>();

    testSuites.setTestSuites(testSuitesList);
    assertSame(testSuitesList, testSuites.getTestSuites());
  }

  /**
   * Test method for {@link org.sonar.plugins.php.phpunit.xml.TestSuites#addTestSuite(org.sonar.plugins.php.phpunit.xml.TestSuite)}.
   */
  @Test
  public void testAddTestSuite() {
    TestSuite suite = getTestSuite("name");

    testSuites.addTestSuite(suite);

    assertThat(testSuites.getTestSuites(), hasItem(suite));
  }

  /**
   * Test method for {@link org.sonar.plugins.php.phpunit.xml.TestSuites#addTestSuite(org.sonar.plugins.php.phpunit.xml.TestSuite)}.
   */
  @Test
  public void testAddMultipleTestSuite() {
    TestSuite suite1 = getTestSuite("name1");
    TestSuite suite2 = getTestSuite("name2");

    testSuites.addTestSuite(suite1);
    testSuites.addTestSuite(suite2);

    assertThat(testSuites.getTestSuites(), both(hasItem(suite1)).and(hasItem(suite2)));
  }

  /**
   * Empty constructors are required by xstream for TestSuite and TestSuite, in order to
   * be compatible with Java 7.
   */
  @Test
  public void test_compatible_java_7() {
    Constructor[] constructors1 = getConstructorForClass("org.sonar.plugins.php.phpunit.xml.TestSuite");
    assertThat(constructors1).isNotEmpty();
    assertThat(getNbParamPerConstructor(constructors1).first()).isEqualTo(0);

    Constructor[] constructors2 = getConstructorForClass("org.sonar.plugins.php.phpunit.xml.TestSuites");
    assertThat(constructors2).isNotEmpty();
    assertThat(getNbParamPerConstructor(constructors2).first()).isEqualTo(0);
  }

  private Constructor[] getConstructorForClass(String className) {
    Constructor[] constructors = new Constructor[0];
    try {
      Class testSuites = Class.forName(className);
      constructors = testSuites.getConstructors();
    } catch (ClassNotFoundException e) {
      LOG.error("If TestSuite and TestSuites class does not exist any more, this test might be useless.", e);
    }
    return constructors;
  }

  private TreeSet<Integer> getNbParamPerConstructor(Constructor[] constructors) {
    TreeSet<Integer> nbParamPerConstructors = new TreeSet<Integer>();
    for (Constructor c : constructors) {
      nbParamPerConstructors.add(c.getParameterTypes().length);
    }

    return nbParamPerConstructors;
  }

  private TestSuite getTestSuite(String name) {
    return new TestSuite(name, "file", "tests", "assertions", 0, null, null);
  }


}
