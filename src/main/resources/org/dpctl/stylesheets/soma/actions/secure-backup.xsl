<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet
    version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/"
    xmlns:mgmt="http://www.datapower.com/schemas/management"
    xmlns:dpctl="http://www.dpctl.org/"
    dpctl:category="SOMA Actions"
    dpctl:doc="Secure backup action"
    exclude-result-prefixes="xsl dpctl">
  <xsl:output method="xml" encoding="UTF-8" omit-xml-declaration="no" media-type="text/xml"/>

  <xsl:param name="cert" dpctl:doc="Crypto certificate object" dpctl:required="true"/>
  <xsl:param name="destination" dpctl:doc="Destination directory URL" dpctl:required="true"/>
  <xsl:param name="include-iscsi" dpctl:doc="Include iSCSI [true|false]" dpctl:required="false"/>
  <xsl:param name="include-raid" dpctl:doc="Include RAID [true|false]" dpctl:required="false"/>

  <xsl:template match="/">
    <soap:Envelope>
      <soap:Body>
        <mgmt:request>
          <mgmt:do-action>
            <SecureBackup>
              <cert><xsl:value-of select="$cert"/></cert>
              <destination><xsl:value-of select="$destination"/></destination>
              <xsl:if test="$include-iscsi">
                <include-iscsi><xsl:value-of select="$include-iscsi"/></include-iscsi>
              </xsl:if>
              <xsl:if test="$include-raid">
                <include-raid><xsl:value-of select="$include-raid"/></include-raid>
              </xsl:if>
            </SecureBackup>
          </mgmt:do-action>
        </mgmt:request>
      </soap:Body>
    </soap:Envelope>
  </xsl:template>
</xsl:stylesheet>
