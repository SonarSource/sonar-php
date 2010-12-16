/*
 * Sonar, open source software quality management tool.
 * Copyright (C) 2010 Akram Ben Aissi
 * mailto:contact AT sonarsource DOT com
 *
 * Sonar is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * Sonar is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Sonar; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */

package org.sonar.plugins.php.cpd;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.sonar.plugins.php.cpd.PhpCpdConfiguration.PHPCPD_DEFAULT_MINIMUM_NUMBER_OF_IDENTICAL_LINES;
import static org.sonar.plugins.php.cpd.PhpCpdConfiguration.PHPCPD_DEFAULT_MINIMUM_NUMBER_OF_IDENTICAL_TOKENS;
import static org.sonar.plugins.php.cpd.PhpCpdConfiguration.PHPCPD_MINIMUM_NUMBER_OF_IDENTICAL_LINES_MODIFIER;
import static org.sonar.plugins.php.cpd.PhpCpdConfiguration.PHPCPD_MINIMUM_NUMBER_OF_IDENTICAL_TOKENS_MODIFIER;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.junit.Ignore;
import org.junit.Test;
import org.sonar.plugins.php.core.PhpPlugin;

/**
 * @author akram
 * 
 */
public class PhpCpdExecutorTest {

	/**
	 * Test method for
	 * {@link org.sonar.plugins.php.codesniffer.PhpCpdExecutor#getPhpcpdCommandLine()}
	 * .
	 */
	@Test
	@Ignore
	public void testGetCommandLine() {

		Configuration configuration = mock(Configuration.class);
		when(configuration.getStringArray(PhpPlugin.FILE_SUFFIXES_KEY))
				.thenReturn(null);
		PhpCpdConfiguration c = mock(PhpCpdConfiguration.class);
		when(c.getMinimunNumberOfIdenticalLines()).thenReturn(
				PHPCPD_DEFAULT_MINIMUM_NUMBER_OF_IDENTICAL_LINES);
		when(c.getMinimunNumberOfIdenticalTokens()).thenReturn(
				PHPCPD_DEFAULT_MINIMUM_NUMBER_OF_IDENTICAL_TOKENS);

		String reportFile = "C:\\projets\\PHP\\Monkey\\target\\logs\\php-cpd.xml";
		when(c.getReportFile()).thenReturn(new File(reportFile));
		when(c.isOsWindows()).thenReturn(false);
		String phpcpdScriptName = "phpcpd";
		when(c.getOsDependentToolScriptName()).thenReturn(phpcpdScriptName);

		PhpCpdExecutor executor = new PhpCpdExecutor(c);
		List<String> commandLine = executor.getCommandLine();
		List<String> expected = new ArrayList<String>();
		expected.add(phpcpdScriptName);

		expected.add(PHPCPD_MINIMUM_NUMBER_OF_IDENTICAL_LINES_MODIFIER);
		expected.add(PHPCPD_DEFAULT_MINIMUM_NUMBER_OF_IDENTICAL_LINES
				.toString());

		expected.add(PHPCPD_MINIMUM_NUMBER_OF_IDENTICAL_TOKENS_MODIFIER);
		expected.add(PHPCPD_DEFAULT_MINIMUM_NUMBER_OF_IDENTICAL_TOKENS
				.toString());
		expected.add("--log-pmd");
		expected.add(new File(reportFile).getAbsolutePath());

		assertEquals(expected, commandLine);
	}
}
