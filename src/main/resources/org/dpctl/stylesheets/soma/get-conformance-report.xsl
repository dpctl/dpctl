<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet
    version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/"
    xmlns:mgmt="http://www.datapower.com/schemas/management"
    xmlns:dpctl="http://www.dpctl.org/"
    dpctl:category="SOMA"
    dpctl:doc="Gets conformance report"
    exclude-result-prefixes="xsl dpctl">
  <xsl:output method="xml" encoding="UTF-8" omit-xml-declaration="no" media-type="text/xml"/>

  <xsl:param name="domain" dpctl:doc="DataPower domain" dpctl:required="true"/>
  <xsl:param name="class" dpctl:doc="Class"/>
  <xsl:param name="name" dpctl:doc="Name"/>
  <xsl:param name="profile" dpctl:doc="Profile"/>

  <xsl:template match="/">
    <soap:Envelope>
      <soap:Body>
        <mgmt:request domain="{$domain}">
          <mgmt:get-conformance-report>
            <xsl:if test="$class">
              <xsl:attribute name="class"><xsl:value-of select="$class"/></xsl:attribute>
            </xsl:if>
            <xsl:if test="$name">
              <xsl:attribute name="name"><xsl:value-of select="$name"/></xsl:attribute>
            </xsl:if>
            <xsl:if test="$profile">
              <xsl:attribute name="profile"><xsl:value-of select="$profile"/></xsl:attribute>
            </xsl:if>
          </mgmt:get-conformance-report>
        </mgmt:request>
      </soap:Body>
    </soap:Envelope>
  </xsl:template>
</xsl:stylesheet>
