/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2023 SonarSource SA
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
package org.sonar.plugins.php.api.visitors;

import java.io.File;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.sonar.plugins.php.api.cache.CacheContext;

public final class PhpInputFileContext {

  private final PhpFile phpFile;

  @Nullable
  private final File workingDirectory;

  @Nullable
  private final CacheContext cacheContext;

  public PhpInputFileContext(PhpFile phpFile, @Nullable File workingDirectory, @Nullable CacheContext cacheContext) {
    this.phpFile = phpFile;
    this.workingDirectory = workingDirectory;
    this.cacheContext = cacheContext;
  }

  public PhpFile phpFile() {
    return phpFile;
  }

  @CheckForNull
  public File workingDirectory() {
    return workingDirectory;
  }

  @CheckForNull
  public CacheContext cacheContext() {
    return cacheContext;
  }
}
