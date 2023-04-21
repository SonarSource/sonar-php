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
package org.sonar.plugins.php;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.sonar.api.batch.fs.InputFile;

public class FileHashingUtils {

  private FileHashingUtils() {
  }

  private static final String HASH_ALGORITHM = "MD5";

  public static byte[] inputFileContentHash(InputFile inputFile) throws IOException, NoSuchAlgorithmException {
    byte[] contentBytes = inputFile.contents().getBytes(StandardCharsets.UTF_8);
    MessageDigest messageDigest = MessageDigest.getInstance(HASH_ALGORITHM);
    return messageDigest.digest(contentBytes);
  }
}