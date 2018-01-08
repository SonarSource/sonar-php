/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2018 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.php.checks.phpini;

import com.google.common.collect.ImmutableList;
import java.io.File;
import org.junit.Test;
import org.sonar.php.ini.PhpIniCheck;

import static org.sonar.php.checks.phpini.PhpIniCheckTestUtils.check;

public class CgiForceRedirectCheckTest {

  private PhpIniCheck check = new CgiForceRedirectCheck();
  private File dir = new File("src/test/resources/checks/phpini");

  @Test
  public void line_issue() throws Exception {
    check(check, new File(dir, "cgi_force_redirect.ini"));
  }

  @Test
  public void empty_file() throws Exception {
    check(check, new File(dir, "empty.ini"), ImmutableList.of());
  }

}
