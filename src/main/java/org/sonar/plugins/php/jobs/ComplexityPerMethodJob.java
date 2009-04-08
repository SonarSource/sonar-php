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

package org.sonar.plugins.php.jobs;

import org.sonar.commons.Language;
import org.sonar.commons.Languages;
import org.sonar.commons.Metric;
import org.sonar.commons.resources.Resource;
import org.sonar.plugins.api.jobs.AbstractWeightingJob;
import org.sonar.plugins.api.metrics.CoreMetrics;
import org.sonar.plugins.php.Php;

public class ComplexityPerMethodJob extends AbstractWeightingJob {

  public ComplexityPerMethodJob(Languages languages) {
    super(languages);
  }

  protected Metric getTargetMetric() {
    return CoreMetrics.COMPLEXITY_AVG_BY_FUNCTION;
  }

  protected Metric getDivisorMetric() {
    return CoreMetrics.FUNCTIONS_COUNT;
  }

  protected Metric getDividendMetric() {
    return CoreMetrics.COMPLEXITY;
  }

  protected boolean shouldExecuteOnLanguage(Language language) {
    return language != null && language.getKey().equals(Php.KEY);
  }

  public boolean shouldExecuteOnResource(Resource resource) {
    return !resource.isUnitTest();
  }
}