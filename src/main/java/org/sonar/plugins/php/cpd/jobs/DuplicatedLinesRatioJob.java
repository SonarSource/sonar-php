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

package org.sonar.plugins.php.cpd.jobs;

import org.sonar.commons.Language;
import org.sonar.commons.Languages;
import org.sonar.commons.Metric;
import org.sonar.commons.resources.Measure;
import org.sonar.commons.resources.MeasureKey;
import org.sonar.commons.resources.Resource;
import org.sonar.plugins.api.jobs.AbstractJob;
import org.sonar.plugins.api.jobs.JobContext;
import org.sonar.plugins.api.metrics.CoreMetrics;

import java.util.Arrays;
import java.util.List;

public class DuplicatedLinesRatioJob extends AbstractJob {
  public DuplicatedLinesRatioJob(Languages languages) {
    super(languages);
  }

  protected boolean shouldExecuteOnLanguage(Language language) {
    return language != null;
  }

  public boolean shouldExecuteOnResource(Resource resource) {
    return !resource.isFile();
  }

  @Override
  public List<Metric> dependsOnMetrics() {
    return Arrays.asList(CoreMetrics.DUPLICATED_LINES, CoreMetrics.LOC);
  }

  @Override
  public List<Metric> generatesMetrics() {
    return Arrays.asList(CoreMetrics.DUPLICATED_LINES_RATIO);
  }

  public void execute(JobContext node) {
    Measure nbDuplicatedLines = node.getMeasure(new MeasureKey(CoreMetrics.DUPLICATED_LINES));
    Measure nbLines = node.getMeasure(new MeasureKey(CoreMetrics.LOC));

    if (nbDuplicatedLines != null && nbLines != null) {
      Double divisor = nbLines.getValue();
      if (divisor > 0.0) {
        node.addMeasure(CoreMetrics.DUPLICATED_LINES_RATIO, calculate(nbDuplicatedLines.getValue(), divisor));
      }
    }
  }

  protected Double calculate(Double dividend, Double divisor) {
    Double result = 100.0 * dividend / divisor;
    if (result < 100.0) {
      return result;
    } else {
      return 100.0;
    }
  }

}