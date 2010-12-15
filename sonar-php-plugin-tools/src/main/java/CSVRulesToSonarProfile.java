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
public class CSVRulesToSonarProfile {

  /**
   * @param args
   * @throws IOException
   */
  public static void generateSonarProfileXml(String inputFile, String outputFile) throws IOException {

    InputStream stream = new FileInputStream(inputFile);
    InputStreamReader streamReader = new InputStreamReader(stream);
    BufferedReader reader = new BufferedReader(streamReader);

    FileOutputStream out = new FileOutputStream(outputFile);
    BufferedOutputStream output = new BufferedOutputStream(out);
    String line = reader.readLine();
    /*
     * <rule priority="BLOCKER"> <key>Code Size Rules/ExcessiveClassLength</key> <repositoryKey>php_codesniffer_rules</repositoryKey>
     * <category name="Maintainability" /> <name>ExcessiveClassLength</name> <configKey>Code Size Rules/ExcessiveClassLength</configKey>
     * <description>:message</description> </rule>
     */
    StringBuilder buffer = new StringBuilder("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?><profile>");
    buffer.append("<name>PHP_CodeSniffer rules</name><language>php</language><rules>");
    // first line contains colum, so we skip it.
    line = reader.readLine();
    line = reader.readLine();
    while (line != null) {
      String[] tokens = line.split(",");
      buffer.append("<rule priority=\"").append(tokens[1]).append("\">");
      buffer.append("<key>").append(tokens[0]).append("</key>\n");
      buffer.append("<repositoryKey>php_codesniffer_rules</repositoryKey>\n");
      buffer.append("<category name=\"").append(tokens[2]).append("\" />");
      buffer.append("<name>").append(tokens[3]).append("</name>");
      buffer.append("<configKey>").append(tokens[4]).append("</configKey>");
      String description = tokens[3];
      if (tokens.length > 5) {
        description = tokens[5];
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
    buffer.append("</rules></profile>");
    output.write(buffer.toString().getBytes());
    output.close();
  }
}
