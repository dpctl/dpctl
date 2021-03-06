<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet
    version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/"
    xmlns:mgmt="http://www.datapower.com/schemas/management"
    xmlns:dpctl="http://www.dpctl.org/"
    dpctl:category="SOMA Actions"
    dpctl:doc="Flush document cache action"
    exclude-result-prefixes="xsl dpctl">
  <xsl:output method="xml" encoding="UTF-8" omit-xml-declaration="no" media-type="text/xml"/>

  <xsl:param name="domain" dpctl:doc="DataPower domain" dpctl:required="true"/>
  <xsl:param name="xml-manager" dpctl:doc="Xml manager" dpctl:required="true"/>
  <xsl:param name="match-pattern" dpctl:doc="Match pattern" dpctl:default="*"/>
  <xsl:param name="expire-entries" dpctl:doc="Expire entries [on|off]"/>

  <xsl:template match="/">
    <soap:Envelope>
      <soap:Body>
        <mgmt:request domain="{$domain}">
          <mgmt:do-action>
            <FlushDocumentCache>
              <XMLManager><xsl:value-of select="$xml-manager"/></XMLManager>
              <xsl:if test="$match-pattern">
                <MatchPattern><xsl:value-of select="$match-pattern"/></MatchPattern>
              </xsl:if>
              <xsl:if test="$expire-entries">
                <ExpireEntries><xsl:value-of select="$expire-entries"/></ExpireEntries>
              </xsl:if>
            </FlushDocumentCache>
          </mgmt:do-action>
        </mgmt:request>
      </soap:Body>
    </soap:Envelope>
  </xsl:template>
</xsl:stylesheet>
