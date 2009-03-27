/*
 * Sonar, open source software quality management tool.
 * Copyright (C) 2009 SonarSource SA
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

import org.junit.Test;

import java.io.File;
import java.util.Arrays;

public class CpdExecutorTest {

  @Test
  public void shoulExecuteCpd() throws Exception {
    File file = new File(getClass().getResource("/org/sonar/plugins/php/cpd/CpdExecutorTest/sample.php").toURI());
    File file2 = new File(getClass().getResource("/org/sonar/plugins/php/cpd/CpdExecutorTest/sample2.php").toURI());

    CpdExecutor executor = new CpdExecutor(Arrays.asList(file, file2));
    executor.execute();
  }

}
