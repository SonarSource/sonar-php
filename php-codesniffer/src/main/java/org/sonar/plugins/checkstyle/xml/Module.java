/*
 * Sonar, open source software quality management tool.
 * Copyright (C) 2010 SQLi
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

package org.sonar.plugins.checkstyle.xml;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.sonar.plugins.php.core.executor.PhpPluginExecutionException;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * The Class Module.
 */
@XStreamAlias("module")
public class Module implements Comparable<String> {

  /** The Constant MODULE_SEPARATOR. */
  public static final String MODULE_SEPARATOR = "/";

  /** The name. */
  @XStreamAsAttribute
  private String name;

  /** The children. */
  @XStreamImplicit
  private List<Module> children;

  /** The properties. */
  @XStreamImplicit(itemFieldName = "property")
  private List<Property> properties;

  /** The metadata.Not used for sonar plugin */
  @XStreamImplicit(itemFieldName = "metadata")
  private List<Metadata> metadata;

  /**
   * Instantiates a new module.
   * 
   * @param name
   *          the name
   * @param parent
   *          the parent
   * @param properties
   *          the properties
   */
  public Module(String name, Module parent, List<Property> properties) {
    this.name = name;
    this.properties = properties;
    this.children = new ArrayList<Module>();
    if (parent != null) {
      parent.addChild(this);
    }
  }

  /**
   * Instantiates a new module.
   * 
   * @param name
   *          the name
   * @param parent
   *          the parent
   */
  public Module(String name, Module parent) {
    this(name, parent, null);
  }

  /**
   * Instantiates a new module.
   * 
   * @param name
   *          the name
   */
  public Module(String name) {
    this(name, null, null);
  }

  /**
   * Adds the child.
   * 
   * @param child
   *          the child
   */
  public void addChild(Module child) {
    children.add(child);
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
   * Sets the properties.
   * 
   * @param properties
   *          the new properties
   */
  public void setProperties(List<Property> properties) {
    this.properties = properties;
  }

  /**
   * Gets the properties.
   * 
   * @return the properties
   */
  public List<Property> getProperties() {
    return properties;
  }

  /**
   * Gets the children.
   * 
   * @return the children
   */
  public List<Module> getChildren() {
    return children;
  }

  /**
   * Compares the module name to the given String
   * 
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  public int compareTo(String value) {
    return value.compareTo(name);
  }

  /**
   * Gets the child position.
   * 
   * @param nameToFind
   *          the name to find
   * 
   * @return the child position
   */
  public int getChildPosition(String nameToFind) {
    int i = 0;
    for (Module child : children) {
      if (child.getName().equals(nameToFind)) {
        return i;
      }
      i++;
    }
    return -1;
  }

  /**
   * Gets the or create child.
   * 
   * @param configKey
   *          the config key
   * 
   * @return the or create child
   */
  public Module getOrCreateChild(String configKey) {
    Module result = this;
    String[] modules = StringUtils.split(configKey, MODULE_SEPARATOR);
    for (int modulePos = 0; modulePos < modules.length; modulePos++) {
      String moduleName = modules[modulePos];
      if (result.getChildren().isEmpty()) {
        Module child = new Module(moduleName);
        result.addChild(child);
        result = child;
      } else {
        int index = result.getChildPosition(moduleName);
        // Create new module if it doesn't exists or if it's the last
        // one (To allow same rule activation)
        if (index < 0 || modulePos == modules.length - 1) {
          Module child = new Module(moduleName);
          result.addChild(child);
          result = child;
        } else {
          result = result.getChildren().get(index);
        }
      }
    }
    return result;
  }

  /**
   * New x stream.
   * 
   * @return the x stream
   */
  private static XStream newXStream() {
    XStream xStream = new XStream();
    xStream.processAnnotations(Module.class);
    xStream.processAnnotations(Property.class);
    xStream.processAnnotations(Metadata.class);
    return xStream;
  }

  /**
   * From xml.
   * 
   * @param xml
   *          the xml
   * 
   * @return the module
   */
  public static Module fromXml(String xml) {
    InputStream input = null;
    try {
      input = IOUtils.toInputStream(xml, "UTF-8");
      return (Module) newXStream().fromXML(input);

    } catch (IOException e) {
      throw new PhpPluginExecutionException("can't read configuration file", e);

    } finally {
      IOUtils.closeQuietly(input);
    }
  }

  /**
   * @param metadata
   *          the metadata to set
   */
  protected final void setMetadata(List<Metadata> metadata) {
    this.metadata = metadata;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    return new HashCodeBuilder(1, 31).append(children).append(metadata).append(name).append(properties).hashCode();
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
    Module other = (Module) obj;
    return new EqualsBuilder().append(children, other.children).append(metadata, other.metadata).append(name, other.name).append(
        properties, other.properties).isEquals();
  }

  /**
   * To xml.
   * 
   * @return the string
   */
  public String toXml() {
    return newXStream().toXML(this);
  }

}
