package org.sonar.plugins.php.phpunit.sensor;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Arrays;

import org.apache.commons.configuration.Configuration;
import org.apache.maven.project.MavenProject;
import org.junit.Test;
import org.sonar.api.resources.Java;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.ProjectFileSystem;
import org.sonar.api.utils.SonarException;
import org.sonar.plugins.php.core.Php;
import org.sonar.plugins.php.phpunit.configuration.PhpUnitConfiguration;

/**
 * The Class PhpUnitSensorTest.
 */
public class PhpUnitSensorTest {

	private Project project;

	private Configuration configuration;

	private PhpUnitSensor sensor;

	/**
	 * Sould not launch on non php project.
	 */
	@Test
	public void shouldNotLaunchOnNonPhpProject() {
		Project project = mock(Project.class);
		when(project.getLanguage()).thenReturn(Java.INSTANCE);
		Configuration configuration = mock(Configuration.class);
		PhpUnitSensor sensor = new PhpUnitSensor();
		when(
		    configuration.getString(PhpUnitConfiguration.REPORT_FILE_NAME_PROPERTY_KEY,
		        PhpUnitConfiguration.DEFAULT_REPORT_FILE_NAME)).thenReturn("punit.summary.xml");
		when(
		    configuration.getString(PhpUnitConfiguration.COVERAGE_REPORT_FILE_PROPERTY_KEY,
		        PhpUnitConfiguration.DEFAULT_REPORT_FILE_NAME)).thenReturn("punit.summary.xml");
		when(
		    configuration.getString(PhpUnitConfiguration.REPORT_FILE_RELATIVE_PATH_PROPERTY_KEY,
		        PhpUnitConfiguration.DEFAULT_REPORT_FILE_PATH)).thenReturn("reports");
		when(project.getConfiguration()).thenReturn(configuration);
		PhpUnitConfiguration config = mock(PhpUnitConfiguration.class);
		assertEquals(false, sensor.shouldExecuteOnProject(project));
		when(project.getLanguage()).thenReturn(Php.INSTANCE);
		when(config.isShouldRun()).thenReturn(false);
		assertEquals(false, sensor.shouldExecuteOnProject(project));
	}

	/**
	 * Sould not launch on non php project.
	 */
	@Test
	public void shouldNotLaunchWhenConfiguredSoOnPhpProject() {
		init();
		when(project.getLanguage()).thenReturn(Php.INSTANCE);
		when(
		    configuration.getBoolean(PhpUnitConfiguration.SHOULD_RUN_PROPERTY_KEY,
		        Boolean.getBoolean(PhpUnitConfiguration.DEFAULT_SHOULD_RUN))).thenReturn(false);
		assertEquals(false, sensor.shouldExecuteOnProject(project));
	}

	@Test(expected = SonarException.class)
	public void shouldThrowSonarExceptionWhenReportFileIsInvalid() {
		init();
		when(
		    configuration.getString(PhpUnitConfiguration.REPORT_FILE_NAME_PROPERTY_KEY,
		        PhpUnitConfiguration.DEFAULT_REPORT_FILE_NAME)).thenReturn("punit-invalid.summary.xml");
		when(
		    configuration.getBoolean(PhpUnitConfiguration.ANALYZE_ONLY_PROPERTY_KEY,
		        Boolean.getBoolean(PhpUnitConfiguration.DEFAULT_ANALYZE_ONLY))).thenReturn(true);
		sensor.analyse(project, null);
	}

	private void init() {
		project = mock(Project.class);
		configuration = mock(Configuration.class);
		sensor = new PhpUnitSensor();
		when(
		    configuration.getString(PhpUnitConfiguration.REPORT_FILE_NAME_PROPERTY_KEY,
		        PhpUnitConfiguration.DEFAULT_REPORT_FILE_NAME)).thenReturn("punit.summary.xml");
		when(
		    configuration.getString(PhpUnitConfiguration.REPORT_FILE_RELATIVE_PATH_PROPERTY_KEY,
		        PhpUnitConfiguration.DEFAULT_REPORT_FILE_PATH)).thenReturn("/log");
		when(
		    configuration.getString(PhpUnitConfiguration.COVERAGE_REPORT_FILE_PROPERTY_KEY,
		        PhpUnitConfiguration.DEFAULT_COVERAGE_REPORT_FILE)).thenReturn("phpunit.coverage.xml");
		when(
		    configuration.getBoolean(PhpUnitConfiguration.SHOULD_RUN_PROPERTY_KEY,
		        Boolean.getBoolean(PhpUnitConfiguration.DEFAULT_SHOULD_RUN))).thenReturn(true);
		when(project.getConfiguration()).thenReturn(configuration);
		MavenProject mavenProject = mock(MavenProject.class);
		ProjectFileSystem fs = mock(ProjectFileSystem.class);
		when(project.getPom()).thenReturn(mavenProject);
		when(project.getFileSystem()).thenReturn(fs);
		when(fs.getSourceDirs()).thenReturn(Arrays.asList(new File("C:\\projets\\PHP\\Monkey\\sources\\main")));
		when(fs.getTestDirs()).thenReturn(Arrays.asList(new File("C:\\projets\\PHP\\Monkey\\sources\\test")));
	}
}
