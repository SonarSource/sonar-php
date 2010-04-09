package org.sonar.plugins.php.phpdepend.xml;

import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * The PackegeNode class represent a phpdepend metrics node. It's used by XStream to marschall or unmarshall xml files.
 */
@XStreamAlias("package")
public class PackageNode {

	/** The files. */
	@XStreamImplicit
	private List<FileNode> files;

	/**
	 * Default constructor with a list of classes.
	 * 
	 * @param files
	 */
	public PackageNode(List<FileNode> files) {
		super();
		this.files = files;
	}

	/**
	 * Gets the files.
	 * 
	 * @return the files
	 */
	public List<FileNode> getFiles() {
		return files;
	}

	/**
	 * Sets the files.
	 * 
	 * @param files the new files
	 */
	public void setFiles(List<FileNode> files) {
		this.files = files;
	}

}
