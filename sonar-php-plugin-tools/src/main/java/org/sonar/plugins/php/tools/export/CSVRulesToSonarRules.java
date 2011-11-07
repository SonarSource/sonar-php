package org.sonar.plugins.php.tools.export;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * @author Akram Ben Aissi
 * 
 */
public class CSVRulesToSonarRules {

  /**
   * @param args
   * @throws IOException
   */
  public static void generateSonarRulesXml(String inputFile, String outputFile) throws IOException {
    InputStream stream = new FileInputStream(inputFile);
    InputStreamReader streamReader = new InputStreamReader(stream);
    BufferedReader reader = new BufferedReader(streamReader);

    FileOutputStream out = new FileOutputStream(outputFile);
    BufferedOutputStream output = new BufferedOutputStream(out);

    StringBuilder buffer = new StringBuilder("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?><rules>");
    // first line contains colum, so we skip it.
    String line = reader.readLine();
    line = reader.readLine();
    while (line != null) {
      String[] tokens = line.split(",");
      buffer.append("<rule key=\"").append(tokens[0]).append("\" priority=\"").append(tokens[1]).append("\">");
      buffer.append("<category name=\"Maintainability\"/>");
      buffer.append("<name>").append(tokens[2]).append("</name>");
      buffer.append("<configKey>").append(tokens[3]).append("</configKey>");
      String description = tokens[2];
      if (tokens.length > 4) {
        description = tokens[4];
        if (description.startsWith("\"")) {
          description = description.substring(1);
        }
        if (description.endsWith("\"")) {
          description = description.substring(0, description.length() - 1);
        }
      }
      buffer.append("<description>");
      buffer.append("<![CDATA[").append(description).append("]]>");
      buffer.append("</description>");
      buffer.append("</rule>\n");
      line = reader.readLine();
    }
    buffer.append("</rules>");
    output.write(buffer.toString().getBytes());
    output.close();
  }
}
