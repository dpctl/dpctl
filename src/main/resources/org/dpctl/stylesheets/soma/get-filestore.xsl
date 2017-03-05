<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet
    version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/"
    xmlns:mgmt="http://www.datapower.com/schemas/management"
    xmlns:dpctl="http://www.dpctl.org/"
    dpctl:category="SOMA"
    dpctl:doc="Get filestore snapshot"
    exclude-result-prefixes="xsl dpctl">
  <xsl:output method="xml" encoding="UTF-8" omit-xml-declaration="no" media-type="text/xml"/>

  <xsl:param name="domain" dpctl:doc="DataPower domain" dpctl:required="true"/>
  <xsl:param name="location" dpctl:doc="Location [local:|store:|cert:|...]"/>
  <xsl:param name="annotated" dpctl:doc="Annotated [true|false]"/>
  <xsl:param name="layout-only" dpctl:doc="Layout only [true|false]"/>
  <xsl:param name="no-subdirectories" dpctl:doc="No subdirectories [true|false]"/>

  <xsl:template match="/">
    <soap:Envelope>
      <soap:Body>
        <mgmt:request domain="{$domain}">
          <mgmt1:get-filestore xmlns:mgmt1="http://www.datapower.com/schemas/management">
            <xsl:if test="$location">
              <xsl:attribute name="location"><xsl:value-of select="$location"/></xsl:attribute>
            </xsl:if>
            <xsl:if test="$annotated">
              <xsl:attribute name="annotated"><xsl:value-of select="$annotated"/></xsl:attribute>
            </xsl:if>
            <xsl:if test="$layout-only">
              <xsl:attribute name="layout-only"><xsl:value-of select="$layout-only"/></xsl:attribute>
            </xsl:if>
            <xsl:if test="$no-subdirectories">
              <xsl:attribute name="no-subdirectories"><xsl:value-of select="$no-subdirectories"/></xsl:attribute>
            </xsl:if>
          </mgmt1:get-filestore>
        </mgmt:request>
      </soap:Body>
    </soap:Envelope>
  </xsl:template>
</xsl:stylesheet>
