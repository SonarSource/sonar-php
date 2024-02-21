/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2024 SonarSource SA
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
package org.sonar.php.cache;

import java.io.Closeable;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import javax.annotation.Nullable;

/**
 * Use variable length encoding for integers.
 * https://en.wikipedia.org/wiki/Variable-length_code
 */
public class VarLengthOutputStream implements Closeable {
  private final DataOutputStream out;

  public VarLengthOutputStream(OutputStream out) {
    this.out = new DataOutputStream(out);
  }

  public void writeInt(int value) throws IOException {
    do {
      // encode the next 7 bits + "next byte" bit
      int bits = value & 0x7F;
      // remove 7 bits from value
      value >>>= 7;
      // add "next byte" bit if another byte is required
      byte b = (byte) (bits + ((value != 0) ? 0x80 : 0));
      out.write(b);
    } while (value != 0);
  }

  public void writeUTF(@Nullable String string) throws IOException {
    byte[] bytes;
    if (string == null) {
      bytes = "".getBytes(StandardCharsets.UTF_8);
    } else {
      bytes = string.getBytes(StandardCharsets.UTF_8);
    }
    writeInt(bytes.length);
    out.write(bytes);
  }

  public void writeBoolean(boolean bool) throws IOException {
    out.write(bool ? 1 : 0);
  }

  public void write(byte[] b) throws IOException {
    out.write(b);
  }

  @Override
  public void close() throws IOException {
    out.close();
  }
}
