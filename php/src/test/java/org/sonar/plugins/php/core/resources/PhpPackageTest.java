package org.sonar.plugins.php.core.resources;

import static org.junit.Assert.assertTrue;

import org.junit.Test;


/**
 * The Class PhpPackageTest.
 */
public class PhpPackageTest {

	/**
	 * Should be considered default package.
	 */
	@Test
	public void shouldBeConsideredDefaultPackage() {
		PhpPackage phpPackage = new PhpPackage("    ");
		assertTrue(phpPackage.isDefault());
		phpPackage = new PhpPackage("tree\\Monkey.php");
		assertTrue(!phpPackage.isDefault());
	}

	/**
	 * Should match pattern.
	 */
	@Test
	public void shouldMatchPattern() {
		PhpPackage phpPackage = new PhpPackage("    earth.tree");
		assertTrue(phpPackage.matchFilePattern("earth.tree.Monkey"));
	}

	/**
	 * Should not match pattern.
	 */
	@Test
	public void shouldNotMatchPattern() {
		PhpPackage phpPackage = new PhpPackage("earth.tree");
		assertTrue(!phpPackage.matchFilePattern("tree"));
	}
}
