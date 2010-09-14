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

package org.sonar.plugins.checkstyle;

import org.sonar.api.rules.RulePriority;
import org.sonar.api.rules.RulePriorityMapper;

/**
 * The Class CheckstyleRulePriorityMapper.
 */
public class CheckstyleRulePriorityMapper implements RulePriorityMapper<String, String> {

  /*
   * (non-Javadoc)
   * 
   * @see org.sonar.api.rules.RulePriorityMapper#from(java.lang.Object)
   */
  public RulePriority from(String priority) {
    if ("error".equalsIgnoreCase(priority)) {
      return RulePriority.BLOCKER;
    }
    if ("warning".equalsIgnoreCase(priority)) {
      return RulePriority.MAJOR;
    }
    if ("info".equalsIgnoreCase(priority)) {
      return RulePriority.INFO;
    }
    // ignore level returns null
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.sonar.api.rules.RulePriorityMapper#to(org.sonar.api.rules.RulePriority )
   */
  public String to(RulePriority priority) {
    if (RulePriority.BLOCKER.equals(priority) || RulePriority.CRITICAL.equals(priority)) {
      return "error";
    }
    if (RulePriority.MAJOR.equals(priority)) {
      return "warning";
    }
    if (RulePriority.MINOR.equals(priority) || RulePriority.INFO.equals(priority)) {
      return "info";
    }
    throw new IllegalArgumentException("Priority not supported: " + priority);
  }

}
