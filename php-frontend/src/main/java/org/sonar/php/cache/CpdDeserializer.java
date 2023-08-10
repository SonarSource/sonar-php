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
package org.sonar.php.cache;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.CheckForNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.php.metrics.CpdVisitor;

public class CpdDeserializer {

  private static final Logger LOG = LoggerFactory.getLogger(CpdDeserializer.class);

  private final VarLengthInputStream in;
  private final VarLengthInputStream stringTableIn;
  private final String pluginVersion;

  private StringTable stringTable;

  private CpdDeserializer(VarLengthInputStream in, VarLengthInputStream stringTableIn, String pluginVersion) {
    this.in = in;
    this.stringTableIn = stringTableIn;
    this.pluginVersion = pluginVersion;
  }

  @CheckForNull
  public static List<CpdVisitor.CpdToken> fromBinary(CpdDeserializationInput input) {
    CpdDeserializer deserializer = new CpdDeserializer(
      new VarLengthInputStream(input.cpdTokensBytes()),
      new VarLengthInputStream(input.stringTable()),
      input.pluginVersion());
    return deserializer.convert();
  }

  @CheckForNull
  private List<CpdVisitor.CpdToken> convert() {
    try (in; stringTableIn) {
      stringTable = readStringTable();
      String pluginVersionText = readString();
      if(!pluginVersionText.equals(pluginVersion)) {
        return null;
      }
      int sizeOfCpdTokens = readInt();
      List<CpdVisitor.CpdToken> cpdTokens = new ArrayList<>(sizeOfCpdTokens);

      for (int i = 0; i < sizeOfCpdTokens; i++) {
        readCpdToken(cpdTokens);
      }

      if (!"END".equals(in.readUTF())) {
        throw new IOException("Can't read data from cache, format corrupted");
      }
      return cpdTokens;
    } catch (IOException e) {
      LOG.debug("Can't deserialize data from the cache", e);
      return null;
    }
  }

  private void readCpdToken(List<CpdVisitor.CpdToken> cpdTokens) throws IOException {
    cpdTokens.add(new CpdVisitor.CpdToken(
      readInt(),
      readInt(),
      readInt(),
      readInt(),
      readString()
    ));
  }

  private int readInt() throws IOException {
    return in.readInt();
  }

  private String readString() throws IOException {
    return stringTable.getString(in.readInt());
  }

  private StringTable readStringTable() throws IOException {
    int size = stringTableIn.readInt();
    List<String> byIndex = new ArrayList<>(size);
    for (int i = 0; i < size; i++) {
      byIndex.add(stringTableIn.readUTF());
    }
    if (!"END".equals(stringTableIn.readUTF())) {
      throw new IOException("Can't read data from cache, format corrupted");
    }
    return new StringTable(byIndex);
  }
}
