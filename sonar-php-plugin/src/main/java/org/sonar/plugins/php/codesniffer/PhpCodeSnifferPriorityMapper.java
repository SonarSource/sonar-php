/*
 * Sonar PHP Plugin
 * Copyright (C) 2010 Sonar PHP Plugin
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

package org.sonar.plugins.php.codesniffer;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.sonar.api.BatchExtension;
import org.sonar.api.ServerExtension;
import org.sonar.api.rules.RulePriority;

public final class PhpCodeSnifferPriorityMapper implements ServerExtension, BatchExtension {

  private static final String INFO_PRIORITY = "info";
  private static final String WARNING_PRIORITY = "warning";
  private static final String ERROR_PRIORITY = "error";

  private Map<String, RulePriority> from = new TreeMap<String, RulePriority>(String.CASE_INSENSITIVE_ORDER);
  private Map<RulePriority, String> to = new HashMap<RulePriority, String>();

  /**
   * 
   */
  public PhpCodeSnifferPriorityMapper() {

    from.put(ERROR_PRIORITY, RulePriority.BLOCKER);
    from.put(WARNING_PRIORITY, RulePriority.MAJOR);
    from.put(INFO_PRIORITY, RulePriority.INFO);

    to.put(RulePriority.BLOCKER, ERROR_PRIORITY);
    to.put(RulePriority.CRITICAL, ERROR_PRIORITY);
    to.put(RulePriority.MAJOR, WARNING_PRIORITY);
    to.put(RulePriority.MINOR, INFO_PRIORITY);
    to.put(RulePriority.INFO, INFO_PRIORITY);

  }

  /**
   * @param priority
   * @return
   */
  public RulePriority from(String priority) {
    if (priority == null || from.get(priority) == null) {
      throw new IllegalArgumentException("Priority not supported: " + priority);
    }
    return from.get(priority);
  }

  /**
   * @param rulePriority
   * @return
   */
  public String to(RulePriority rulePriority) {
    String priority = to.get(rulePriority);
    if (priority == null) {
      throw new IllegalArgumentException("Priority not supported: " + rulePriority);
    }
    return priority;
  }

}
