/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2022 SonarSource SA
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
package org.sonar.plugins.php.cache;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.sonar.DurationStatistics;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.php.cache.Cache;
import org.sonar.php.cache.CacheContext;
import org.sonar.php.cache.CacheContextImpl;
import org.sonar.php.cache.ProjectSymbolDataDeserializer;
import org.sonar.php.cache.ProjectSymbolDataSerializer;
import org.sonar.php.symbols.ClassSymbol;
import org.sonar.php.symbols.ClassSymbolData;
import org.sonar.php.symbols.FunctionSymbolData;
import org.sonar.php.symbols.LocationInFileImpl;
import org.sonar.php.symbols.MethodSymbolData;
import org.sonar.php.symbols.Parameter;
import org.sonar.php.symbols.ProjectSymbolData;
import org.sonar.php.symbols.Visibility;
import org.sonar.php.tree.symbols.SymbolQualifiedName;
import org.sonar.plugins.php.SymbolScanner;

import static org.assertj.core.api.Assertions.assertThat;

public class ProjectSymbolDataSerializerTest {
  private final SensorContextTester context = SensorContextTester.create(new File("src/test/resources").getAbsoluteFile());

  @Test
  public void shouldSerializeAndDeserializeExampleData() {
    ProjectSymbolData projectSymbolData = new ProjectSymbolData();
    List<MethodSymbolData> methods = new ArrayList<>();
    methods.add(new MethodSymbolData(
      new LocationInFileImpl("Mail.php", 183, 27, 183, 46),
      "setDefaultTransport",
      List.of(new Parameter("$transport", "Zend_Mail_Transport_Abstract", false, false)),
    new FunctionSymbolData.FunctionSymbolProperties(false, false),
      Visibility.PUBLIC,
      false));
    methods.add(new MethodSymbolData(
      new LocationInFileImpl("Mail.php", 195, 27, 195, 46),
      "getDefaultTransport",
      List.of(),
      new FunctionSymbolData.FunctionSymbolProperties(true, false),
      Visibility.PUBLIC,
      false));

    methods.add(new MethodSymbolData(
      new LocationInFileImpl("Mail.php", 215, 20, 215, 31),
      "__construct",
      List.of(new Parameter("$charset", null, true, false)),
      new FunctionSymbolData.FunctionSymbolProperties(false, false),
      Visibility.PUBLIC,
      false
    ));

    ClassSymbolData classSymbolData = new ClassSymbolData(
      new LocationInFileImpl("Mail.php", 52, 6, 52, 15),
      SymbolQualifiedName.qualifiedName("zend_mail"),
      SymbolQualifiedName.qualifiedName("zend_mime_message"),
      List.of(),
      ClassSymbol.Kind.NORMAL,
      methods);
    projectSymbolData.add(classSymbolData);
    FunctionSymbolData functionSymbolData = new FunctionSymbolData(
      new LocationInFileImpl("file1.php", 2,9,2,12),
      SymbolQualifiedName.qualifiedName("foo"),
      List.of(new Parameter("$i", "int", false, false)),
      new FunctionSymbolData.FunctionSymbolProperties(false, false)
    );
    projectSymbolData.add(functionSymbolData);

    ProjectSymbolDataSerializer.SerializationData binary = ProjectSymbolDataSerializer.toBinary(projectSymbolData);
    ProjectSymbolData actual = ProjectSymbolDataDeserializer.fromBinary(binary.data(), binary.stringTable());

    assertThat(actual).isEqualTo(projectSymbolData);
  }

  @Test
  public void shouldSerializeAndDeserializeData() {
    DurationStatistics statistics = new DurationStatistics(context.config());
    CacheContext cacheContext = CacheContextImpl.of(context);
    Cache cache = new Cache(cacheContext);
    SymbolScanner symbolScanner = new SymbolScanner(context, statistics, cache);
    ProjectSymbolData projectSymbolData = symbolScanner.getProjectSymbolData();

    ProjectSymbolDataSerializer.SerializationData binary = ProjectSymbolDataSerializer.toBinary(projectSymbolData);
    ProjectSymbolData actual = ProjectSymbolDataDeserializer.fromBinary(binary.data(), binary.stringTable());

    assertThat(actual).isEqualTo(projectSymbolData);
  }
}
