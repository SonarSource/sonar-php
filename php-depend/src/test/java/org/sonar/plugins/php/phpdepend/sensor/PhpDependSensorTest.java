package org.sonar.plugins.php.phpdepend.sensor;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.commons.configuration.Configuration;
import org.junit.Test;
import org.sonar.api.resources.Java;
import org.sonar.api.resources.Project;
import org.sonar.plugins.php.core.Php;
import org.sonar.plugins.php.phpdepend.configuration.PhpDependConfiguration;

public class PhpDependSensorTest {

	@Test
	public void shouldNotLaunchOnNonPhpProject() {
		Project project = mock(Project.class);
		when(project.getLanguage()).thenReturn(Java.INSTANCE);
		Configuration configuration = mock(Configuration.class);
		PhpDependSensor sensor = new PhpDependSensor();
		when(
				configuration.getString(PhpDependConfiguration.REPORT_FILE_NAME_PROPERTY_KEY,
						PhpDependConfiguration.DEFAULT_REPORT_FILE_NAME)).thenReturn("pdepend.xml");
		when(
				configuration.getString(PhpDependConfiguration.REPORT_FILE_RELATIVE_PATH_PROPERTY_KEY,
						PhpDependConfiguration.DEFAULT_REPORT_FILE_PATH)).thenReturn("reports");
		when(project.getConfiguration()).thenReturn(configuration);
		PhpDependConfiguration config = mock(PhpDependConfiguration.class);
		when(config.isShouldRun()).thenReturn(true);
		assertEquals(false, sensor.shouldExecuteOnProject(project));
		when(config.isShouldRun()).thenReturn(false);
		assertEquals(false, sensor.shouldExecuteOnProject(project));
	}

	@Test
	public void shouldLaunchOnPhpProjectIfConfiguredSo() {
		Project project = mock(Project.class);
		when(project.getLanguage()).thenReturn(Php.INSTANCE);
		Configuration configuration = mock(Configuration.class);
		PhpDependSensor sensor = new PhpDependSensor();
		when(
				configuration.getString(PhpDependConfiguration.REPORT_FILE_NAME_PROPERTY_KEY,
						PhpDependConfiguration.DEFAULT_REPORT_FILE_NAME)).thenReturn("pdepend.xml");
		when(
				configuration.getString(PhpDependConfiguration.REPORT_FILE_NAME_PROPERTY_KEY,
						PhpDependConfiguration.DEFAULT_REPORT_FILE_PATH)).thenReturn("reports");
		when(
				configuration.getBoolean(PhpDependConfiguration.SHOULD_RUN_PROPERTY_KEY, Boolean
						.parseBoolean(PhpDependConfiguration.DEFAULT_SHOULD_RUN))).thenReturn(Boolean.TRUE);
		when(project.getConfiguration()).thenReturn(configuration);
		assertEquals(true, sensor.shouldExecuteOnProject(project));
	}

	@Test
	public void shouldNotLaunchOnPhpProjectIfConfiguredSo() {
		Project project = mock(Project.class);
		when(project.getLanguage()).thenReturn(Php.INSTANCE);
		Configuration configuration = mock(Configuration.class);
		PhpDependSensor sensor = new PhpDependSensor();
		when(
				configuration.getString(PhpDependConfiguration.REPORT_FILE_NAME_PROPERTY_KEY,
						PhpDependConfiguration.DEFAULT_REPORT_FILE_NAME)).thenReturn("pdepend.xml");
		when(
				configuration.getString(PhpDependConfiguration.REPORT_FILE_NAME_PROPERTY_KEY,
						PhpDependConfiguration.DEFAULT_REPORT_FILE_PATH)).thenReturn("reports");
		when(configuration.getBoolean(PhpDependConfiguration.SHOULD_RUN_PROPERTY_KEY, Boolean.FALSE)).thenReturn(Boolean.FALSE);
		when(project.getConfiguration()).thenReturn(configuration);
		assertEquals(false, sensor.shouldExecuteOnProject(project));
	}
}
