package org.sonar.plugins.php.phpunit.sensor;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.List;

import org.apache.maven.project.MavenProject;
import org.junit.Test;
import org.sonar.api.resources.Project;
import org.sonar.plugins.php.phpunit.configuration.PhpUnitConfiguration;

public class PhpUnitExecutorTest {

	@Test
	public void shouldReturnCommandLineWithoutCoverageOptions() {
		PhpUnitConfiguration config = mock(PhpUnitConfiguration.class);
		Project project = mock(Project.class);
		MavenProject mProject = mock(MavenProject.class);
		when(project.getPom()).thenReturn(mProject);
		when(mProject.getBasedir()).thenReturn(new File("toto"));
		PhpUnitExecutor executor = new PhpUnitExecutor(config, project);
		when(config.shouldRunCoverage()).thenReturn(false);
		when(config.getCoverageReportFile()).thenReturn(new File("phpUnit.coverage.xml"));
		List<String> commandLine = executor.getCommandLine();
		assertTrue("Should not return any coverage options", !commandLine.contains("--coverage-clover=phpUnit.coverage.xml"));
	}

}
