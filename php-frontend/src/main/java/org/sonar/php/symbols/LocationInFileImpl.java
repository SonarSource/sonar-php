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
package org.sonar.php.symbols;

import java.util.Objects;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.sonar.plugins.php.api.visitors.LocationInFile;

public class LocationInFileImpl implements LocationInFile {

  @Nullable
  private final String filePath;
  private final int startLine;
  private final int startLineOffset;
  private final int endLine;
  private final int endLineOffset;

  public LocationInFileImpl(@Nullable String filePath, int startLine, int startLineOffset, int endLine, int endLineOffset) {
    this.filePath = filePath;
    this.startLine = startLine;
    this.startLineOffset = startLineOffset;
    this.endLine = endLine;
    this.endLineOffset = endLineOffset;
  }

  @Override
  @CheckForNull
  public String filePath() {
    return filePath;
  }

  @Override
  public int startLine() {
    return startLine;
  }

  @Override
  public int startLineOffset() {
    return startLineOffset;
  }

  @Override
  public int endLine() {
    return endLine;
  }

  @Override
  public int endLineOffset() {
    return endLineOffset;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    LocationInFileImpl that = (LocationInFileImpl) o;
    return startLine == that.startLine &&
      startLineOffset == that.startLineOffset &&
      endLine == that.endLine &&
      endLineOffset == that.endLineOffset &&
      Objects.equals(filePath, that.filePath);
  }

  @Override
  public int hashCode() {
    return Objects.hash(filePath, startLine, startLineOffset, endLine, endLineOffset);
  }

  @Override
  public String toString() {
    return "LocationInFileImpl{" +
      filePath +
      ", " + startLine +
      ", " + startLineOffset +
      ", " + endLine +
      ", " + endLineOffset +
      '}';
  }
}
