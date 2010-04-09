package org.sonar.plugins.php.phpunit.xml;

import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * The php unit report root node..
 */
@XStreamAlias("coverage")
public class CoverageNode {

	/** The projects. */
	@XStreamImplicit
	@XStreamAlias("project")
	private List<ProjectNode> projects;

	/**
	 * Instantiates a new coverage node.
	 * 
	 * @param projects the projects
	 */
	public CoverageNode(List<ProjectNode> projects) {
		super();
		this.projects = projects;
	}

	/**
	 * Gets the projects.
	 * 
	 * @return the projects
	 */
	public List<ProjectNode> getProjects() {
		return projects;
	}

	/**
	 * Sets the projects.
	 * 
	 * @param projects the new projects
	 */
	public void setProjects(List<ProjectNode> projects) {
		this.projects = projects;
	}

}
