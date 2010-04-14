/*
 * Sonar, open source software quality management tool.
 * Copyright (C) 2010 MyCompany
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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.sonar.api.measures.Metric;
import org.sonar.plugins.php.core.resources.PhpFile;

/**
 * The Class ResourcesBag.
 */
public class ResourcesBag {

	/**
	 * The Class MeasuresByMetric.
	 */
	class MeasuresByMetric {

		/** The measures by metric. */
		private Map<Metric, Double> measuresByMetric;

		/**
		 * Instantiates a new measures by metric.
		 */
		public MeasuresByMetric() {
			this.measuresByMetric = new HashMap<Metric, Double>();
		}

		/**
		 * Adds the.
		 * 
		 * @param value the value
		 * @param metric the metric
		 */
		public void add(Double value, Metric metric) {
			Double val = measuresByMetric.get(metric);
			if (val == null) {
				val = 0.0;
			}
			val += value;
			measuresByMetric.put(metric, val);
		}

		/**
		 * Gets the measure.
		 * 
		 * @param metric the metric
		 * @return the measure
		 */
		public Double getMeasure(Metric metric) {
			return measuresByMetric.get(metric);
		}

		/**
		 * Gets the metrics.
		 * 
		 * @return the metrics
		 */
		public Set<Metric> getMetrics() {
			return measuresByMetric.keySet();
		}
	}

	/** The resource measures. */
	private Map<PhpFile, MeasuresByMetric> resourceMeasures;

	/**
	 * Instantiates a new resources bag.
	 */
	public ResourcesBag() {
		this.resourceMeasures = new HashMap<PhpFile, MeasuresByMetric>();
	}

	/**
	 * Associate the given velue, to the given file and metrics.
	 * 
	 * @param value the value
	 * @param metric the metric
	 * @param resource the resource
	 */
	public void add(Double value, Metric metric, PhpFile resource) {
		MeasuresByMetric measuresByMetric = resourceMeasures.get(resource);
		if (measuresByMetric == null) {
			measuresByMetric = new MeasuresByMetric();
		}
		measuresByMetric.add(value, metric);
		resourceMeasures.put(resource, measuresByMetric);
	}

	/**
	 * Gets the measure.
	 * 
	 * @param metric the metric
	 * @param resource the resource
	 * @return the measure
	 */
	public Double getMeasure(Metric metric, PhpFile resource) {
		MeasuresByMetric measuresByMetric = resourceMeasures.get(resource);
		return measuresByMetric.getMeasure(metric);
	}

	/**
	 * Gets the metrics.
	 * 
	 * @param resource the resource
	 * @return the metrics
	 */
	public Set<Metric> getMetrics(PhpFile resource) {
		MeasuresByMetric measuresByMetric = resourceMeasures.get(resource);
		return measuresByMetric.getMetrics();
	}

	/**
	 * Gets the resources.
	 * 
	 * @return the resources
	 */
	public Set<PhpFile> getResources() {
		return resourceMeasures.keySet();
	}
}