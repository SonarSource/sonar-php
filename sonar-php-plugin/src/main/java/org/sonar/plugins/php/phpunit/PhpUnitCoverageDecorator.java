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
package org.sonar.plugins.php.phpunit;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.Decorator;
import org.sonar.api.batch.DecoratorContext;
import org.sonar.api.batch.DependedUpon;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.Measure;
import org.sonar.api.measures.Metric;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.Resource;
import org.sonar.api.resources.ResourceUtils;
import org.sonar.plugins.php.api.Php;

/**
 * Decorates resources that do not have coverage metrics because they were not touched by any test, and thus not present in the coverage
 * report file.
 */
public class PhpUnitCoverageDecorator implements Decorator {

  private static final Logger LOG = LoggerFactory.getLogger(PhpUnitCoverageDecorator.class);

  private PhpUnitConfiguration configuration;

  public PhpUnitCoverageDecorator(PhpUnitConfiguration configuration) {
    this.configuration = configuration;
  }

  /**
   * {@inheritDoc}
   */
  public boolean shouldExecuteOnProject(Project project) {
    if (!Php.KEY.equals(project.getLanguageKey()) || !configuration.isDynamicAnalysisEnabled()) {
      return false;
    }
    return !configuration.shouldSkipCoverage();
  }

  @DependedUpon
  public List<Metric> generatesCoverageMetrics() {
    return Arrays.asList(CoreMetrics.COVERAGE, CoreMetrics.LINE_COVERAGE, CoreMetrics.LINES_TO_COVER, CoreMetrics.UNCOVERED_LINES);
  }

  /**
   * {@inheritDoc}
   */
  @SuppressWarnings("rawtypes")
  public void decorate(Resource resource, DecoratorContext context) {
    if (ResourceUtils.isFile(resource) && context.getMeasure(CoreMetrics.LINE_COVERAGE) == null) {
      LOG.debug("Coverage metrics have not been set on '{}': default values will be inserted.", resource.getName());
      context.saveMeasure(CoreMetrics.LINE_COVERAGE, 0.0);
      // for LINES_TO_COVER and UNCOVERED_LINES, we use NCLOC as an approximation
      Measure ncloc = context.getMeasure(CoreMetrics.NCLOC);
      if (ncloc != null && context.getMeasure(CoreMetrics.LINES_TO_COVER) == null) {
        context.saveMeasure(CoreMetrics.LINES_TO_COVER, ncloc.getValue());
      }
      if (ncloc != null && context.getMeasure(CoreMetrics.UNCOVERED_LINES) == null) {
        context.saveMeasure(CoreMetrics.UNCOVERED_LINES, ncloc.getValue());
      }
    }
  }

}
