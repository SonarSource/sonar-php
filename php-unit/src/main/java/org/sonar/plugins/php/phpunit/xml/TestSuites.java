package org.sonar.plugins.php.phpunit.xml;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;


/**
 * The Class TestSuites.
 */
@XStreamAlias("testsuites")
public final class TestSuites {

	/** The test suites. */
	@XStreamImplicit(itemFieldName = "testsuite")
	private List<TestSuite> testSuites;

	/**
	 * Gets the test suites.
	 * 
	 * @return the test suites
	 */
	public List<TestSuite> getTestSuites() {
		return testSuites;
	}

	/**
	 * Sets the test suites.
	 * 
	 * @param testSuites the new test suites
	 */
	public void setTestSuites(final List<TestSuite> testSuites) {
		this.testSuites = testSuites;
	}

	/**
	 * Adds the test suite.
	 * 
	 * @param testSuite the test suite
	 */
	public void addTestSuite(final TestSuite testSuite) {
		if (testSuites == null) {
			testSuites = new ArrayList<TestSuite>();
		}
		testSuites.add(testSuite);
	}
}
