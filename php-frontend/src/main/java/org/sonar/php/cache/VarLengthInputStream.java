/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.php.cache;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Use variable length encoding for integers.
 * https://en.wikipedia.org/wiki/Variable-length_code
 */
public class VarLengthInputStream implements Closeable {
  private final DataInputStream in;

  public VarLengthInputStream(byte[] input) {
    this.in = new DataInputStream(new ByteArrayInputStream(input));
  }

  public String readUTF() throws IOException {
    int length = readInt();
    byte[] bytes = new byte[length];
    in.readFully(bytes);
    return new String(bytes, StandardCharsets.UTF_8);
  }

  public int readInt() throws IOException {
    int result = 0;
    int shift = 0;
    int b;
    do {
      // get 7 bits from next byte and add them to correct position
      b = readByte();
      result |= (b & 0x7F) << shift;
      shift += 7;
    } while ((b & 0x80) != 0);
    return result;
  }

  public boolean readBoolean() throws IOException {
    return in.readBoolean();
  }

  public byte readByte() throws IOException {
    return in.readByte();
  }

  @Override
  public void close() throws IOException {
    in.close();
  }
}
