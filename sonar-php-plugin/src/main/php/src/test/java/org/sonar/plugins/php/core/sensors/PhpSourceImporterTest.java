package org.sonar.plugins.php.core.sensors;

import org.apache.commons.configuration.Configuration;
import org.apache.maven.project.MavenProject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import static org.mockito.Mockito.*;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.resources.DefaultProjectFileSystem;
import org.sonar.api.resources.Project;
import org.sonar.plugins.php.core.Php;
import org.sonar.plugins.php.core.PhpPlugin;
import org.sonar.plugins.php.core.resources.PhpFile;

import java.io.File;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.List;

/**
 * Tests the basic functionality of the PhpSourceImporter.
 *
 * @author juergen_kellerer, 2010-10-21
 * @version 1.0
 */
public class PhpSourceImporterTest {

  static List<String> sourceNames = Arrays.asList("one.php", "two.php", "three.php");
  static List<String> testNames = Arrays.asList("oneTest.php", "twoTest.php", "threeTest.php");
  static String phpCode = "<?php ?>";

  @Rule
  public TemporaryFolder tempFolder = new TemporaryFolder();

  private Project project;
  private SensorContext context;

  File sources, tests, testsBelowSources;

  @Before
  public void init() throws Exception {
    tempFolder.create();
    sources = tempFolder.newFolder("sources");
    tests = tempFolder.newFolder("tests");
    testsBelowSources = tempFolder.newFolder("sources/tests");

    Configuration configuration = mock(Configuration.class);
    when(configuration.getStringArray(PhpPlugin.FILE_SUFFIXES_KEY)).thenReturn(null);
    new Php(configuration);

    context = mock(SensorContext.class);
    project = mock(Project.class);
    when(project.getPom()).thenReturn(new MavenProject());

    DefaultProjectFileSystem fileSystem = new DefaultProjectFileSystem(project);
    fileSystem.addSourceDir(sources);
    fileSystem.addTestDir(tests);
    fileSystem.addTestDir(testsBelowSources);

    when(project.getFileSystem()).thenReturn(fileSystem);
  }

  @Test
  public void testAnalyseAddsTestsAndSources() throws Exception {
    doTestAnalyseAddsTestsAndSources(sources, tests);
  }

  @Test
  public void testAnalyseAddsTestsAndSourcesWhenTestsAreBelowSources() throws Exception {
    doTestAnalyseAddsTestsAndSources(sources, testsBelowSources);
  }

  void doTestAnalyseAddsTestsAndSources(File sources, File tests) throws Exception {
    createFiles(sources, false);
    createFiles(tests, true);

    new PhpSourceImporter().analyse(project, context);
    for (String name : sourceNames) {
      PhpFile pf = PhpFile.fromIOFile(new File(sources, name), Arrays.asList(sources), false);
      verify(context).saveSource(pf, phpCode);
    }
    for (String name : testNames) {
      PhpFile pf = PhpFile.fromIOFile(new File(tests, name), Arrays.asList(tests), true);
      verify(context).saveSource(pf, phpCode);
    }
    verifyNoMoreInteractions(context);
  }

  void createFiles(File path, boolean isTest) throws Exception {
    for (String s : isTest ? testNames : sourceNames) {
      FileWriter fw = new FileWriter(new File(path, s));
      fw.write(phpCode);
      fw.close();
    }
  }
}
