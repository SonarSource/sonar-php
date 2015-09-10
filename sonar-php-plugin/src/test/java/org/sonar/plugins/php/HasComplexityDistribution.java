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
package org.sonar.plugins.php;

import org.mockito.ArgumentMatcher;
import org.sonar.api.measures.Measure;
import org.sonar.api.measures.Metric;

/**
 * Argument matcher for verifing complexity distribution
 *
 * @author Sebastian Marek
 */
public class HasComplexityDistribution extends ArgumentMatcher {

  private Metric expectedMetric;
  private String expectedMeasure;

  /**
   * Stores expected complexity distribution for a given metric
   *
   * @param metric
   * @param measure
   */
  public HasComplexityDistribution(Metric metric, String measure) {
    super();
    expectedMetric = metric;
    expectedMeasure = measure;
  }

  /**
   * Checks whether given measure matches the expectation
   *
   * @param measure
   * @return boolean
   */
  public boolean matches(Object measure) {

    return (
      ((Measure) measure).getMetric().equals(expectedMetric) &&
        ((Measure) measure).getData().equals(expectedMeasure)
    );
  }
}
