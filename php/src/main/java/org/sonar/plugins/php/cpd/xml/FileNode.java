/**
 * 
 */
package org.sonar.plugins.php.cpd.xml;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/**
 * Model a <file> tag in the pmd-cpd XML file.
 * 
 * @author akram
 * 
 */
@XStreamAlias("file")
public class FileNode {

  /**
   * The for the file containing duplication;
   */
  @XStreamAlias("path")
  @XStreamAsAttribute
  private String path;
  /**
   * The line number containing duplication.
   */
  @XStreamAlias("line")
  @XStreamAsAttribute
  private Double lineNumber;

  /**
   * @return the path
   */
  public String getPath() {
    return path;
  }

  /**
   * @return the lineNumber
   */
  public Double getLineNumber() {
    return lineNumber;
  }

}
