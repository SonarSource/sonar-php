/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2024 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.plugins.php.api.visitors;

import java.io.File;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.sonar.plugins.php.api.cache.CacheContext;

public record PhpInputFileContext(PhpFile phpFile, @Nullable File workingDirectory, @Nullable CacheContext cacheContext) {

  @Override
  @CheckForNull
  public File workingDirectory() {
    return workingDirectory;
  }

  @Override
  @CheckForNull
  public CacheContext cacheContext() {
    return cacheContext;
  }
}
