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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import org.junit.Test;
import org.sonar.commons.Metric;
import org.sonar.commons.resources.Resource;

public class ResourcesBagTest {

  @Test
  public void shouldGetAPreviousAddedValueToAResource(){
    ResourcesBag resourcesBag = new ResourcesBag();
    Metric metric = aMetricFixture();
    Resource resource = aResourceFixture();

    resourcesBag.add(1d, metric, resource);

    Double result = resourcesBag.getMeasure(metric, resource);
    assertThat(result, is(1d));    
  }
  
  @Test
  public void shouldGetAPreviousAddedValueToANullResource(){
    ResourcesBag resourcesBag = new ResourcesBag();
    Metric metric = aMetricFixture();

    resourcesBag.add(1d, metric, null);

    Double result = resourcesBag.getMeasure(metric, null);
    assertThat(result, is(1d));
  }

  @Test
  public void shouldDoTheSumWhenAddingTwiceAFileAMetric() {
    ResourcesBag resourcesBag = new ResourcesBag();
    Metric metric = aMetricFixture();
    Resource resource = aResourceFixture();

    resourcesBag.add(1d, metric, resource);
    resourcesBag.add(3d, metric, resource);

    Double result = resourcesBag.getMeasure(metric, resource);
    assertThat(result, is(4d));
  }

  @Test
  public void shouldDoTheSumByMetrics() {
    ResourcesBag resourcesBag = new ResourcesBag();
    Metric metric = aMetricFixture();
    Metric anotherMetric = anotherMetricFixture();
    Resource resource = aResourceFixture();

    resourcesBag.add(1d, metric, resource);
    resourcesBag.add(2d, metric, resource);
    resourcesBag.add(5d, anotherMetric, resource);
    resourcesBag.add(6d, anotherMetric, resource);

    Double result = resourcesBag.getMeasure(metric, resource);
    assertThat(result, is(3d));
    Double result2 = resourcesBag.getMeasure(anotherMetric, resource);
    assertThat(result2, is(11d));
  }

  public Metric aMetricFixture() {
    return new Metric("aKey");
  }

  public Metric anotherMetricFixture() {
    return new Metric("anotherKey");
  }

  public Resource aResourceFixture() {
    return new Resource("aScope", "aKey", "aQualifier", 1, "aName");
  }
}
