<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet
    version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/"
    xmlns:mgmt="http://www.datapower.com/schemas/management"
    xmlns:dpctl="http://www.dpctl.org/"
    dpctl:doc="Import domain configuration and files"
    exclude-result-prefixes="xsl dpctl">
  <xsl:output method="xml" encoding="UTF-8" omit-xml-declaration="no" media-type="text/xml"/>

  <xsl:param name="domain" dpctl:doc="DataPower domain"/>
  <xsl:param name="format" dpctl:doc=" [ZIP|XML]"/>
  <xsl:param name="dry-run" dpctl:doc=" [true|false]" select=""/>
  <xsl:param name="overwrite-objects" dpctl:doc=" [true|false]" select=""/>
  <xsl:param name="overwrite-files" dpctl:doc=" [true|false]" select=""/>
  <xsl:param name="rewrite-local-ip" dpctl:doc=" [true|false]" select=""/>
  <xsl:param name="deployment-policy" dpctl:doc="" select=""/>
  <xsl:param name="deployment-policy-variables" dpctl:doc="" select=""/>
  <xsl:param name="import-input-file" dpctl:doc="Import input file"/>
  <xsl:param name="import-input" dpctl:doc="Base64 import configuration"/>

  <xsl:template match="/">
    <soap:Envelope>
      <soap:Body>
        <xsl:call-template name="build-do-import-request">
          <xsl:with-param name="domain" select="$domain"/>
          <xsl:with-param name="format" select="$format"/>
          <xsl:with-param name="dry-run" select="$dry-run"/>
          <xsl:with-param name="overwrite-objects" select="$overwrite-objects"/>
          <xsl:with-param name="overwrite-files" select="$overwrite-files"/>
          <xsl:with-param name="rewrite-local-ip" select="$rewrite-local-ip"/>
          <xsl:with-param name="deployment-policy" select="$deployment-policy"/>
          <xsl:with-param name="deployment-policy-variables" select="$deployment-policy-variables"/>
          <xsl:with-param name="import-input-file" select="$import-input-file"/>
        </xsl:call-template>
      </soap:Body>
    </soap:Envelope>
  </xsl:template>

  <xsl:template name="build-do-import-request">
    <xsl:param name="domain"/>
    <xsl:param name="format"/>
    <xsl:param name="dry-run"/>
    <xsl:param name="overwrite-objects"/>
    <xsl:param name="overwrite-files"/>
    <xsl:param name="rewrite-local-ip"/>
    <xsl:param name="deployment-policy"/>
    <xsl:param name="deployment-policy-variables"/>
    <xsl:param name="import-input-file"/>
    <xsl:param name="object-nodes"/>
    <xsl:param name="file-nodes"/>
    <xsl:param name="deployment-policy-nodes"/>
    <xsl:param name="deployment-policy-variables-nodes"/>

    <mgmt:request domain="{$domain}">
      <mgmt:do-import>
        <xsl:if test="$format">
          <xsl:attribute name="source-type"><xsl:value-of select="$format"/></xsl:attribute>
        </xsl:if>
        <xsl:if test="$dry-run">
          <xsl:attribute name="dry-run"><xsl:value-of select="$dry-run"/></xsl:attribute>
        </xsl:if>
        <xsl:if test="$overwrite-objects">
          <xsl:attribute name="overwrite-objects"><xsl:value-of select="$overwrite-objects"/></xsl:attribute>
        </xsl:if>
        <xsl:if test="$overwrite-files">
          <xsl:attribute name="overwrite-files"><xsl:value-of select="$overwrite-files"/></xsl:attribute>
        </xsl:if>
        <xsl:if test="$rewrite-local-ip">
          <xsl:attribute name="rewrite-local-ip"><xsl:value-of select="$rewrite-local-ip"/></xsl:attribute>
        </xsl:if>
        <xsl:if test="$deployment-policy">
          <xsl:attribute name="deployment-policy"><xsl:value-of select="$deployment-policy"/></xsl:attribute>
        </xsl:if>
        <xsl:if test="$deployment-policy-variables">
          <xsl:attribute name="deployment-policy-variables"><xsl:value-of select="$deployment-policy-variables"/></xsl:attribute>
        </xsl:if>
        <mgmt:input-file>
          <xsl:value-of select="$import-input"/>
        </mgmt:input-file>
        <xsl:copy-of select="$object-nodes"/>
        <xsl:copy-of select="$file-nodes"/>
        <xsl:copy-of select="$deployment-policy-nodes"/>
        <xsl:copy-of select="$deployment-policy-variables-nodes"/>
      </mgmt:do-import>
    </mgmt:request>
  </xsl:template>

  <xsl:template name="build-do-import-object">
    <xsl:param name="class"/>
    <xsl:param name="name"/>
    <xsl:param name="overwrite"/>
    <xsl:param name="import-debug"/>

    <mgmt:object>
      <xsl:if test="$class">
        <xsl:attribute name="class"><xsl:value-of select="$class"/></xsl:attribute>
      </xsl:if>
      <xsl:if test="$name">
        <xsl:attribute name="name"><xsl:value-of select="$name"/></xsl:attribute>
      </xsl:if>
      <xsl:if test="$overwrite">
        <xsl:attribute name="overwrite"><xsl:value-of select="$overwrite"/></xsl:attribute>
      </xsl:if>
      <xsl:if test="$import-debug">
        <xsl:attribute name="import-debug"><xsl:value-of select="$import-debug"/></xsl:attribute>
      </xsl:if>
    </mgmt:object>
  </xsl:template>

  <xsl:template name="build-do-import-file">
    <xsl:param name="name"/>
    <xsl:param name="overwrite"/>

    <mgmt:object>
      <xsl:if test="$name">
        <xsl:attribute name="name"><xsl:value-of select="$name"/></xsl:attribute>
      </xsl:if>
      <xsl:if test="$overwrite">
        <xsl:attribute name="overwrite"><xsl:value-of select="$overwrite"/></xsl:attribute>
      </xsl:if>
    </mgmt:object>
  </xsl:template>
</xsl:stylesheet>
