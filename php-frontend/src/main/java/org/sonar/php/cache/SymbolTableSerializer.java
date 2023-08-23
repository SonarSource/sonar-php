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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nullable;
import org.sonar.php.symbols.ClassSymbolData;
import org.sonar.php.symbols.FunctionSymbolData;
import org.sonar.php.symbols.MethodSymbolData;
import org.sonar.php.symbols.Parameter;
import org.sonar.php.tree.symbols.SymbolQualifiedName;
import org.sonar.php.tree.symbols.SymbolTableImpl;
import org.sonar.plugins.php.api.symbols.QualifiedName;
import org.sonar.plugins.php.api.visitors.LocationInFile;

public class SymbolTableSerializer {

  private final ByteArrayOutputStream stream;
  private final VarLengthOutputStream out;
  private final StringTable stringTable;

  private SymbolTableSerializer() {
    stream = new ByteArrayOutputStream();
    out = new VarLengthOutputStream(stream);
    stringTable = new StringTable();
  }

  public static SerializationResult toBinary(SymbolTableSerializationInput serializationInput) {
    SymbolTableSerializer serializer = new SymbolTableSerializer();
    return serializer.convert(serializationInput);
  }

  private SerializationResult convert(SymbolTableSerializationInput symbolTableSerializationInput) {
    try (out; stream) {
      String pluginVersion = symbolTableSerializationInput.pluginVersion();
      writeText(pluginVersion);
      SymbolTableImpl projectSymbolData = symbolTableSerializationInput.symbolTable();
      Collection<ClassSymbolData> classSymbols = projectSymbolData.classSymbolDatas();
      writeInt(classSymbols.size());
      for (ClassSymbolData classSymbol : classSymbols) {
        write(classSymbol);
      }
      Collection<FunctionSymbolData> nameFuncSymbolData = projectSymbolData.functionSymbolDatas();
      writeInt(nameFuncSymbolData.size());
      for (FunctionSymbolData value : nameFuncSymbolData) {
        write(value);
      }
      out.writeUTF("END");

      return new SerializationResult(stream.toByteArray(), writeStringTable());
    } catch (IOException e) {
      throw new IllegalStateException("Can't store data in cache", e);
    }
  }

  private void write(QualifiedName qualifiedName) throws IOException {
    if (!(qualifiedName instanceof SymbolQualifiedName)) {
      throw new IllegalStateException("The QualifiedName of type " + qualifiedName.getClass().getSimpleName() + " is not supported");
    }
    writeText(qualifiedName.toString());
  }

  private void write(ClassSymbolData classSymbolData) throws IOException {
    LocationInFile location = classSymbolData.location();
    write(location);
    write(classSymbolData.qualifiedName());
    writeText(classSymbolData.superClass().map(Object::toString).orElse(""));
    writeInt(classSymbolData.implementedInterfaces().size());
    for (QualifiedName implementedInterface : classSymbolData.implementedInterfaces()) {
      write(implementedInterface);
    }
    writeText(classSymbolData.kind().name());
    writeInt(classSymbolData.methods().size());
    for (MethodSymbolData method : classSymbolData.methods()) {
      write(method);
    }
  }

  private void write(MethodSymbolData method) throws IOException {
    writeText(method.visibility().toString());
    writeText(method.name());
    writeBoolean(method.isAbstract());
    writeBoolean(method.isTestMethod());
    write(method.location());
    writeParameters(method.parameters());
    writeBoolean(method.properties().hasReturn());
    writeBoolean(method.properties().hasFuncGetArgs());
  }

  private void writeParameters(List<Parameter> parameters) throws IOException {
    writeInt(parameters.size());
    for (Parameter parameter : parameters) {
      writeText(parameter.name());
      writeText(parameter.type());
      writeBoolean(parameter.hasDefault());
      writeBoolean(parameter.hasEllipsisOperator());
    }
  }

  private void write(LocationInFile location) throws IOException {
    String filePath = location.filePath();
    if ("[unknown file]".equals(filePath)) {
      writeText(filePath);
    } else {
      writeText(filePath);
      writeInt(location.startLine());
      writeInt(location.startLineOffset());
      writeInt(location.endLine());
      writeInt(location.endLineOffset());
    }
  }

  private void write(FunctionSymbolData data) throws IOException {
    if (data instanceof MethodSymbolData) {
      throw new IllegalStateException("The FunctionSymbolData of type " + data.getClass().getName() + " is not supported");
    }
    write(data.location());
    write(data.qualifiedName());
    writeParameters(data.parameters());
    write(data.properties());
  }

  private void write(FunctionSymbolData.FunctionSymbolProperties properties) throws IOException {
    writeBoolean(properties.hasReturn());
    writeBoolean(properties.hasFuncGetArgs());
  }

  private void writeText(@Nullable String text) throws IOException {
    out.writeInt(stringTable.getIndex(text));
  }

  private void writeInt(int number) throws IOException {
    out.writeInt(number);
  }

  private void writeBoolean(boolean bool) throws IOException {
    out.writeBoolean(bool);
  }

  private byte[] writeStringTable() throws IOException {
    ByteArrayOutputStream stringTableStream = new ByteArrayOutputStream();
    VarLengthOutputStream output = new VarLengthOutputStream(stringTableStream);
    List<String> byIndex = stringTable.getStringList();
    output.writeInt(byIndex.size());
    for (String string : byIndex) {
      output.writeUTF(string);
    }

    output.writeUTF("END");
    return stringTableStream.toByteArray();
  }
}
