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

package org.sonar.plugins.php.phpdepend;

import org.apache.commons.io.IOUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class PhpDependExecutor {

  private PhpDependConfiguration configuration;

  public PhpDependExecutor(PhpDependConfiguration configuration) {
    this.configuration = configuration;
  }

  public void execute() {
   /* String commandLine;

    try {
      String[] cmd = {configuration.getCommandLine(), configuration.getReportFilecommandOption(), configuration.getSuffixesCommandOption(),
        configuration.getSourceDir().getAbsolutePath()};
      commandLine = StringUtils.join(cmd, " ");
      ProcessBuilder builder = new ProcessBuilder(cmd);
      builder.redirectErrorStream(true);

      LOG.info("Execute PHP Depend with command '{}'", commandLine);
      Process p = builder.start();
      new StreamGobbler(p.getInputStream()).start();
      int returnCde = p.waitFor();
      if (returnCde != 0) {
        throw new PhpDependExecutionException("Status=" + returnCde + ", command=" + commandLine);
      }
    } catch (PhpDependExecutionException e) {
      throw e;

    } catch (Exception e) {
      throw new PhpDependExecutionException(e);
    }     */
  }

  class StreamGobbler extends Thread {
    InputStream is;

    StreamGobbler(InputStream is) {
      this.is = is;
    }

    public void run() {
      InputStreamReader isr = new InputStreamReader(is);
      BufferedReader br = new BufferedReader(isr);
      try {

        while (br.readLine() != null) {
          // nothing
        }
      } catch (IOException ioe) {
        ioe.printStackTrace();

      } finally {
        IOUtils.closeQuietly(br);
        IOUtils.closeQuietly(isr);
      }
    }
  }
}
