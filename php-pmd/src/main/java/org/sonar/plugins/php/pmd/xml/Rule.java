/*
 * Sonar, open source software quality management tool.
 * Copyright (C) 2010 MyCompany
 * mailto:contact AT sonarsource DOT com
 *
 * Sonar is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * Sonar is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Sonar; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */

package org.sonar.plugins.php.pmd.xml;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

/**
 * The Rule class represent a PHPMD rule.
 */
@XStreamAlias("rule")
public class Rule implements Comparable<String> {

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
   * @param className
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
  private Properties properties;

  /**
   * Instantiates a new rule.
   * 
   * @param name
   *          the rule name
   */
  public Rule(String name) {
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
  public Rule(String name, String priority) {
    super();
    properties = new Properties();
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
  public Properties getProperties() {
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
  public final void setProperties(Properties properties) {
    this.properties = properties;
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((className == null) ? 0 : className.hashCode());
    result = prime * result + ((description == null) ? 0 : description.hashCode());
    result = prime * result + ((example == null) ? 0 : example.hashCode());
    result = prime * result + ((exclude == null) ? 0 : exclude.hashCode());
    result = prime * result + ((externalInfoUrl == null) ? 0 : externalInfoUrl.hashCode());
    result = prime * result + ((message == null) ? 0 : message.hashCode());
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + ((priority == null) ? 0 : priority.hashCode());
    result = prime * result + ((properties == null) ? 0 : properties.hashCode());
    result = prime * result + ((since == null) ? 0 : since.hashCode());
    return result;
  }

  /**
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
    Rule other = (Rule) obj;
    if (className == null) {
      if (other.className != null) {
        return false;
      }
    } else if ( !className.equals(other.className)) {
      return false;
    }
    if (description == null) {
      if (other.description != null) {
        return false;
      }
    } else if ( !description.equals(other.description)) {
      return false;
    }
    if (example == null) {
      if (other.example != null) {
        return false;
      }
    } else if ( !example.equals(other.example)) {
      return false;
    }
    if (exclude == null) {
      if (other.exclude != null) {
        return false;
      }
    } else if ( !exclude.equals(other.exclude)) {
      return false;
    }
    if (externalInfoUrl == null) {
      if (other.externalInfoUrl != null) {
        return false;
      }
    } else if ( !externalInfoUrl.equals(other.externalInfoUrl)) {
      return false;
    }
    if (message == null) {
      if (other.message != null) {
        return false;
      }
    } else if ( !message.equals(other.message)) {
      return false;
    }
    if (name == null) {
      if (other.name != null) {
        return false;
      }
    } else if ( !name.equals(other.name)) {
      return false;
    }
    if (priority == null) {
      if (other.priority != null) {
        return false;
      }
    } else if ( !priority.equals(other.priority)) {
      return false;
    }
    if (properties == null) {
      if (other.properties != null) {
        return false;
      }
    } else if ( !properties.equals(other.properties)) {
      return false;
    }
    if (since == null) {
      if (other.since != null) {
        return false;
      }
    } else if ( !since.equals(other.since)) {
      return false;
    }
    return true;
  }
}