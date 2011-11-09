/*
 * Sonar PHP Plugin
 * Copyright (C) 2010 Codehaus Sonar Plugins
 * dev@sonar.codehaus.org
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
 * 
 */
package org.sonar.plugins.php.codesniffer;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.sonar.api.rules.RulePriority;

/**
 * @author akram
 * 
 */
public class PhpCodeSnifferPriorityMapperTest {

  /**
   * Test method for {@link org.sonar.plugins.php.checkstyle.PhpCodeSnifferPriorityMapper#from(java.lang.String)}.
   */
  @Test
  public void testFromWithRegularValues() {
    PhpCodeSnifferPriorityMapper mapper = new PhpCodeSnifferPriorityMapper();
    assertEquals(RulePriority.BLOCKER, mapper.from("10"));
    assertEquals(RulePriority.CRITICAL, mapper.from("9"));
    assertEquals(RulePriority.MAJOR, mapper.from("8"));
    assertEquals(RulePriority.MINOR, mapper.from("7"));
    assertEquals(RulePriority.INFO, mapper.from("6"));
    assertEquals(RulePriority.INFO, mapper.from("1"));
  }

  /**
   * Test method for {@link org.sonar.plugins.php.checkstyle.PhpCodeSnifferPriorityMapper#from(java.lang.String)}.
   */
  @Test
  public void testFromWithUnknownInput() {
    PhpCodeSnifferPriorityMapper mapper = new PhpCodeSnifferPriorityMapper();
    assertEquals(RulePriority.MAJOR, mapper.from("foo"));
  }

  /**
   * Test method for {@link org.sonar.plugins.php.checkstyle.PhpCodeSnifferPriorityMapper#from(java.lang.String)}.
   */
  @Test
  public void testFromWithNullValue() {
    PhpCodeSnifferPriorityMapper mapper = new PhpCodeSnifferPriorityMapper();
    assertEquals(RulePriority.MAJOR, mapper.from(null));
  }

  /**
   * Test method for {@link org.sonar.plugins.php.checkstyle.PhpCodeSnifferPriorityMapper#to(org.sonar.api.rules.RulePriority)}.
   */
  @Test
  public void testTo() {
    PhpCodeSnifferPriorityMapper mapper = new PhpCodeSnifferPriorityMapper();
    assertEquals("6", mapper.to(RulePriority.INFO));
    assertEquals("7", mapper.to(RulePriority.MINOR));
    assertEquals("8", mapper.to(RulePriority.MAJOR));
    assertEquals("9", mapper.to(RulePriority.CRITICAL));
    assertEquals("10", mapper.to(RulePriority.BLOCKER));
  }

  /**
   * Test method for {@link org.sonar.plugins.php.checkstyle.PhpCodeSnifferPriorityMapper#to(org.sonar.api.rules.RulePriority)}.
   */
  @Test(expected = IllegalArgumentException.class)
  public void testToIfUnexistingPriority() {
    PhpCodeSnifferPriorityMapper mapper = new PhpCodeSnifferPriorityMapper();
    assertEquals(null, mapper.to(null));
  }
}
