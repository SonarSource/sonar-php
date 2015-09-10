/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010 SonarSource and Akram Ben Aissi
 * sonarqube@googlegroups.com
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

import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.measures.CoreMetrics;

/**
 * The Class PhpUnitOverallCoverageResultParser.
 */
public class PhpUnitOverallCoverageResultParser extends PhpUnitCoverageResultParser {

  /**
   * Instantiates a new php unit coverage result parser.
   *
   * @param context    the context
   */
  public PhpUnitOverallCoverageResultParser(SensorContext context, FileSystem fileSystem) {
    super(context, fileSystem);
    lineCoverageMetric = CoreMetrics.OVERALL_LINE_COVERAGE;
    linesToCoverMetric = CoreMetrics.OVERALL_LINES_TO_COVER;
    uncoveredLinesMetric = CoreMetrics.OVERALL_UNCOVERED_LINES;
    coverageLineHitsDataMetric = CoreMetrics.OVERALL_COVERAGE_LINE_HITS_DATA;
  }

  @Override
  public String toString() {
    return "PHPUnit Overall Coverage Result Parser";

  }

}
