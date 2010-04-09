package org.sonar.plugins.php.core.resources;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Arrays;

import org.junit.Test;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.ProjectFileSystem;
import org.sonar.api.resources.Resource;
import org.sonar.plugins.php.core.Php;


/**
 * The Class PhpFileTest.
 */
public class PhpFileTest {

	/** The project. */
	private Project project;

	/**
	 * Constructor with null key should only set qualifier and scope.
	 */
	@Test(expected=IllegalArgumentException.class)
	public void constructorWithNullKeyShouldOnlySetQualifierAndScope() {
		new PhpFile(null);
//		PhpFile phpFile = new PhpFile(null);
//		assertEquals(null, phpFile.getLanguage());
//		assertEquals(null, phpFile.getKey());
//		assertEquals(null, phpFile.getName());
//		assertEquals(Resource.QUALIFIER_FILE, phpFile.getScope());
//		assertEquals(Resource.QUALIFIER_CLASS, phpFile.getQualifier());
	}

	/**
	 * From absolute path in wrong path should return null.
	 */
	@Test
	public void fromAbsolutePathInWrongPathShouldReturnNull() {
		init();
		assertEquals(null, PhpFile.fromAbsolutePath("C:\\projets\\PHP\\Monkey\\src\\lib\\animal\\Monkey.php", project));
	}

	/**
	 * From absolute path should initialize package and class name.
	 */
	@Test
	public void fromAbsolutePathShouldInitializePackageAndClassName() {
		init();
		PhpFile phpFile = PhpFile.fromAbsolutePath("C:\\projets\\PHP\\Monkey\\src\\main\\animal\\Monkey.php", project);
		assertEquals("animal.Monkey", phpFile.getKey());
		phpFile = PhpFile.fromAbsolutePath("C:\\projets\\PHP\\Monkey\\src\\main\\insult\\Monkey.php", project);
		assertEquals("insult.Monkey", phpFile.getKey());
		phpFile = PhpFile.fromAbsolutePath("C:\\projets\\PHP\\Monkey\\src\\main\\Monkey.php", project);
		assertEquals("Monkey", phpFile.getKey());
	}

	/**
	 * From absolute path should recognize and initialize source file.
	 */
	@Test
	public void fromAbsolutePathShouldRecognizeAndInitializeSourceFile() {
		init();
		PhpFile phpFile = PhpFile.fromAbsolutePath("C:\\projets\\PHP\\Monkey\\src\\main\\animal\\Monkey.php", project);
		assertEquals(Php.INSTANCE, phpFile.getLanguage());
		assertEquals("animal.Monkey", phpFile.getKey());
		assertEquals("Monkey", phpFile.getName());
		assertEquals(Resource.QUALIFIER_FILE, phpFile.getScope());
		assertEquals(Resource.QUALIFIER_CLASS, phpFile.getQualifier());
	}

	/**
	 * From absolute path should recognize and initialize test file.
	 */
	@Test
	public void fromAbsolutePathShouldRecognizeAndInitializeTestFile() {
		init();
		PhpFile phpFile = PhpFile.fromAbsolutePath("C:\\projets\\PHP\\Monkey\\src\\test\\animal\\Monkey.php", project);
		assertEquals(Php.INSTANCE, phpFile.getLanguage());
		assertEquals("animal.Monkey", phpFile.getKey());
		assertEquals("Monkey", phpFile.getName());
		assertEquals(Resource.QUALIFIER_FILE, phpFile.getScope());
		assertEquals(Resource.QUALIFIER_UNIT_TEST_CLASS, phpFile.getQualifier());
	}

	/**
	 * From absolute path with null key should return null.
	 */
	@Test
	public void fromAbsolutePathWithNullKeyShouldReturnNull() {
		assertEquals(null, PhpFile.fromAbsolutePath(null, null));
	}

	/**
	 * From absolute path with wrong extension should return null.
	 */
	@Test
	public void fromAbsolutePathWithWrongExtensionShouldReturnNull() {
		init();
		assertEquals(null, PhpFile.fromAbsolutePath("C:\\projets\\PHP\\Monkey\\src\\main\\animal\\Monkey.java", project));
	}

	/**
	 * Inits the.
	 */
	private void init() {
		project = mock(Project.class);
		ProjectFileSystem fileSystem = mock(ProjectFileSystem.class);
		when(project.getFileSystem()).thenReturn(fileSystem);
		when(fileSystem.getSourceDirs()).thenReturn(Arrays.asList(new File("C:\\projets\\PHP\\Monkey\\src\\main")));
		when(fileSystem.getTestDirs()).thenReturn(Arrays.asList(new File("C:\\projets\\PHP\\Monkey\\src\\test")));
	}

}
