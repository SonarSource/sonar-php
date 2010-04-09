/*
 * Sonar, open source software quality management tool. Copyright (C) 2009 SonarSource SA mailto:contact AT sonarsource DOT com
 * Sonar is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version. Sonar is
 * distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details. You should have received a copy of
 * the GNU Lesser General Public License along with Sonar; if not, write to the Free Software Foundation, Inc., 51 Franklin Street,
 * Fifth Floor, Boston, MA 02
 */
package org.sonar.plugins.php.pmd.xml;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;


/**
 * The property class represent a PHPmd rule property
 */
@XStreamAlias("property")
public class Property {

	/** The property name. */
	@XStreamAsAttribute
	private String name;

	/** The property value. */
	@XStreamAsAttribute
	private String value;

	/** The property description. */
	@XStreamAsAttribute
	private String description;

	/**
	 * Instantiates a new property.
	 * 
	 * @param name the name
	 * @param value the value
	 */
	public Property(String name, String value) {
		this.name = name;
		this.value = value;
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
	 * Gets the value.
	 * 
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Gets the description.
	 * 
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}
}
