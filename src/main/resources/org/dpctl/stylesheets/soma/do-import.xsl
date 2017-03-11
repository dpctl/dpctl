<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet
    version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/"
    xmlns:mgmt="http://www.datapower.com/schemas/management"
    xmlns:dpctl="http://www.dpctl.org/"
    xmlns:java="http://xml.apache.org/xslt/java"
    dpctl:category="SOMA"
    dpctl:doc="Import domain configuration and files"
    exclude-result-prefixes="xsl dpctl java">
  <xsl:output method="xml" encoding="UTF-8" omit-xml-declaration="no" media-type="text/xml"/>

  <xsl:param name="domain" dpctl:doc="DataPower domain" dpctl:required="true"/>
  <xsl:param name="format" dpctl:doc="Export format [ZIP|XML]" dpctl:required="true"/>
  <xsl:param name="dry-run" dpctl:doc="Dry run [true|false]"/>
  <xsl:param name="overwrite-files" dpctl:doc="Overwrite files [true|false]"/>
  <xsl:param name="overwrite-objects" dpctl:doc="Overwrite objects [true|false]"/>
  <xsl:param name="rewrite-local-ip" dpctl:doc="Rewrite local IP [true|false]"/>
  <xsl:param name="objects-file" dpctl:doc="Objects file"/>
  <xsl:param name="files-file" dpctl:doc="Files file"/>
  <xsl:param name="deployment-policy" dpctl:doc="Deployment policy"/>
  <xsl:param name="deployment-policy-variables" dpctl:doc="Deployment policy variables"/>
  <xsl:param name="deployment-policy-file" dpctl:doc="Deployment policy file"/>
  <xsl:param name="deployment-policy-variables-file" dpctl:doc="Deployment policy variables file"/>
  <xsl:param name="file" dpctl:doc="Input file" dpctl:required="true"/>
  <xsl:param name="import-input" dpctl:doc="Base64 import configuration"/>

  <xsl:variable name="read-data-from-file-fn" select="java:clojure.java.api.Clojure.var('org.dpctl.util.io','read-data-from-file')"/>

  <xsl:template match="/">
    <soap:Envelope>
      <soap:Body>
        <mgmt:request domain="{$domain}">
          <mgmt:do-import>
            <xsl:if test="$format">
              <xsl:attribute name="source-type"><xsl:value-of select="$format"/></xsl:attribute>
            </xsl:if>
            <xsl:if test="$dry-run">
              <xsl:attribute name="dry-run"><xsl:value-of select="$dry-run"/></xsl:attribute>
            </xsl:if>
            <xsl:if test="$overwrite-files">
              <xsl:attribute name="overwrite-files"><xsl:value-of select="$overwrite-files"/></xsl:attribute>
            </xsl:if>
            <xsl:if test="$overwrite-objects">
              <xsl:attribute name="overwrite-objects"><xsl:value-of select="$overwrite-objects"/></xsl:attribute>
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
              <xsl:choose>
                <xsl:when test="$file">
                  <xsl:value-of select="java:invoke($read-data-from-file-fn,$file,true())"/>
                </xsl:when>
                <xsl:when test="$import-input">
                  <xsl:value-of select="$import-input"/>
                </xsl:when>
                <xsl:otherwise>
                  <xsl:message terminate="yes">Missing import input</xsl:message>
                </xsl:otherwise>
              </xsl:choose>
            </mgmt:input-file>
            <xsl:if test="$objects-file">
              <xsl:copy-of select="document($objects-file)"/>
            </xsl:if>
            <xsl:if test="$files-file">
              <xsl:copy-of select="document($files-file)"/>
            </xsl:if>
            <xsl:if test="$deployment-policy-file">
              <xsl:copy-of select="document($deployment-policy-file)"/>
            </xsl:if>
            <xsl:if test="$deployment-policy-variables-file">
              <xsl:copy-of select="document($deployment-policy-variables-file)"/>
            </xsl:if>
          </mgmt:do-import>
        </mgmt:request>
      </soap:Body>
    </soap:Envelope>
  </xsl:template>
</xsl:stylesheet>
