<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet
    version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/"
    xmlns:mgmt="http://www.datapower.com/schemas/management"
    xmlns:dpctl="http://www.dpctl.org/"
    dpctl:category="SOMA Actions"
    dpctl:doc="Secure restore action"
    exclude-result-prefixes="xsl dpctl">
  <xsl:output method="xml" encoding="UTF-8" omit-xml-declaration="no" media-type="text/xml"/>

  <xsl:param name="cred" dpctl:doc="Identification credentials object" dpctl:required="true"/>
  <xsl:param name="source" dpctl:doc="Source directory URL" dpctl:required="true"/>
  <xsl:param name="validate" dpctl:doc="Only validate the backup [true|false]" dpctl:required="false"/>
  <xsl:param name="backup-machine-type" dpctl:doc="Backup appliance model" dpctl:required="false"/>

  <xsl:template match="/">
    <soap:Envelope>
      <soap:Body>
        <mgmt:request>
          <mgmt:do-action>
            <SecureBackup>
              <cred><xsl:value-of select="$cred"/></cred>
              <source><xsl:value-of select="$source"/></source>
              <xsl:if test="$validate">
                <validate><xsl:value-of select="$validate"/></validate>
              </xsl:if>
              <xsl:if test="$backup-machine-type">
                <backup-machine-type><xsl:value-of select="$backup-machine-type"/></backup-machine-type>
              </xsl:if>
            </SecureBackup>
          </mgmt:do-action>
        </mgmt:request>
      </soap:Body>
    </soap:Envelope>
  </xsl:template>
</xsl:stylesheet>
