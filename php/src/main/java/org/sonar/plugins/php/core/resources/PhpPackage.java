package org.sonar.plugins.php.core.resources;

import org.apache.commons.lang.StringUtils;
import org.sonar.api.resources.Language;
import org.sonar.api.resources.Resource;
import org.sonar.api.utils.WildcardPattern;
import org.sonar.plugins.php.core.Php;

/**
 * Defines a php package
 */
public class PhpPackage extends Resource {

	/** The Constant DEFAULT_PACKAGE_NAME. */
	public static final String DEFAULT_PACKAGE_NAME = "";

	/**
	 * Instantiates a new php package.
	 */
	public PhpPackage() {
		this(null);
	}

	/**
	 * Instantiates a new php package.
	 * 
	 * @param key the key
	 */
	public PhpPackage(String key) {
		setKey(StringUtils.defaultIfEmpty(StringUtils.trim(key), DEFAULT_PACKAGE_NAME));
	}

	/**
	 * @see org.sonar.api.resources.Resource#getLongName()
	 */
	public String getLongName() {
		return getName();
	}

	/**
	 * Checks if this package is the default one.
	 * 
	 * @return <code>true</code> the package key is empty, <code>false</code> in any other case
	 */
	public boolean isDefault() {
		return StringUtils.equals(getKey(), DEFAULT_PACKAGE_NAME);
	}

	/**
	 * Match file pattern.
	 * 
	 * @param antPattern the ant pattern
	 * @return true, if match file pattern
	 * @see org.sonar.api.resources.Resource#matchFilePattern(java.lang.String)
	 */
	public boolean matchFilePattern(String antPattern) {
		String patternWithoutFileSuffix = StringUtils.substringBeforeLast(antPattern, ".");
		WildcardPattern matcher = WildcardPattern.create(patternWithoutFileSuffix, ".");
		return matcher.match(getKey());
	}

	@Override
	public String getDescription() {
		return null;
	}

	@Override
	public Language getLanguage() {
		return Php.INSTANCE;
	}

	@Override
	public String getName() {
		return getKey();
	}

	@Override
	public Resource<?> getParent() {
		return null;
	}

	@Override
	public String getQualifier() {
		return Resource.QUALIFIER_PACKAGE;
	}

	@Override
	public String getScope() {
	    return Resource.SCOPE_SPACE;
	}
}
