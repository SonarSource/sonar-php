<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:output method="xml" indent="yes" encoding="iso-8859-1" />
  <xsl:template match="/">
    <rules>
      <xsl:text>&#10;</xsl:text>
      <xsl:for-each select="file/error">
        <rule>
          <xsl:attribute name="key">
        	<xsl:value-of select="@source" />
        </xsl:attribute>
          <xsl:attribute name="severity">
  	 <xsl:if test="level='error'">
  	    <xsl:text>MAJOR</xsl:text>
  	 </xsl:if>
  	 <xsl:if test="level='WARNING'">
  	    <xsl:text>MINOR</xsl:text>
  	 </xsl:if>
        </xsl:attribute>
          <xsl:text>&#10;&#09;</xsl:text>
          <category>
            <xsl:text>Maintenability</xsl:text>
          </category>
          <xsl:text>&#10;&#09;</xsl:text>
          <name>
            <xsl:value-of select="@message" />
          </name>
          <xsl:text>&#10;&#09;</xsl:text>
          <configKey>
            <xsl:value-of select="@source" />
          </configKey>
          <xsl:text>&#10;&#09;</xsl:text>
          <description>
            <xsl:value-of select="@message" />
          </description>
          <xsl:text>&#10;</xsl:text>
        </rule>
        <xsl:text>&#10;</xsl:text>
      </xsl:for-each>
    </rules>
  </xsl:template>
</xsl:stylesheet>

