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
import org.sonar.plugins.api.jobs.AbstractSumChildrenJob;
import org.sonar.plugins.api.metrics.CoreMetrics;

public class DuplicatedLinesJob extends AbstractSumChildrenJob {
  public DuplicatedLinesJob(Languages languages) {
    super(languages);
  }

  protected boolean shouldExecuteOnLanguage(Language language) {
    return language != null;
  }

  public Metric getMetric() {
    return CoreMetrics.DUPLICATED_LINES;
  }

  @Override
  protected boolean shouldInsertZeroIfNoChildrenMeasures() {
    return true;
  }
}
