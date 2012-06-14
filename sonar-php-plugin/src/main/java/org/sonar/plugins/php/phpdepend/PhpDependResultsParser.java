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
package org.sonar.plugins.php.phpdepend;

import org.sonar.api.BatchExtension;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.resources.Project;

import java.io.File;

/**
 * Php Depend Result Parser abstraction layer
 *
 * @since 1.1
 */
public abstract class PhpDependResultsParser implements BatchExtension {

  static final Number[] FUNCTIONS_DISTRIB_BOTTOM_LIMITS = {1, 2, 4, 6, 8, 10, 12};
  static final Number[] CLASSES_DISTRIB_BOTTOM_LIMITS = {0, 5, 10, 20, 30, 60, 90};

  /**
   * The context.
   */
  private SensorContext context;

  /**
   * The project.
   */
  private Project project;

  private double classComplexity;

  private int numberOfMethods;

  /**
   * Instantiates a new php depend results parser.
   *
   * @param project
   *          the project
   * @param context
   *          the context
   */
  public PhpDependResultsParser(Project project, SensorContext context) {
    this.project = project;
    this.context = context;
  }

  /**
   *
   * @return SensorContext
   */
  public SensorContext getContext() {
    return context;
  }

  /**
   *
   * @return Project
   */
  public Project getProject() {
    return project;
  }

  /**
   *
   * @return double
   */
  public double getClassComplexity() {
    return classComplexity;
  }

  /**
   *
   * @return int
   */
  public int getNumberOfMethods() {
    return numberOfMethods;
  }

  public void increaseClassComplexity() {
    classComplexity++;
  }

  public void increaseClassComplexity(double classComplexity) {
    this.classComplexity = this.classComplexity + classComplexity;
  }

  public void setClassComplexity(double classComplexity) {
    this.classComplexity = classComplexity;
  }

  public void increaseNumberOfMethods() {
    numberOfMethods++;
  }

  public void setNumberOfMethods(int numberOfMethods) {
    this.numberOfMethods = numberOfMethods;
  }

  /**
   * Parses given Xml report
   *
   * @param reportXml Php Depend Xml report
   */
  public abstract void parse(File reportXml);
}
