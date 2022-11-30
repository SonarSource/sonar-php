package org.sonar.plugins.php.cache;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.sonar.api.internal.apachecommons.io.IOUtils;
import org.sonar.php.symbols.ClassSymbolData;
import org.sonar.php.symbols.FunctionSymbolData;
import org.sonar.php.symbols.MethodSymbolData;
import org.sonar.php.symbols.Parameter;
import org.sonar.php.symbols.ProjectSymbolData;
import org.sonar.plugins.php.api.symbols.QualifiedName;
import org.sonar.plugins.php.api.visitors.LocationInFile;

public class ProjectSymbolDataSerializer {

  private final ByteArrayOutputStream stream;
  private final VarLengthOutputStream out;
  private final StringTable stringTable;

  private ProjectSymbolDataSerializer() {
    stream = new ByteArrayOutputStream();
    out = new VarLengthOutputStream(stream);
    stringTable = new StringTable();
  }

  public static byte [] toBinary(ProjectSymbolData projectSymbolData) {
    ProjectSymbolDataSerializer serializer = new ProjectSymbolDataSerializer();
    serializer.convert(projectSymbolData);
    return serializer.stream.toByteArray();
  }


  private void convert(ProjectSymbolData projectSymbolData) {
    try {
      Map<QualifiedName, ClassSymbolData> classSymbolsByQualifiedName = projectSymbolData.classSymbolsByQualifiedName();
      writeInt(classSymbolsByQualifiedName.size());
      for (Map.Entry<QualifiedName, ClassSymbolData> entry : classSymbolsByQualifiedName.entrySet()) {
        write(entry.getKey());
        write(entry.getValue());
      }
      Map<QualifiedName, List<FunctionSymbolData>> nameFuncSymbolData = projectSymbolData.functionSymbolsByQualifiedName();
      writeInt(nameFuncSymbolData.size());
      for (Map.Entry<QualifiedName, List<FunctionSymbolData>> entry : nameFuncSymbolData.entrySet()) {
        write(entry.getKey());
        write(entry.getValue());
      }
    } catch (IOException e) {
      throw new IllegalStateException("Can't store data in cache", e);
    } finally {
      IOUtils.closeQuietly(out, stream);
    }
  }

  private void write(QualifiedName qualifiedName) throws IOException {
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
      writeText(method.visibility().toString());
      writeText(method.name());
      writeBoolean(method.isAbstract());
      write(method.location());
      write(method.qualifiedName());
      writeParameters(method.parameters());
    }
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
    if("[unknown file]".equals(filePath)) {
      writeText(filePath);
    } else {
      writeText(filePath == null ? "" : filePath);
      writeInt(location.startLine());
      writeInt(location.startLineOffset());
      writeInt(location.endLine());
      writeInt(location.endLineOffset());
    }
  }

  private void write(List<FunctionSymbolData> symbolDataList) throws IOException {
    writeInt(symbolDataList.size());
    for (FunctionSymbolData data : symbolDataList) {
      // Be sure there is only FunctionSymbolData ant not MethodSymbolData
      write(data.location());
      write(data.qualifiedName());
      writeParameters(data.parameters());
      write(data.properties());
    }
  }

  private void write(FunctionSymbolData.FunctionSymbolProperties properties) throws IOException {
    writeBoolean(properties.hasReturn());
    writeBoolean(properties.hasFuncGetArgs());
  }

  private void writeText(String text) throws IOException {
    out.writeInt(stringTable.getIndex(text));
  }

  private void writeInt(int number) throws IOException {
    out.writeInt(number);
  }

  private void writeBoolean(boolean bool) throws IOException {
    out.writeBoolean(bool);
  }
}
