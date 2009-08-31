/*
 * Sonar, open source software quality management tool.
 * Copyright (C) 2009 SonarSource SA
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

package org.sonar.plugins.php;

import org.sonar.api.Plugin;
import org.sonar.api.Extension;
import org.sonar.plugins.php.sensors.PhpSourceImporter;
import org.sonar.plugins.php.decorators.PhpDirectoryDecorator;
import org.sonar.plugins.php.decorators.PhpFilesDecorator;
import org.sonar.plugins.php.phpdepend.PhpDependSensor;

import java.util.List;
import java.util.ArrayList;

public class PhpPlugin implements Plugin {

  public String getKey() {
    return "php-language";
  }

  public String getName() {
    return "PHP Language";
  }

  public String getDescription() {
    return "A plugin to cover the PHP language";
  }

  public List<Class<? extends Extension>> getExtensions() {
    List<Class<? extends Extension>> list = new ArrayList<Class<? extends Extension>>();
    list.add(Php.class);
    list.add(PhpSourceImporter.class);
    list.add(PhpDependSensor.class);
    list.add(PhpDirectoryDecorator.class);
    list.add(PhpFilesDecorator.class);
    return list;
  }

  public String toString() {
    return getKey();
  }
}
