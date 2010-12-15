/*
 * Sonar, open source software quality management tool.
 * Copyright (C) 2010 SQLi
 * mailto:contact AT sonarsource DOT com
 *
 * Sonar is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * Sonar is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Sonar; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */

/**
 * 
 */
package org.sonar.plugins.checkstyle;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.sonar.api.rules.RulePriority;

/**
 * @author akram
 * 
 */
public class CheckstyleRulePriorityMapperTest {

  /**
   * Test method for {@link org.sonar.plugins.checkstyle.CheckstyleRulePriorityMapper#from(java.lang.String)}.
   */
  @Test
  public void testFrom() {
    CheckstyleRulePriorityMapper mapper = new CheckstyleRulePriorityMapper();
    assertEquals(RulePriority.BLOCKER, mapper.from("error"));
    assertEquals(RulePriority.MAJOR, mapper.from("warning"));
    assertEquals(RulePriority.INFO, mapper.from("info"));
    assertEquals(null, mapper.from(null));
    assertEquals(null, mapper.from("RANDOM"));

  }

  /**
   * Test method for {@link org.sonar.plugins.checkstyle.CheckstyleRulePriorityMapper#to(org.sonar.api.rules.RulePriority)}.
   */
  @Test(expected = IllegalArgumentException.class)
  public void testTo() {
    CheckstyleRulePriorityMapper mapper = new CheckstyleRulePriorityMapper();
    assertEquals("info", mapper.to(RulePriority.INFO));
    assertEquals("info", mapper.to(RulePriority.MINOR));
    assertEquals("warning", mapper.to(RulePriority.MAJOR));
    assertEquals("error", mapper.to(RulePriority.BLOCKER));
    assertEquals("error", mapper.to(RulePriority.CRITICAL));
    assertEquals(null, mapper.to(null));
  }
}
