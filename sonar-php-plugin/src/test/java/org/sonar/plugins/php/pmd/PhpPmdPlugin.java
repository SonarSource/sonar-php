/*
 * Sonar PHP Plugin
 * Copyright (C) 2010 Akram Ben Aissi or Jerome Tama or Frederic Leroy
 * mailto: akram.benaissi@free.fr or jerome.tama@codehaus.org
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

package org.sonar.plugins.php.pmd;

import java.util.ArrayList;
import java.util.List;

import org.sonar.api.Extension;
import org.sonar.api.Plugin;
import org.sonar.plugins.php.core.PhpPlugin;

/**
 * The Class PhpPmdPlugin class declares all extensions to be run for a project to be analyzed by the PHPMD tool.
 */
public class PhpPmdPlugin implements Plugin {

  /**
   * Gets the description.
   * 
   * @return the description *
   * @see org.sonar.api.Plugin#getDescription()
   */
  public String getDescription() {
    return "A plugin to cover the PMD PHP";
  }

  /**
   * Gets the extensions.
   * 
   * @return the extensions
   * @see org.sonar.api.Plugin#getExtensions()
   */
  public List<Class<? extends Extension>> getExtensions() {
    List<Class<? extends Extension>> extensions = new ArrayList<Class<? extends Extension>>();
    extensions.add(PhpmdSensor.class);
    // extensions.add(PhpPmdRulesRepository.class);
    return extensions;
  }

  /**
   * Gets the key.
   * 
   * @return the key
   * @see org.sonar.api.Plugin#getKey()
   */
  public String getKey() {
    return PhpPlugin.PHPMD_PLUGIN_KEY;
  }

  /**
   * Gets the name.
   * 
   * @return the name
   * @see org.sonar.api.Plugin#getName()
   */
  public String getName() {
    return "PHPMD";
  }

  /**
   * To string.
   * 
   * @return the string
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return getKey();
  }
}
