package org.sonar.plugins.php.pmd.sensor;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Arrays;

import org.apache.commons.configuration.Configuration;
import org.apache.maven.project.MavenProject;
import org.junit.Test;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.resources.Java;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.ProjectFileSystem;
import org.sonar.api.rules.Violation;
import org.sonar.plugins.php.core.Php;
import org.sonar.plugins.php.pmd.configuration.PhpPmdConfiguration;

/**
 * The Class PhpPmdSensorTest.
 */
public class PhpPmdSensorTest {

	/**
	 * Sould not launch on non php project.
	 */
	@Test
	public void shouldNotLaunchOnNonPhpProject() {
		Project project = mock(Project.class);
		when(project.getLanguage()).thenReturn(Java.INSTANCE);
		PhpPmdSensor sensor = new PhpPmdSensor();
		assertEquals(false, sensor.shouldExecuteOnProject(project));
	}

	/**
	 * Sould launch on php project.
	 */
	@Test
	public void shouldLaunchOnPhpProject() {
		Project project = mock(Project.class);
		when(project.getLanguage()).thenReturn(Php.INSTANCE);
		PhpPmdSensor sensor = new PhpPmdSensor();
		assertEquals(true, sensor.shouldExecuteOnProject(project));
	}

	/**
	 * Sould not launch parsing when no report can be found.
	 */
	@Test
	public void shouldNotLaunchParsingWhenNoReportCanBeFound() {
		Project project = mock(Project.class);
		Configuration configuration = mock(Configuration.class);
		MavenProject mavenProject = mock(MavenProject.class);
		ProjectFileSystem fs = mock(ProjectFileSystem.class);
		when(project.getPom()).thenReturn(mavenProject);
		when(project.getFileSystem()).thenReturn(fs);
		when(fs.getSourceDirs()).thenReturn(Arrays.asList(new File("C:\\projets\\PHP\\Monkey\\sources\\main")));
		when(fs.getTestDirs()).thenReturn(Arrays.asList(new File("C:\\projets\\PHP\\Monkey\\Sources\\test")));
		when(fs.getBuildDir()).thenReturn(new File("C:\\projets\\PHP\\Monkey\\target"));
		when(
				configuration.getString(PhpPmdConfiguration.REPORT_FILE_NAME_PROPERTY_KEY,
						PhpPmdConfiguration.DEFAULT_REPORT_FILE_NAME)).thenReturn("tot.xml");
		when(
				configuration.getString(PhpPmdConfiguration.REPORT_FILE_RELATIVE_PATH_PROPERTY_KEY,
						PhpPmdConfiguration.DEFAULT_REPORT_FILE_PATH)).thenReturn(PhpPmdConfiguration.DEFAULT_REPORT_FILE_PATH);
		when(configuration.getBoolean(PhpPmdConfiguration.ANALYZE_ONLY_KEY, false)).thenReturn(true);
		when(project.getConfiguration()).thenReturn(configuration);
		when(project.getLanguage()).thenReturn(Php.INSTANCE);
		PhpPmdSensor sensor = new PhpPmdSensor();
		SensorContext context = mock(SensorContext.class);
		sensor.analyse(project, context);
		verify(context, never()).saveViolation(any(Violation.class));
	}

}
