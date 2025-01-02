/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource SA
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
package org.sonar.php.cache;

import java.io.File;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.php.ParsingTestUtils;
import org.sonar.php.compat.PhpFileImpl;
import org.sonar.php.metrics.CpdVisitor;
import org.sonar.php.tree.symbols.SymbolTableImpl;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;

import static org.assertj.core.api.Assertions.assertThat;

class CpdSerializerTest {

  static final String PLUGIN_VERSION = "1.2.3";

  @Test
  void shouldSerializeAndDeserializeExampleData() {
    CpdVisitor.CpdToken cpd1 = new CpdVisitor.CpdToken(3, 0, 3, 3, "try");
    CpdVisitor.CpdToken cpd2 = new CpdVisitor.CpdToken(3, 4, 3, 5, "{");
    CpdVisitor.CpdToken cpd3 = new CpdVisitor.CpdToken(4, 2, 4, 5, "foo");
    CpdVisitor.CpdToken cpd4 = new CpdVisitor.CpdToken(4, 5, 4, 6, "(");
    CpdVisitor.CpdToken cpd5 = new CpdVisitor.CpdToken(4, 6, 4, 7, ")");
    CpdVisitor.CpdToken cpd6 = new CpdVisitor.CpdToken(4, 7, 4, 8, ";");
    List<CpdVisitor.CpdToken> cpdTokens = List.of(cpd1, cpd2, cpd3, cpd4, cpd5, cpd6);

    SerializationResult binary = CpdSerializer.toBinary(new CpdSerializationInput(cpdTokens, PLUGIN_VERSION));
    List<CpdVisitor.CpdToken> actual = CpdDeserializer.fromBinary(new CpdDeserializationInput(binary.data(), binary.stringTable(), PLUGIN_VERSION));

    assertThat(actual)
      .usingRecursiveFieldByFieldElementComparator()
      .isEqualTo(cpdTokens);
  }

  @Test
  void shouldReturnsNullWhenProjectSymbolDataCorrupted() {
    List<CpdVisitor.CpdToken> cpdTokens = List.of(new CpdVisitor.CpdToken(3, 0, 3, 3, "try"));

    SerializationResult binary = CpdSerializer.toBinary(new CpdSerializationInput(cpdTokens, PLUGIN_VERSION));
    List<CpdVisitor.CpdToken> actual = CpdDeserializer.fromBinary(
      new CpdDeserializationInput(
        corruptBit(binary.data()),
        binary.stringTable(),
        PLUGIN_VERSION));

    assertThat(actual).isNull();
  }

  @Test
  void shouldReturnsNullWhenStringTableDataCorrupted() {
    List<CpdVisitor.CpdToken> cpdTokens = List.of(new CpdVisitor.CpdToken(3, 0, 3, 3, "try"));

    SerializationResult binary = CpdSerializer.toBinary(new CpdSerializationInput(cpdTokens, PLUGIN_VERSION));
    List<CpdVisitor.CpdToken> actual = CpdDeserializer.fromBinary(
      new CpdDeserializationInput(
        binary.data(),
        corruptBit(binary.stringTable()),
        PLUGIN_VERSION));

    assertThat(actual).isNull();
  }

  @Test
  void shouldReturnNullWhenWrongPluginVersion() {
    List<CpdVisitor.CpdToken> cpdTokens = List.of(new CpdVisitor.CpdToken(3, 0, 3, 3, "try"));

    SerializationResult binary = CpdSerializer.toBinary(new CpdSerializationInput(cpdTokens, PLUGIN_VERSION));
    List<CpdVisitor.CpdToken> actual = CpdDeserializer.fromBinary(new CpdDeserializationInput(binary.data(), binary.stringTable(), "5.5.5"));

    assertThat(actual).isNull();
  }

  @Test
  void shouldSerializeAndDeserializeData() {
    for (File file : FileUtils.listFiles(new File("src/test/resources"), new String[] {"php"}, true)) {
      CompilationUnitTree unitTree = ParsingTestUtils.parse(file);
      CpdVisitor cpdVisitor = new CpdVisitor();
      SymbolTableImpl symbolTable = SymbolTableImpl.create(unitTree);
      InputFile inputFile = TestInputFileBuilder.create("", file.getPath()).build();
      List<CpdVisitor.CpdToken> cpdTokens = cpdVisitor.computeCpdTokens(PhpFileImpl.create(inputFile), unitTree, symbolTable, null);

      SerializationResult binary = CpdSerializer.toBinary(new CpdSerializationInput(cpdTokens, PLUGIN_VERSION));
      List<CpdVisitor.CpdToken> actual = CpdDeserializer.fromBinary(new CpdDeserializationInput(binary.data(), binary.stringTable(), PLUGIN_VERSION));

      assertThat(actual)
        .usingRecursiveFieldByFieldElementComparator()
        .isEqualTo(cpdTokens);
    }
  }

  private byte[] corruptBit(byte[] input) {
    input[input.length - 1] = (byte) (input[input.length - 1] << 1);
    return input;
  }
}
