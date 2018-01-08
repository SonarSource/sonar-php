/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2018 SonarSource SA
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
package org.sonar.plugins.php.phpunit;

import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;


public class MultiPathImporter implements ReportImporter {

  private static final Logger LOG = Loggers.get(MultiPathImporter.class);

  private final SingleFileReportImporter importer;
  private final String pathsKey;
  private final String msg;

  public MultiPathImporter(SingleFileReportImporter importer, String pathsKey, String msg) {
    this.importer = importer;
    this.pathsKey = pathsKey;
    this.msg = msg;
  }

  @Override
  public void importReport(SensorContext context) {
    final String[] paths = context.config().getStringArray(pathsKey);
    if (paths.length == 0) {
      LOG.info("No PHPUnit {} reports provided (see '{}' property)", msg, pathsKey);
      return;
    }
    for (String path : paths) {
      if (!path.isEmpty()) {
        importer.importReport(path, msg, context);
      }
    }
  }
}
