/*
 * Sonar PHP Plugin
 * Copyright (C) 2010 Codehaus Sonar Plugins
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

/**
 * The Class PhpPluginExecutionException.
 */
public class PhpPluginExecutionException extends RuntimeException {

  /**
   * 
   */
  private static final long serialVersionUID = 1017202811537234016L;

  /**
   * Instantiates a new php plugin execution exception.
   */
  public PhpPluginExecutionException() {
    super();
  }

  /**
   * Instantiates a new php plugin execution exception.
   * 
   * @param message
   *          the message
   */
  public PhpPluginExecutionException(String message) {
    super(message);
  }

  /**
   * Instantiates a new php plugin execution exception.
   * 
   * @param message
   *          the message
   * @param cause
   *          the cause
   */
  public PhpPluginExecutionException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Instantiates a new php plugin execution exception.
   * 
   * @param cause
   *          the cause
   */
  public PhpPluginExecutionException(Throwable cause) {
    super(cause);
  }

}
