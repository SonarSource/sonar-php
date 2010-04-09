package org.sonar.plugins.php.phpunit.sensor;

import java.util.List;

import org.sonar.plugins.php.phpunit.xml.TestCase;

/**
 * The Class PhpUnitTestReport.
 */
public class PhpUnitTestReport {

	/** The class key. */
	private String classKey;

	/** A list of all test cases. */
	private List<TestCase> details;

	/** The numbers of errors. */
	private int errors = 0;

	/** The numbers of failed tests. */
	private int failures = 0;

	/** The file. */
	private String file;

	/** The numbers of skipped. */
	private int skipped = 0;

	/** The numbers of tests. */
	private int tests = 0;

	/** The time. */
	private int time = 0;

	/**
	 * Gets the class key.
	 * 
	 * @return the class key
	 */
	public String getClassKey() {
		return classKey;
	}

	/**
	 * Gets the details.
	 * 
	 * @return the details
	 */
	public List<TestCase> getDetails() {
		return details;
	}

	/**
	 * Gets the number or errors.
	 * 
	 * @return the errors
	 */
	public int getErrors() {
		return errors;
	}

	/**
	 * Gets the number or failed tests.
	 * 
	 * @return the failures
	 */
	public int getFailures() {
		return failures;
	}

	/**
	 * Gets the file.
	 * 
	 * @return the file
	 */
	public String getFile() {
		return file;
	}

	/**
	 * Gets the numbers of skipped tests.
	 * 
	 * @return the skipped
	 */
	public int getSkipped() {
		return skipped;
	}

	/**
	 * Gets the numbers of tests.
	 * 
	 * @return the tests
	 */
	public int getTests() {
		return tests;
	}

	/**
	 * Gets the time.
	 * 
	 * @return the time
	 */
	public int getTime() {
		return time;
	}

	/**
	 * Checks if is valid.
	 * 
	 * @return true, if is valid
	 */
	public boolean isValid() {
		return classKey != null;
	}

	/**
	 * Sets the class key.
	 * 
	 * @param classKey the new class key
	 */
	public void setClassKey(String classKey) {
		this.classKey = classKey;
	}

	/**
	 * Sets the details.
	 * 
	 * @param details the new details
	 */
	public void setDetails(List<TestCase> details) {
		this.details = details;
	}

	/**
	 * Sets the numbers of errors.
	 * 
	 * @param errors the new errors
	 */
	public void setErrors(int errors) {
		this.errors = errors;
	}

	/**
	 * Sets the numbers of failures.
	 * 
	 * @param failures the new failures
	 */
	public void setFailures(int failures) {
		this.failures = failures;
	}

	/**
	 * Sets the file.
	 * 
	 * @param file the new file
	 */
	public void setFile(String file) {
		this.file = file;
	}

	/**
	 * Sets the numbers of skipped.
	 * 
	 * @param skipped the new skipped
	 */
	public void setSkipped(int skipped) {
		this.skipped = skipped;
	}

	/**
	 * Sets the numbers of tests.
	 * 
	 * @param tests the new tests
	 */
	public void setTests(int tests) {
		this.tests = tests;
	}

	/**
	 * Sets the time.
	 * 
	 * @param time the new time
	 */
	public void setTime(int time) {
		this.time = time;
	}

}
