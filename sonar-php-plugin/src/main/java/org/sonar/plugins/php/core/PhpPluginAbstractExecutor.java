/*
 * Sonar PHP Plugin
 * Copyright (C) 2010 Akram Ben Aissi
 * dev@sonar.codehaus.org
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
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */

package org.sonar.plugins.php.core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.BatchExtension;

/**
 * Abstract php plugin executor. This class handles common executor needs such as running the process, reading its common and error output
 * streams and logging. In nominal case implementing executor should just construct the desire command line.
 */

public abstract class PhpPluginAbstractExecutor implements BatchExtension {

  /**
   * The Class AsyncPipe.
   */
  private static class AsyncPipe extends Thread {

    /** The logger. */
    private static final Logger LOG = LoggerFactory.getLogger(AsyncPipe.class);

    /** The input stream. */
    private BufferedReader reader;

    /** The output stream. */
    private BufferedWriter writer;

    /**
     * Instantiates a new async pipe.
     * 
     * @param input
     *          an InputStream
     * @param output
     *          an OutputStream
     */
    public AsyncPipe(InputStream input, ByteArrayOutputStream output) {
      this.reader = new BufferedReader(new InputStreamReader(input));
      this.writer = new BufferedWriter(new OutputStreamWriter(output));
    }

    /**
     * @see java.lang.Thread#run()
     */
    @Override
    public void run() {
      try {
        // Reads the process input stream and writes it to the output stream
        String line = reader.readLine();
        while (line != null) {
          synchronized (writer) {
            writer.write(line);
            LOG.debug(line);
          }
          line = reader.readLine();
        }
      } catch (IOException e) {
        LOG.error("Can't execute the Async Pipe", e);
      }
    }
  }

  /** The logger */
  private static final Logger LOG = LoggerFactory.getLogger(PhpPluginAbstractExecutor.class);
  private static final int DEFAUT_BUFFER_INITIAL_SIZE = 1024;

  /**
   * Executes the external tool.
   */
  public void execute() {
    try {
      // Gets the tool command line
      List<String> commandLine = getCommandLine();
      ProcessBuilder builder = new ProcessBuilder(commandLine);
      LOG.info("Executing " + getExecutedTool() + " with command '{}'", prettyPrint(commandLine));
      // Starts the process
      Process p = builder.start();
      // And handles it's normal and error stream in separated threads.

      ByteArrayOutputStream output = new ByteArrayOutputStream(DEFAUT_BUFFER_INITIAL_SIZE);
      AsyncPipe outputStreamThread = new AsyncPipe(p.getInputStream(), output);
      outputStreamThread.start();

      ByteArrayOutputStream error = new ByteArrayOutputStream(DEFAUT_BUFFER_INITIAL_SIZE);
      AsyncPipe errorStreamThread = new AsyncPipe(p.getErrorStream(), error);
      errorStreamThread.start();

      LOG.info(getExecutedTool() + " ended with returned code '{}'.", p.waitFor());
    } catch (IOException e) {
      LOG.error("Can't execute the external tool", e);
      throw new PhpPluginExecutionException(e);
    } catch (InterruptedException e) {
      LOG.error("Async pipe interrupted: ", e);
      throw new PhpPluginExecutionException(e);
    }
  }

  /**
   * Returns a String where each list member is separated with a space
   * 
   * @param commandLine
   *          the external tool command line argument
   * @return String where each list member is separated with a space
   */
  private String prettyPrint(List<String> commandLine) {
    StringBuilder sb = new StringBuilder();
    for (Iterator<String> iter = commandLine.iterator(); iter.hasNext();) {
      String part = iter.next();
      sb.append(part);
      if (iter.hasNext()) {
        sb.append(" ");
      }
    }
    return sb.toString();
  }

  /**
   * Gets the command line.
   * 
   * @return the command line
   */
  protected abstract List<String> getCommandLine();

  /**
   * Gets the executed tool.
   * 
   * @return the executed tool
   */
  protected abstract String getExecutedTool();
}
