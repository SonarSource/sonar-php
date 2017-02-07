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
package org.sonar.plugins.php.phpunit;

import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.sensor.coverage.CoverageType;
import org.sonar.api.measures.CoreMetrics;

/**
 * The Class PhpUnitItCoverageResultParser.
 */
public class PhpUnitItCoverageResultParser extends PhpUnitCoverageResultParser {

  /**
   * Instantiates a new php unit coverage result parser.
   */
  public PhpUnitItCoverageResultParser(FileSystem fileSystem) {
    super(fileSystem);
    linesToCoverMetric = CoreMetrics.IT_LINES_TO_COVER;
    uncoveredLinesMetric = CoreMetrics.IT_UNCOVERED_LINES;
    coverageType = CoverageType.IT;
  }

  @Override
  public String toString() {
    return "PHPUnit IT Coverage Result Parser";
  }

}
