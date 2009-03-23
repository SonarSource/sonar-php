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

package org.sonar.plugins.php.phpdepend;

import org.sonar.commons.Metric;
import org.sonar.commons.resources.Resource;
import org.sonar.plugins.php.Php;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ResourcesManager {

  private Map<Resource, MeasuresByMetric> resourceMeasures;

  public ResourcesManager() {
    this.resourceMeasures = new HashMap<Resource, MeasuresByMetric>();
  }

  private void add(Double value, Metric metric, Resource resource) {
    MeasuresByMetric measuresByMetric = resourceMeasures.get(resource);
    if (measuresByMetric == null) {
      measuresByMetric = new MeasuresByMetric();
    }
    measuresByMetric.add(value, metric);
    resourceMeasures.put(resource, measuresByMetric);
  }

  public void addFile(Double value, Metric metric, Resource resource){
    add(value, metric, resource);
    Resource parent = new Php().getParent(resource);
    if (parent != null){
      add(value, metric, parent);
    }
  }

  public void addProject(Double value, Metric metric){
    add(value, metric, null);
  }

  public Set<Resource> getResources() {
    return resourceMeasures.keySet();
  }

  public Set<Metric> getMetrics(Resource resource) {
    MeasuresByMetric measuresByMetric = resourceMeasures.get(resource);
    return measuresByMetric.getMetrics();
  }

  public Double getMeasure(Metric metric, Resource resource) {
    MeasuresByMetric measuresByMetric = resourceMeasures.get(resource);
    return measuresByMetric.getMeasure(metric);
  }


  class MeasuresByMetric {
    private Map<Metric, Double> measuresByMetric;

    public MeasuresByMetric() {
      this.measuresByMetric = new HashMap<Metric, Double>();
    }

    public void add(Double value, Metric metric) {
      Double val = measuresByMetric.get(metric);
      if (val == null) {
        val = 0.0;
      }
      val += value;
      measuresByMetric.put(metric, val);
    }

    public Set<Metric> getMetrics() {
      return measuresByMetric.keySet();
    }

    public Double getMeasure(Metric metric) {
      return measuresByMetric.get(metric);
    }
  }
}