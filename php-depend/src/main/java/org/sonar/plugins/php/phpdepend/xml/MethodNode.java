package org.sonar.plugins.php.phpdepend.xml;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/**
 * The MethoNode class represent a phpdepend function node. It's used by XStream to marschall or unmarshall xml files.
 */
@XStreamAlias("method")
public final class MethodNode {

	/** The complexity. */
	@XStreamAsAttribute
	@XStreamAlias("ccn")
	private double complexity;

	/**
	 * Instantiates a new method node.
	 * 
	 * @param complexity the complexity
	 */
	public MethodNode(final double complexity) {
		super();
		this.complexity = complexity;
	}

	/**
	 * Gets the complexity.
	 * 
	 * @return the complexity
	 */
	public double getComplexity() {
		return complexity;
	}

	/**
	 * Sets the complexity.
	 * 
	 * @param complexity the new complexity
	 */
	public void setComplexity(final double complexity) {
		this.complexity = complexity;
	}
}
