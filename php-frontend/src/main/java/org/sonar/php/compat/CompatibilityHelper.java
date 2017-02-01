/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2017 SonarSource SA
 * mailto:info AT sonarsource DOT com
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
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.php.compat;

import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.utils.Version;
import org.sonar.plugins.php.api.visitors.PhpFile;

/**
 * {@link PhpFile} factory class using SQ runtime version to choose which class to instantiate.
 */
public class CompatibilityHelper {

  private static final Version V6_0 = Version.create(6, 0);
  private static final Version V6_2 = Version.create(6, 2);

  private CompatibilityHelper() {
    // utility class, forbidden constructor
  }

  public static PhpFile phpFile(InputFile inputFile, SensorContext context) {
    Version version = context.getSonarQubeVersion();
    if (version.isGreaterThanOrEqual(V6_2)) {
      return new CompatibleInputFile(inputFile);
    }
    if (version.isGreaterThanOrEqual(V6_0)) {
      return new InputFileV60Compat(inputFile);
    }
    return new InputFileV56Compat(inputFile, context);
  }
}
