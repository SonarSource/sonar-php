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

package org.sonar.plugins.pmd;

import org.sonar.api.rules.RulePriority;
import org.sonar.api.rules.RulePriorityMapper;

/**
 * The Class PmdRulePriorityMapper.
 */
public class PmdRulePriorityMapper implements RulePriorityMapper<String, String> {

  /**
   * Returns a RulePriority instance from a given string
   * 
   * @param level
   *          the given value
   * 
   * @return the rule priority
   * 
   * @see org.sonar.api.rules.RulePriorityMapper#from(java.lang.Object) Major priority will be returned if given level is null
   */
  public RulePriority from(String level) {
    if ("1".equals(level)) {
      return RulePriority.BLOCKER;
    }
    if ("2".equals(level)) {
      return RulePriority.CRITICAL;
    }
    if ("4".equals(level)) {
      return RulePriority.MINOR;
    }
    if ("5".equals(level)) {
      return RulePriority.INFO;
    }
    return RulePriority.MAJOR;
  }

  /**
   * Returns a string from a RulePriority instance.
   * 
   * @param priority
   *          the priority
   * 
   * @return the string
   * 
   * @see org.sonar.api.rules.RulePriorityMapper#to(org.sonar.api.rules.RulePriority)
   */
  public String to(RulePriority priority) {
    if (priority.equals(RulePriority.BLOCKER)) {
      return "1";
    }
    if (priority.equals(RulePriority.CRITICAL)) {
      return "2";
    }
    if (priority.equals(RulePriority.MAJOR)) {
      return "3";
    }
    if (priority.equals(RulePriority.MINOR)) {
      return "4";
    }
    if (priority.equals(RulePriority.INFO)) {
      return "5";
    }
    throw new IllegalArgumentException("Level not supported: " + priority);
  }

}
