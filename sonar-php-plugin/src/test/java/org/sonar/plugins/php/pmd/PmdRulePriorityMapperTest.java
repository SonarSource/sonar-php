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
package org.sonar.plugins.php.pmd;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.sonar.api.rules.RulePriority;

/**
 * @author akram
 * 
 */
public class PmdRulePriorityMapperTest {

  /**
   * Test method for {@link org.sonar.plugins.php.checkstyle.CheckstyleRulePriorityMapper#from(java.lang.String)}.
   */
  @Test
  public void testFrom() {
    PmdRulePriorityMapper mapper = new PmdRulePriorityMapper();
    assertEquals(RulePriority.BLOCKER, mapper.from("1"));
    assertEquals(RulePriority.CRITICAL, mapper.from("2"));
    assertEquals(RulePriority.MAJOR, mapper.from("3"));
    assertEquals(RulePriority.MINOR, mapper.from("4"));
    assertEquals(RulePriority.INFO, mapper.from("5"));
    assertEquals(RulePriority.MAJOR, mapper.from(null));
    assertEquals(RulePriority.MAJOR, mapper.from(""));
  }

  /**
   * Test method for {@link org.sonar.plugins.php.checkstyle.CheckstyleRulePriorityMapper#to(org.sonar.api.rules.RulePriority)}.
   */
  @Test(expected = IllegalArgumentException.class)
  public void testTo() {
    PmdRulePriorityMapper mapper = new PmdRulePriorityMapper();
    assertEquals("1", mapper.to(RulePriority.BLOCKER));
    assertEquals("2", mapper.to(RulePriority.CRITICAL));
    assertEquals("3", mapper.to(RulePriority.MAJOR));
    assertEquals("4", mapper.to(RulePriority.MINOR));
    assertEquals("5", mapper.to(RulePriority.INFO));
    assertEquals(null, mapper.to(null));
  }
}
