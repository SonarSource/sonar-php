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
import org.sonar.php.symbols.ClassSymbol;
import org.sonar.php.symbols.ClassSymbolData;
import org.sonar.php.symbols.FunctionSymbolData;
import org.sonar.php.symbols.LocationInFileImpl;
import org.sonar.php.symbols.MethodSymbolData;
import org.sonar.php.symbols.Parameter;
import org.sonar.php.symbols.UnknownLocationInFile;
import org.sonar.php.symbols.Visibility;
import org.sonar.php.tree.symbols.SymbolQualifiedName;
import org.sonar.php.tree.symbols.SymbolTableImpl;
import org.sonar.plugins.php.api.symbols.QualifiedName;
import org.sonar.plugins.php.api.visitors.LocationInFile;

public class SymbolTableDeserializer {

  private static final Logger LOG = LoggerFactory.getLogger(SymbolTableDeserializer.class);

  private final VarLengthInputStream in;
  private final VarLengthInputStream stringTableIn;
  private final String pluginVersion;

  private StringTable stringTable;

  private SymbolTableDeserializer(VarLengthInputStream in, VarLengthInputStream stringTableIn, String pluginVersion) {
    this.in = in;
    this.stringTableIn = stringTableIn;
    this.pluginVersion = pluginVersion;
  }

  @CheckForNull
  public static SymbolTableImpl fromBinary(SymbolTableDeserializationInput input) {
    SymbolTableDeserializer deserializer = new SymbolTableDeserializer(
      new VarLengthInputStream(input.projectSymbolDataBytes()),
      new VarLengthInputStream(input.stringTable()),
      input.pluginVersion());
    return deserializer.convert();
  }

  private SymbolTableImpl convert() {
    try (in; stringTableIn) {
      List<ClassSymbolData> classSymbolData = new ArrayList<>();
      List<FunctionSymbolData> functionSymbolData = new ArrayList<>();
      stringTable = readStringTable();
      String pluginVersionText = readString();
      if (!pluginVersionText.equals(pluginVersion)) {
        return null;
      }
      int sizeOfClassSymbols = readInt();
      for (int i = 0; i < sizeOfClassSymbols; i++) {
        ClassSymbolData data = readClassSymbolData();
        classSymbolData.add(data);
      }
      int sizeOfFuncSymbols = readInt();
      for (int i = 0; i < sizeOfFuncSymbols; i++) {
        FunctionSymbolData data = readFunctionSymbolDataList();
        functionSymbolData.add(data);
      }
      if (!"END".equals(in.readUTF())) {
        throw new IOException("Can't read data from cache, format corrupted");
      }
      return SymbolTableImpl.create(classSymbolData, functionSymbolData);
    } catch (IOException e) {
      LOG.debug("Can't deserialize data from the cache", e);
      return null;
    }
  }

  private FunctionSymbolData readFunctionSymbolDataList() throws IOException {
    LocationInFile location = readLocation();
    QualifiedName qualifiedName = readQualifiedName();
    List<Parameter> parameters = readParameters();
    FunctionSymbolData.FunctionSymbolProperties properties = readProperties();
    return new FunctionSymbolData(location, qualifiedName, parameters, properties);
  }

  private FunctionSymbolData.FunctionSymbolProperties readProperties() throws IOException {
    boolean hasReturn = readBoolean();
    boolean hasFuncGetArgs = readBoolean();
    return new FunctionSymbolData.FunctionSymbolProperties(hasReturn, hasFuncGetArgs);
  }

  private QualifiedName readQualifiedName() throws IOException {
    String name = readString();
    return SymbolQualifiedName.qualifiedName(name);
  }

  private QualifiedName readQualifiedNameOrNull() throws IOException {
    String name = readString();
    if (name.isBlank()) {
      return null;
    }
    return SymbolQualifiedName.qualifiedName(name);
  }

  private ClassSymbolData readClassSymbolData() throws IOException {
    LocationInFile location = readLocation();
    QualifiedName qualifiedName = readQualifiedName();
    QualifiedName superClass = readQualifiedNameOrNull();
    int sizeOfImplementedInterfaces = readInt();
    List<QualifiedName> implementedInterfaces = new ArrayList<>();
    for (int i = 0; i < sizeOfImplementedInterfaces; i++) {
      QualifiedName implInterface = readQualifiedName();
      implementedInterfaces.add(implInterface);
    }
    String kindText = readString();
    int numberOfMethods = readInt();
    List<MethodSymbolData> methods = new ArrayList<>();
    for (int i = 0; i < numberOfMethods; i++) {
      MethodSymbolData method = readMethod();
      methods.add(method);
    }
    return new ClassSymbolData(location, qualifiedName, superClass, implementedInterfaces, ClassSymbol.Kind.valueOf(kindText), methods);
  }

  private LocationInFile readLocation() throws IOException {
    String filePath = readString();
    if ("[unknown file]".equals(filePath)) {
      return UnknownLocationInFile.UNKNOWN_LOCATION;
    } else {
      int startLine = readInt();
      int startLineOffset = readInt();
      int endLine = readInt();
      int endLineOffset = readInt();
      return new LocationInFileImpl(filePath, startLine, startLineOffset, endLine, endLineOffset);
    }
  }

  private MethodSymbolData readMethod() throws IOException {
    Visibility visibility = Visibility.valueOf(readString());
    String name = readString();
    boolean isAbstract = readBoolean();
    LocationInFile location = readLocation();
    List<Parameter> parameters = readParameters();
    boolean hasReturn = readBoolean();
    boolean hasFuncGetArgs = readBoolean();
    FunctionSymbolData.FunctionSymbolProperties properties = new FunctionSymbolData.FunctionSymbolProperties(hasReturn, hasFuncGetArgs);
    return new MethodSymbolData(location, name, parameters, properties, visibility, isAbstract);
  }

  private List<Parameter> readParameters() throws IOException {
    int size = readInt();
    List<Parameter> parameters = new ArrayList<>();
    for (int i = 0; i < size; i++) {
      String name = readString();
      String type = readString();
      String typeOrNull = "".equals(type) ? null : type;
      boolean hasDefault = readBoolean();
      boolean hasEllipsisOperator = readBoolean();
      parameters.add(new Parameter(name, typeOrNull, hasDefault, hasEllipsisOperator));
    }
    return parameters;
  }

  private int readInt() throws IOException {
    return in.readInt();
  }

  private boolean readBoolean() throws IOException {
    return in.readBoolean();
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
