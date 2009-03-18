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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class DirectoryMeasures {

  private Map<Metric, MeasuresByResource> directoyMeasures;

  public DirectoryMeasures() {
    this.directoyMeasures = new HashMap<Metric, MeasuresByResource>();
  }

  public void add(Double value, Metric metric, Resource resource) {
    MeasuresByResource measuresByResource = directoyMeasures.get(metric);
    if (measuresByResource == null) {
      measuresByResource = new MeasuresByResource();
    }
    measuresByResource.add(value, resource);
    directoyMeasures.put(metric, measuresByResource);
  }

  public Set<Metric> getKeys() {
    return directoyMeasures.keySet();
  }

  public Set<Resource> getResources(Metric metric) {
    MeasuresByResource measuresByResource = directoyMeasures.get(metric);
    return measuresByResource.getKeys();
  }

  public Double getMeasure(Metric metric, Resource resource) {
    MeasuresByResource measuresByResource = directoyMeasures.get(metric);
    return measuresByResource.getMeasure(resource);
  }


  class MeasuresByResource {
    private Map<Resource, Double> measuresByResource;

    public MeasuresByResource() {
      this.measuresByResource = new HashMap<Resource, Double>();
    }

    public void add(Double value, Resource resource) {
      Double val = measuresByResource.get(resource);
      if (val == null) {
        val = 0.0;
      }
      val += value;
      measuresByResource.put(resource, val);
    }

    public Set<Resource> getKeys() {
      return measuresByResource.keySet();
    }

    public Double getMeasure(Resource resource) {
      return measuresByResource.get(resource);
    }
  }
}
