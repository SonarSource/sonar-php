/*
 * Sonar, open source software quality management tool. Copyright (C) 2009 SonarSource SA mailto:contact AT sonarsource DOT com
 * Sonar is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version. Sonar is
 * distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details. You should have received a copy of
 * the GNU Lesser General Public License along with Sonar; if not, write to the Free Software Foundation, Inc., 51 Franklin Street,
 * Fifth Floor, Boston, MA 02
 */
package org.sonar.plugins.php.phpunit.sensor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sonar.api.utils.ParsingUtils;
import org.sonar.plugins.php.phpunit.xml.TestCase;
import org.sonar.plugins.php.phpunit.xml.TestSuite;


/**
 * The PhpTestSuiteParser .
 */
public class PhpTestSuiteReader {

	/** The reports per class. */
	private Map<String, PhpUnitTestReport> reportsPerClass = new HashMap<String, PhpUnitTestReport>();

	/**
	 * Cumulates test case details.
	 * 
	 * @param testCase the test case to analyse
	 * @param report the report in which results will be added
	 */
	private void cumulateTestCaseDetails(TestCase testCase, PhpUnitTestReport report) {

		if (TestCase.STATUS_SKIPPED.equals(testCase.getStatus())) {
			report.setSkipped(report.getSkipped() + 1);
		} else if (TestCase.STATUS_FAILURE.equals(testCase.getStatus())) {
			report.setFailures(report.getFailures() + 1);
		} else if (TestCase.STATUS_ERROR.equals(testCase.getStatus())) {
			report.setErrors(report.getErrors() + 1);
		}
		report.setTests(report.getTests() + 1);
		if (!Double.isNaN(testCase.getTime())) {
			Double scaled = ParsingUtils.scaleValue(testCase.getTime() * 1000, 3);
			report.setTime(report.getTime() + scaled.intValue());
		}
		report.getDetails().add(testCase);
	}

	/**
	 * Reads the given test suite.
	 * 
	 * @param testSuite the test suite
	 * @return List<PhpUnitTestReport> A list containing on report per php class
	 */
	public List<PhpUnitTestReport> readSuite(TestSuite testSuite) {
		if (testSuite.getTestSuites() != null) {
			List<PhpUnitTestReport> result = new ArrayList<PhpUnitTestReport>();
			for (TestSuite childSuite : testSuite.getTestSuites()) {
				result.addAll(readSuite(childSuite));
			}
			return result;
		}
		// For all cases
		if (testSuite.getTestCases() != null) {
		for (TestCase testCase : testSuite.getTestCases()) {
			String testClassName = testCase.getClassName();
			PhpUnitTestReport report = reportsPerClass.get(testClassName);
			// If no reports exists for this class we create one
			if (report == null) {
				report = new PhpUnitTestReport();
				report.setDetails(new ArrayList<TestCase>());
				report.setClassKey(testClassName);
				report.setFile(testCase.getFile());
				// and add it to the map
				reportsPerClass.put(testClassName, report);
			}
			cumulateTestCaseDetails(testCase, report);
			}
		}
		return new ArrayList<PhpUnitTestReport>(reportsPerClass.values());
	}

}
