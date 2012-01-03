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
/**
 * This package is a duplication of {@link org.sonar.duplications.token} package in order to make sure 
 * that the future release of the PHP plugin will remain compatible will future releases of Sonar.
 * (as the {@link org.sonar.duplications.token} package may be reworked in Sonar 2.14).
 * 
 * This package will be removed in a future version, when the PHP plugin depends on Sonar 2.14+.
 */
package org.sonar.plugins.php.duplications.internal;

