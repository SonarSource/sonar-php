package org.sonar.plugins.php.pmd.xml;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias("properties")
public class Properties {

	/** The rules. */
	@XStreamImplicit(itemFieldName = "property")
	private List<Property> properties;
	
	
	public Properties() {
		properties = new ArrayList<Property>(); 
	}
	
	public void add(Property property) {
		properties.add(property);
	}

	public List<Property> getProperties() {
		return properties;
	}
	
}
