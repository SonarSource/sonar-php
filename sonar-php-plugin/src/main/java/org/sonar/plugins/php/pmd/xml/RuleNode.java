/*
 * Sonar PHP Plugin
 * Copyright (C) 2010 Codehaus Sonar Plugins
 * dev@sonar.codehaus.org
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
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.plugins.php.pmd.xml;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

/**
 * The Rule class represent a PHPMD rule.
 */
@XStreamAlias("rule")
public class RuleNode implements Comparable<String> {

  /** The class name. */
  @XStreamAsAttribute
  @XStreamAlias("class")
  private String className;

  /**
   * Gets the example.
   * 
   * @return the example
   */
  public final String getExample() {
    return example;
  }

  /**
   * Sets the example.
   * 
   * @param example
   *          the new example
   */
  public final void setExample(String example) {
    this.example = example;
  }

  /**
   * Gets the exclude.
   * 
   * @return the exclude
   */
  public String getExclude() {
    return exclude;
  }

  /**
   * Sets the exclude.
   * 
   * @param exclude
   *          the new exclude
   */
  public final void setExclude(String exclude) {
    this.exclude = exclude;
  }

  /**
   * Gets the external info url.
   * 
   * @return the external info url
   */
  public String getExternalInfoUrl() {
    return externalInfoUrl;
  }

  /**
   * Sets the external info url.
   * 
   * @param externalInfoUrl
   *          the new external info url
   */
  public final void setExternalInfoUrl(String externalInfoUrl) {
    this.externalInfoUrl = externalInfoUrl;
  }

  /**
   * Gets the message.
   * 
   * @return the message
   */
  public String getMessage() {
    return message;
  }

  /**
   * Sets the message.
   * 
   * @param message
   *          the new message
   */
  public final void setMessage(String message) {
    this.message = message;
  }

  /**
   * Gets the since.
   * 
   * @return the since
   */
  public String getSince() {
    return since;
  }

  /**
   * Sets the since.
   * 
   * @param since
   *          the new since
   */
  public final void setSince(String since) {
    this.since = since;
  }

  /**
   * Sets the class name.
   * 
   * @param fileName
   *          the new class name
   */
  public final void setClassName(String className) {
    this.className = className;
  }

  /** The description. */
  @XStreamAlias("description")
  private String description;

  /** The example. */
  @XStreamOmitField
  private String example;

  /** The exclude. */
  @XStreamOmitField
  private String exclude;

  /** The external info url. */
  @XStreamOmitField
  private String externalInfoUrl;

  /** The message. */
  @XStreamOmitField
  private String message;

  /** The since. */
  @XStreamOmitField
  private String since;

  /** The priority. */
  @XStreamAsAttribute
  private String name;

  /** The priority. */
  @XStreamAlias("priority")
  private String priority;

  /** The properties. */
  @XStreamAlias("properties")
  private PropertiesNode properties;

  /**
   * Instantiates a new rule.
   * 
   * @param name
   *          the rule name
   */
  public RuleNode(String name) {
    this(name, null);
  }

  /**
   * Instantiates a new rule.
   * 
   * @param priority
   *          the priority
   * @param name
   *          the name
   */
  public RuleNode(String name, String priority) {
    super();
    properties = new PropertiesNode();
    this.name = name;
    this.priority = priority;
  }

  /**
   * Compare to.
   * 
   * @param o
   *          the o
   * 
   * @return the int
   * 
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  public int compareTo(String o) {
    return o.compareTo(name);
  }

  /**
   * Gets the description.
   * 
   * @return the description
   */
  public String getDescription() {
    return description;
  }

  /**
   * Gets the name.
   * 
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * Gets the class name.
   * 
   * @return the class name
   */
  public String getClassName() {
    return className;
  }

  /**
   * Gets the priority.
   * 
   * @return the priority
   */
  public String getPriority() {
    return priority;
  }

  /**
   * Gets the properties.
   * 
   * @return the properties
   */
  public PropertiesNode getProperties() {
    return properties;
  }

  /**
   * Sets the description.
   * 
   * @param description
   *          the new description
   */
  public final void setDescription(String description) {
    this.description = description;
  }

  /**
   * Sets the name.
   * 
   * @param name
   *          the new name
   */
  public final void setName(String name) {
    this.name = name;
  }

  /**
   * Sets the priority.
   * 
   * @param priority
   *          the new priority
   */
  public final void setPriority(String priority) {
    this.priority = priority;
  }

  /**
   * Sets the properties.
   * 
   * @param properties
   *          the new properties
   */
  public final void setProperties(PropertiesNode properties) {
    this.properties = properties;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    return new HashCodeBuilder().append(className).append(message).append(name).append(priority).toHashCode();
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    RuleNode other = (RuleNode) obj;
    return new EqualsBuilder().append(className, other.className).append(message, other.message).append(name, other.name)
        .append(priority, other.priority).isEquals();
  }

}
