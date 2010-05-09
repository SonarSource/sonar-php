/*
 * Sonar, open source software quality management tool.
 * Copyright (C) 2010 SQLi
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

package org.sonar.plugins.php.codesniffer.executor;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.mockito.Mockito;
import org.sonar.plugins.php.codesniffer.configuration.PhpCodesnifferConfiguration;

/**
 * @author akram
 * 
 */
public class PhpCodesnifferExecutorTest {

  /**
   * Test method for {@link org.sonar.plugins.php.codesniffer.executor.PhpCodesnifferExecutor#getCommandLine()}.
   */
  @Test
  public void testGetCommandLine() {
    PhpCodesnifferConfiguration c = mock(PhpCodesnifferConfiguration.class);
    PhpCodesnifferExecutor executor = new PhpCodesnifferExecutor(c);
    executor.getCommandLine();
    Mockito.verify(c);
  }

  /**
   * Test method for {@link org.sonar.plugins.php.codesniffer.executor.PhpCodesnifferExecutor#getExecutedTool()}.
   */
  @Test
  public void testGetExecutedTool() {
    PhpCodesnifferConfiguration c = mock(PhpCodesnifferConfiguration.class);
    PhpCodesnifferExecutor executor = new PhpCodesnifferExecutor(c);
    assertEquals("PHPCodeSniffer", executor.getExecutedTool());

  }

  /**
   * Test method for {@link org.sonar.plugins.php.core.executor.PhpPluginAbstractExecutor#execute()}.
   */
  @Test
  public void testExecute() {
    PhpCodesnifferConfiguration c = mock(PhpCodesnifferConfiguration.class);
    when(c.getOsDependentToolScriptName()).thenReturn("sqlics");
    PhpCodesnifferExecutor executor = new PhpCodesnifferExecutor(c);
    executor.execute();
    Mockito.verify(c);
  }

}
