<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet
    version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/"
    xmlns:mgmt="http://www.datapower.com/schemas/management"
    xmlns:dpctl="http://www.dpctl.org/"
    dpctl:category="SOMA"
    dpctl:doc="Returns the status information"
    exclude-result-prefixes="xsl dpctl">
  <xsl:output method="xml" encoding="UTF-8" omit-xml-declaration="no" media-type="text/xml"/>

  <xsl:param name="domain" dpctl:doc="DataPower domain"/>
  <xsl:param name="class" dpctl:doc="Status class"/>
  <xsl:param name="object-class" dpctl:doc="Object class"/>
  <xsl:param name="object-name" dpctl:doc="Object name"/>

  <xsl:template match="/">
    <soap:Envelope>
      <soap:Body>
        <mgmt:request>
          <xsl:if test="$domain">
            <xsl:attribute name="domain"><xsl:value-of select="$domain"/></xsl:attribute>
          </xsl:if>
          <mgmt:get-status>
            <xsl:if test="$class">
              <xsl:attribute name="class"><xsl:value-of select="$class"/></xsl:attribute>
            </xsl:if>
            <xsl:if test="$object-class">
              <xsl:attribute name="object-class"><xsl:value-of select="$object-class"/></xsl:attribute>
            </xsl:if>
            <xsl:if test="$object-name">
              <xsl:attribute name="object-name"><xsl:value-of select="$object-name"/></xsl:attribute>
            </xsl:if>
          </mgmt:get-status>
        </mgmt:request>
      </soap:Body>
    </soap:Envelope>
  </xsl:template>
</xsl:stylesheet>
