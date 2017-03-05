<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet
    version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/"
    xmlns:mgmt="http://www.datapower.com/schemas/management"
    xmlns:dpctl="http://www.dpctl.org/"
    dpctl:category="SOMA"
    dpctl:doc="Get log"
    exclude-result-prefixes="xsl dpctl">
  <xsl:output method="xml" encoding="UTF-8" omit-xml-declaration="no" media-type="text/xml"/>

  <xsl:param name="domain" dpctl:doc="DataPower domain" dpctl:required="true"/>
  <xsl:param name="name" dpctl:doc="Remote file name"/>

  <xsl:template match="/">
    <soap:Envelope>
      <soap:Body>
        <mgmt:request domain="{$domain}">
          <mgmt:get-log>
            <xsl:if test="$name">
              <xsl:attribute name="name"><xsl:value-of select="$name"/></xsl:attribute>
            </xsl:if>
          </mgmt:get-log>
        </mgmt:request>
      </soap:Body>
    </soap:Envelope>
  </xsl:template>
</xsl:stylesheet>
