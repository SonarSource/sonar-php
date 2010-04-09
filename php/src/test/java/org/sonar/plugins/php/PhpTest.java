package org.sonar.plugins.php;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.sonar.plugins.php.core.Php;

/**
 * The Class PhpTest.
 */
public class PhpTest {

	/**
	 * Should check valid php extensions.
	 */
	@Test
	public void shouldCheckValidPhpExtensions() {
		assertTrue(Php.hasValidSuffixes("goodExtension.php"));
		assertTrue(Php.hasValidSuffixes("goodExtension.php5"));
		assertTrue(Php.hasValidSuffixes("goodExtension.inc"));
		assertFalse(Php.hasValidSuffixes("wrong.extension"));
	}
}
