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

    /*
     * <rule key="PHPCS/Generic/RC_DYNAMIC_ERROR" priority="MAJOR"> <category name="Maintainability" /> <name>RC_DYNAMIC_ERROR</name>
     * <configKey>rulesets/RC_DYNAMIC_ERROR</configKey> <description>:message</description> </rule>
     * key,priority,category,name,configKey,description
     */
    StringBuilder buffer = new StringBuilder("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?><rules>");
    // first line contains colum, so we skip it.
    String line = reader.readLine();
    line = reader.readLine();
    while (line != null) {
      String[] tokens = line.split(",");
      buffer.append("<rule key=\"").append(tokens[0]).append("\" priority=\"").append(tokens[1]).append("\">");
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
    buffer.append("</rules>");
    output.write(buffer.toString().getBytes());
    output.close();
  }
}
