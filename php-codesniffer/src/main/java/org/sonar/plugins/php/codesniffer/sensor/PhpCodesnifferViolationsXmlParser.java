package org.sonar.plugins.php.codesniffer.sensor;

import java.io.File;

import javax.xml.stream.XMLStreamException;

import org.codehaus.staxmate.in.SMInputCursor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.AbstractViolationsStaxParser;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.Resource;
import org.sonar.api.rules.RulesManager;
import org.sonar.plugins.php.codesniffer.PhpCodesnifferPlugin;
import org.sonar.plugins.php.core.resources.PhpFile;

/**
 * The Class PmdViolationsXmlParser.
 */
class PhpCheckStyleViolationsXmlParser extends AbstractViolationsStaxParser {

	private static final String FILE_NAME_ATTRIBUTE_NAME = "name";

	private static final String RULE_NAME_ATTRIBUTE_NAME = "source";

	private static final String LINE_NUMBER_ATTRIBUTE_NAME = "line";

	private static final String VIOLATION_NODE_NAME = "error";

	private static final String FILE_NODE_NAME = "file";

	private static final String MESSAGE_ATTRIBUTE_NAME = "message";

	private static final Logger LOG = LoggerFactory.getLogger(PhpCheckStyleViolationsXmlParser.class);

	/** The project. */
	private Project project;

	/**
	 * Instantiates a new checkstyle violations xml parser.
	 * 
	 * @param project the project
	 * @param context the context
	 * @param rulesManager the rules manager
	 * @param profile the profile
	 */
	public PhpCheckStyleViolationsXmlParser(Project project, SensorContext context, RulesManager rulesManager) {
		super(context, rulesManager);
		this.project = project;
	}

	/**
	 * @see org.sonar.api.batch.AbstractViolationsStaxParser#cursorForResources(org .codehaus.staxmate.in.SMInputCursor)
	 */
	@Override
	protected SMInputCursor cursorForResources(SMInputCursor rootCursor) throws XMLStreamException {
		return rootCursor.descendantElementCursor(FILE_NODE_NAME);
	}

	/**
	 * @see org.sonar.api.batch.AbstractViolationsStaxParser#cursorForViolations(org.codehaus.staxmate.in.SMInputCursor)
	 */
	@Override
	protected SMInputCursor cursorForViolations(SMInputCursor resourcesCursor) throws XMLStreamException {
		return resourcesCursor.descendantElementCursor(VIOLATION_NODE_NAME);
	}

	/**
	 * @see org.sonar.api.batch.AbstractViolationsStaxParser#keyForPlugin()
	 */
	@Override
	protected String keyForPlugin() {
		return PhpCodesnifferPlugin.KEY;
	}

	/**
	 * @see org.sonar.api.batch.AbstractViolationsStaxParser#lineNumberForViolation (org.codehaus.staxmate.in.SMInputCursor)
	 */
	@Override
	protected String lineNumberForViolation(SMInputCursor violationCursor) throws XMLStreamException {
		return violationCursor.getAttrValue(LINE_NUMBER_ATTRIBUTE_NAME);
	}

	/**
	 * @see org.sonar.api.batch.AbstractViolationsStaxParser#messageFor(org.codehaus .staxmate.in.SMInputCursor)
	 */
	@Override
	protected String messageFor(SMInputCursor violationCursor) throws XMLStreamException {
		return violationCursor.getAttrValue(MESSAGE_ATTRIBUTE_NAME);
	}

	/**
	 * @see org.sonar.api.batch.AbstractViolationsStaxParser#ruleKey(org.codehaus .staxmate.in.SMInputCursor)
	 */
	@Override
	protected String ruleKey(SMInputCursor violationCursor) throws XMLStreamException {
		return violationCursor.getAttrValue(RULE_NAME_ATTRIBUTE_NAME);
	}

	/**
	 * Returns the php file corresponding to the given violation
	 * 
	 * @see org.sonar.api.batch.AbstractViolationsStaxParser#toResource(org.codehaus.staxmate.in.SMInputCursor)
	 */
	@Override
	protected Resource toResource(SMInputCursor resourcesCursor) throws XMLStreamException {
		String fileName = resourcesCursor.getAttrValue(FILE_NAME_ATTRIBUTE_NAME);
		if (LOG.isDebugEnabled()) {
			LOG.debug("Loading " + fileName + " to be associated with rule violation");
		}
		return PhpFile.fromIOFile(new File(fileName), project.getFileSystem().getSourceDirs(), false);
	}
}
