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
package org.sonar.plugins.php.core;

import org.sonar.api.rules.RulePriority;

/**
 * Represents implementations that maps priority/severity systems between Sonar and another tool.
 */
public interface PriorityMapper {

  /**
   * Returns the Sonar priority corresponding to the given tool severity.
   * 
   * @param priority
   *          the tool severity
   * @return the Sonar priority
   */
  RulePriority from(String priority);

  /**
   * Returns the tool priority corresponding to the given Sonar priority.
   * 
   * @param rulePriority
   *          the Sonar priority
   * @return the tool priority
   */
  String to(RulePriority rulePriority);

}
