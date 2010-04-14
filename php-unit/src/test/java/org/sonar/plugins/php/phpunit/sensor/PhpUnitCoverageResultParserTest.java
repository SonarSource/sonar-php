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

package org.sonar.plugins.php.phpunit.sensor;

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Arrays;

import org.apache.maven.project.MavenProject;
import org.junit.Test;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.Measure;
import org.sonar.api.measures.Metric;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.ProjectFileSystem;
import org.sonar.plugins.php.core.resources.PhpFile;
import org.sonar.plugins.php.core.resources.PhpPackage;
import org.sonar.plugins.php.phpunit.configuration.PhpUnitConfiguration;

public class PhpUnitCoverageResultParserTest {

	/** The context. */
	private SensorContext context;

	/** The config. */
	private PhpUnitConfiguration config;

	/** The project. */
	private Project project;

	/** The metric. */
	private Metric metric;

	/**
	 * Inits the.
	 */
	private void init() {
		try {
			config = mock(PhpUnitConfiguration.class);
			project = mock(Project.class);
			context = mock(SensorContext.class);
			MavenProject mavenProject = mock(MavenProject.class);
			ProjectFileSystem fs = mock(ProjectFileSystem.class);
			when(project.getPom()).thenReturn(mavenProject);
			when(project.getFileSystem()).thenReturn(fs);
			when(fs.getSourceDirs()).thenReturn(Arrays.asList(new File("C:\\projets\\PHP\\Monkey\\sources\\main")));
			when(fs.getTestDirs()).thenReturn(Arrays.asList(new File("C:\\projets\\PHP\\Monkey\\sources\\test")));
			when(mavenProject.getPackaging()).thenReturn("maven-plugin");
			when(config.getReportFile()).thenReturn(new File(getClass().getResource("/org/sonar/plugins/php/phpunit/sensor/phpunit.coverage.xml").getFile()));
			PhpUnitCoverageResultParser parser = new PhpUnitCoverageResultParser(project, context);
			parser.parse(config.getReportFile());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Should not throw an exception when report not found.
	 */
	@Test
	public void shouldNotThrowAnExceptionWhenReportNotFound() {
		config = mock(PhpUnitConfiguration.class);
		project = mock(Project.class);
		context = mock(SensorContext.class);
		MavenProject mavenProject = mock(MavenProject.class);
		when(project.getPom()).thenReturn(mavenProject);
		when(mavenProject.getPackaging()).thenReturn("maven-plugin");
		when(config.getReportFile()).thenReturn(new File("path/to/nowhere"));
		PhpUnitCoverageResultParser parser = new PhpUnitCoverageResultParser(project, context);
		parser.parse(null);
	}

	/**
	 * Should save zero value when report not found.
	 */
	@Test
	public void shouldSaveZeroValueWhenReportNotFound() {
		metric = CoreMetrics.COVERAGE;
		config = mock(PhpUnitConfiguration.class);
		project = mock(Project.class);
		context = mock(SensorContext.class);
		MavenProject mavenProject = mock(MavenProject.class);
		when(project.getPom()).thenReturn(mavenProject);
		when(mavenProject.getPackaging()).thenReturn("maven-plugin");
		PhpUnitCoverageResultParser parser = new PhpUnitCoverageResultParser(project, context);
		parser.parse(null);
		verify(context).saveMeasure(metric, 0.0);
	}

	/**
	 * Should generate coverage metrics.
	 */
	@Test()
	public void shouldGenerateCoverageMeasures() {
		metric = CoreMetrics.COVERAGE;
		init();
		PhpFile phpFile = new PhpFile("Banana", true);
		verify(context).saveMeasure(phpFile, metric, 0.0);
		
		phpFile = new PhpFile("Monkey");
		verify(context).saveMeasure(phpFile, metric, 50.0);
		
		PhpPackage phpPackage = new PhpPackage("");
		verify(context).saveMeasure(phpPackage, metric, 25.0);
	}

	/**
	 * Should not generate coverage metrics for files that are not under project sources dirs.
	 */
	@Test()
	public void shouldNotGenerateCoverageMeasures() {
		metric = CoreMetrics.COVERAGE;
		init();
		verify(context, never()).saveMeasure(new PhpFile("IndexControllerTest", true), metric, 1.0);
	}

	/**
	 * Should generate line hits metrics.
	 */
	@Test()
	public void shouldGenerateLineHitsMeasures() {
		metric = CoreMetrics.COVERAGE_LINE_HITS_DATA;
		init();
		verify(context, atLeastOnce()).saveMeasure(new PhpFile("Monkey", true),
		    new Measure(metric, "34=1;35=1;38=1;40=0;45=1;46=1"));
		verify(context, atLeastOnce()).saveMeasure(new PhpFile("Banana"), new Measure(metric, "34=0;35=0;38=0;40=0;45=1;46=1"));
	}

	/**
	 * Should generate project converage metric.
	 */
	@Test()
	public void shouldGenerateProjectCoverageMeasures() {
		metric = CoreMetrics.COVERAGE;
		init();
		verify(context, times(1)).saveMeasure(metric, 25.0);
	}

}
