/*
 * Sonar PHP Plugin
 * Copyright (C) 2010 Akram Ben Aissi
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

package org.sonar.plugins.php.phpunit;

import java.util.ArrayList;
import java.util.List;

import org.sonar.api.Extension;
import org.sonar.api.Plugin;

/**
 * The PhpUnitPlugin handles files and metrics provided by punit tool.
 * 
 * @author jtama
 */
public class PhpUnitPlugin implements Plugin {

  /** The plugin key. */
  public static final String KEY = "PHP UNIT";

  /**
   * Gets the description.
   * 
   * @return the description
   * @see org.sonar.api.Plugin#getDescription()
   */
  public final String getDescription() {
    return "A plugin to cover the PHP unit files";
  }

  /**
   * Gets the extensions.
   * 
   * @return the extensions
   * @see org.sonar.api.Plugin#getExtensions()
   */
  public final List<Class<? extends Extension>> getExtensions() {
    List<Class<? extends Extension>> extensions = new ArrayList<Class<? extends Extension>>();
    extensions.add(PhpUnitSensor.class);
    return extensions;
  }

  /**
   * Gets the key.
   * 
   * @return the key
   * @see org.sonar.api.Plugin#getKey()
   */
  public final String getKey() {
    return KEY;
  }

  /**
   * Gets the name.
   * 
   * @return the name
   * @see org.sonar.api.Plugin#getName()
   */
  public final String getName() {
    return "PHP UNIT";
  }

  /**
   * To string.
   * 
   * @return the string
   * @see java.lang.Object#toString()
   */
  @Override
  public final String toString() {
    return getKey();
  }
}
