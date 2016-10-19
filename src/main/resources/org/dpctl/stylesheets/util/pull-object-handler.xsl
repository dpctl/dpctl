<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet
    version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xslt="http://xml.apache.org/xslt"
    exclude-result-prefixes="xslt">
  <xsl:output method="xml" encoding="UTF-8" omit-xml-declaration="yes" media-type="text/xml" indent="yes" xslt:indent-amount="2"/>

  <xsl:strip-space elements="*"/>

  <xsl:template match="*[@class='GeneratedPolicy']">
    <xsl:element name="{local-name()}">
      <xsl:attribute name="class">GeneratedPolicy</xsl:attribute>
    </xsl:element>
  </xsl:template>

  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>
</xsl:stylesheet>
