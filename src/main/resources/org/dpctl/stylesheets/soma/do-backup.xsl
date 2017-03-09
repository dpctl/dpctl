<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet
    version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/"
    xmlns:mgmt="http://www.datapower.com/schemas/management"
    xmlns:dpctl="http://www.dpctl.org/"
    dpctl:category="SOMA"
    dpctl:doc="Backup config"
    exclude-result-prefixes="xsl dpctl">
  <xsl:output method="xml" encoding="UTF-8" omit-xml-declaration="no" media-type="text/xml"/>

  <xsl:param name="domain" dpctl:doc="DataPower domain" dpctl:required="true"/>
  <xsl:param name="user-comment" dpctl:doc="User comment"/>
  <xsl:param name="format" dpctl:doc="Export format [ZIP|XML]"/>
  <xsl:param name="persisted" dpctl:doc="Persisted [true|false]"/>
  <xsl:param name="deployment-policy" dpctl:doc="Deployment policy"/>
  <xsl:param name="deployment-policy-file" dpctl:doc="Deployment policy file"/>
  <xsl:param name="file" dpctl:doc="Local file" dpctl:required="true"/>

  <xsl:template match="/">
    <soap:Envelope>
      <soap:Body>
        <mgmt:request>
          <mgmt:do-backup>
            <xsl:if test="$format">
              <xsl:attribute name="format"><xsl:value-of select="$format"/></xsl:attribute>
            </xsl:if>
            <xsl:if test="$persisted">
              <xsl:attribute name="persisted"><xsl:value-of select="$persisted"/></xsl:attribute>
            </xsl:if>
            <xsl:if test="$deployment-policy">
              <xsl:attribute name="deployment-policy"><xsl:value-of select="$deployment-policy"/></xsl:attribute>
            </xsl:if>
            <xsl:if test="$user-comment">
              <mgmt:user-comment><xsl:value-of select="$user-comment"/></mgmt:user-comment>
            </xsl:if>
            <mgmt:domain name="{$domain}"/>
            <xsl:if test="$deployment-policy-file">
              <xsl:copy-of select="document($deployment-policy-file)"/>
            </xsl:if>
          </mgmt:do-backup>
        </mgmt:request>
      </soap:Body>
    </soap:Envelope>
  </xsl:template>
</xsl:stylesheet>
