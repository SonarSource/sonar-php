<?xml version="1.0" encoding="ISO-8859-1"?>

<xsl:stylesheet version="1.0"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:output method="xml" indent="yes" encoding="iso-8859-1"/>

<xsl:template match="/">
  <rules>
  	<xsl:text>&#10;</xsl:text>
    <xsl:for-each select="report/event">
    <rule>
      <xsl:attribute name="key">
      	<xsl:value-of select="@source"/>/<xsl:value-of select="@code"/>
      </xsl:attribute>
      <xsl:attribute name="priority">
	 <xsl:if test="level='ERROR'">
	    <xsl:text>MAJOR</xsl:text>
	 </xsl:if>
	 <xsl:if test="level='WARNING'">
	    <xsl:text>MINOR</xsl:text>
	 </xsl:if>
      </xsl:attribute>
      <xsl:text>&#10;&#09;</xsl:text>
      <category>
      	<xsl:attribute name="name">
      	  <xsl:value-of select="category"/>
      	</xsl:attribute>
      </category>
      <xsl:text>&#10;&#09;</xsl:text>
      <name><xsl:value-of select="@code"/></name>
      <xsl:text>&#10;&#09;</xsl:text>
      <configKey>rulesets/<xsl:value-of select="@code"/></configKey>
      <xsl:text>&#10;&#09;</xsl:text>
      <description><xsl:value-of select="message"/></description>
      <xsl:text>&#10;</xsl:text>
    </rule>
      <xsl:text>&#10;</xsl:text>
    </xsl:for-each>
  </rules>
</xsl:template>

</xsl:stylesheet>
