<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet
    version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/"
    xmlns:mgmt="http://www.datapower.com/schemas/management"
    xmlns:dpctl="http://www.dpctl.org/"
    dpctl:category="SOMA"
    dpctl:doc="Export domain configuration and files"
    exclude-result-prefixes="xsl dpctl">
  <xsl:output method="xml" encoding="UTF-8" omit-xml-declaration="no" media-type="text/xml"/>

  <xsl:param name="domain" dpctl:doc="DataPower domain" dpctl:required="true"/>
  <xsl:param name="format" dpctl:doc="Export format [ZIP|XML]" dpctl:required="true"/>
  <xsl:param name="all-files" dpctl:doc="Export all files [true|false]"/>
  <xsl:param name="persisted" dpctl:doc="Returns only persisted objects [true|false]"/>
  <xsl:param name="deployment-policy" dpctl:doc="Deployment policy"/>
  <xsl:param name="deployment-policy-file" dpctl:doc="Deployment policy file"/>
  <xsl:param name="user-comment" dpctl:doc="Descriptive summary of the export package"/>
  <xsl:param name="class" dpctl:doc="Object class"/>
  <xsl:param name="name" dpctl:doc="Object name"/>
  <xsl:param name="ref-objects" dpctl:doc="Export referenced objects [true|false]"/>
  <xsl:param name="ref-files" dpctl:doc="Export referenced files [true|false]"/>
  <xsl:param name="include-debug" dpctl:doc="Include debug information [true|false]"/>
  <xsl:param name="file" dpctl:doc="Output file" dpctl:required="true"/>

  <xsl:template match="/">
    <soap:Envelope>
      <soap:Body>
        <xsl:variable name="object-nodes">
          <xsl:call-template name="build-do-export-object">
            <xsl:with-param name="class" select="$class"/>
            <xsl:with-param name="name" select="$name"/>
            <xsl:with-param name="ref-objects" select="$ref-objects"/>
            <xsl:with-param name="ref-files" select="$ref-files"/>
            <xsl:with-param name="include-debug" select="$include-debug"/>
          </xsl:call-template>
        </xsl:variable>

        <mgmt:request domain="{$domain}">
          <mgmt:do-export>
            <xsl:if test="$format">
              <xsl:attribute name="format"><xsl:value-of select="$format"/></xsl:attribute>
            </xsl:if>
            <xsl:if test="$all-files">
              <xsl:attribute name="all-files"><xsl:value-of select="$all-files"/></xsl:attribute>
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
            <xsl:copy-of select="$object-nodes"/>
            <xsl:if test="$deployment-policy-file">
              <xsl:copy-of select="document($deployment-policy-file)"/>
            </xsl:if>
          </mgmt:do-export>
        </mgmt:request>
      </soap:Body>
    </soap:Envelope>
  </xsl:template>

  <xsl:template name="build-do-export-object">
    <xsl:param name="class"/>
    <xsl:param name="name"/>
    <xsl:param name="ref-objects"/>
    <xsl:param name="ref-files"/>
    <xsl:param name="include-debug"/>

    <mgmt:object>
      <xsl:if test="$class">
        <xsl:attribute name="class"><xsl:value-of select="$class"/></xsl:attribute>
      </xsl:if>
      <xsl:if test="$name">
        <xsl:attribute name="name"><xsl:value-of select="$name"/></xsl:attribute>
      </xsl:if>
      <xsl:if test="$ref-objects">
        <xsl:attribute name="ref-objects"><xsl:value-of select="$ref-objects"/></xsl:attribute>
      </xsl:if>
      <xsl:if test="$ref-files">
        <xsl:attribute name="ref-files"><xsl:value-of select="$ref-files"/></xsl:attribute>
      </xsl:if>
      <xsl:if test="$include-debug">
        <xsl:attribute name="include-debug"><xsl:value-of select="$include-debug"/></xsl:attribute>
      </xsl:if>
    </mgmt:object>
  </xsl:template>
</xsl:stylesheet>
