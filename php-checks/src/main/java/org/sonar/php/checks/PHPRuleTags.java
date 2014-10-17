/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010 SonarSource and Akram Ben Aissi
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
package org.sonar.php.checks;

public interface PHPRuleTags {

  String CONVENTION = "convention";
  String CWE = "cwe";
  String BRAIN_OVERLOAD = "brain-overload";
  String BUG = "bug";
  String PSR2 = "psr2";
  String PSR1 = "psr1";
  String SECURITY = "security";
  String CERT = "cert";
  String UNUSED = "unused";
  String PITFAIL = "pitfail";
  String MISRA_C = "misra-c";
  String MISRA_CPP = "misra-c++";
  String PERFORMANCE = "performance";
  String OBSOLETE = "obsolete";
}
