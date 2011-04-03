/*
 * Sonar PHP Plugin
 * Copyright (C) 2010 Sonar PHP Plugin
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

package org.sonar.plugins.php.phpdepend;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import org.junit.Test;
import org.sonar.api.measures.Metric;
import org.sonar.plugins.php.core.PhpFile;

/**
 * The Class ResourcesBagTest.
 */
public class ResourcesBagTest {

  /**
   * Should get a previous added value to a resource.
   */
  @Test
  public void shouldGetAPreviousAddedValueToAResource() {
    ResourcesBag resourcesBag = new ResourcesBag();
    Metric metric = aMetricFixture();
    PhpFile resource = aResourceFixture();

    resourcesBag.add(1d, metric, resource);

    Double result = resourcesBag.getMeasure(metric, resource);
    assertThat(result, is(1d));
  }

  /**
   * Should get a previous added value to a null resource.
   */
  @Test
  public void shouldGetAPreviousAddedValueToANullResource() {
    ResourcesBag resourcesBag = new ResourcesBag();
    Metric metric = aMetricFixture();

    resourcesBag.add(1d, metric, null);

    Double result = resourcesBag.getMeasure(metric, null);
    assertThat(result, is(1d));
  }

  /**
   * Should do the sum when adding twice a file.
   */
  @Test
  public void shouldDoTheSumWhenAddingTwiceAFile() {
    ResourcesBag resourcesBag = new ResourcesBag();
    Metric metric = aMetricFixture();
    PhpFile resource = aResourceFixture();

    resourcesBag.add(1d, metric, resource);
    resourcesBag.add(3d, metric, resource);

    Double result = resourcesBag.getMeasure(metric, resource);
    assertThat(result, is(4d));
  }

  /**
   * Should do the sum by metric.
   */
  @Test
  public void shouldDoTheSumByMetric() {
    ResourcesBag resourcesBag = new ResourcesBag();
    Metric metric = aMetricFixture();
    Metric anotherMetric = anotherMetricFixture();
    PhpFile resource = aResourceFixture();

    resourcesBag.add(1d, metric, resource);
    resourcesBag.add(2d, metric, resource);
    resourcesBag.add(5d, anotherMetric, resource);
    resourcesBag.add(6d, anotherMetric, resource);

    Double result = resourcesBag.getMeasure(metric, resource);
    assertThat(result, is(3d));
    Double result2 = resourcesBag.getMeasure(anotherMetric, resource);
    assertThat(result2, is(11d));
  }

  /**
   * A metric fixture.
   * 
   * @return the metric
   */
  public Metric aMetricFixture() {
    return new Metric("aKey");
  }

  /**
   * Another metric fixture.
   * 
   * @return the metric
   */
  public Metric anotherMetricFixture() {
    return new Metric("anotherKey");
  }

  /**
   * A resource fixture.
   * 
   * @return the resource
   */
  public PhpFile aResourceFixture() {
    return new PhpFile("aKey");
  }
}
