<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet
    version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/"
    xmlns:xslt="http://xml.apache.org/xslt"
    xmlns:mgmt="http://www.datapower.com/schemas/management"
    xmlns:java="http://xml.apache.org/xslt/java"
    exclude-result-prefixes="soap xslt java">
  <xsl:output method="xml" encoding="UTF-8" omit-xml-declaration="yes" media-type="text/xml" indent="yes" xslt:indent-amount="2"/>
  <xsl:param name="output-format" select="'txt'"/>
  <xsl:param name="file"/>

  <xsl:strip-space elements="*"/>

  <xsl:variable name="new-line" select="'&#x0A;'"/>
  <xsl:variable name="save-data-to-file-fn" select="java:clojure.java.api.Clojure.var('org.dpctl.util.io','save-data-to-file')"/>

  <xsl:template match="/soap:Envelope/soap:Body">
    <xsl:choose>
      <xsl:when test="$output-format='txt'">
        <xsl:apply-templates select="node()|@*" mode="dpctl-text-output"/>
      </xsl:when>
      <xsl:when test="$output-format='xml'">
        <xsl:copy-of select="*"/>
      </xsl:when>
      <xsl:when test="$output-format='raw'">
        <xsl:copy-of select="/"/>
      </xsl:when>
    </xsl:choose>
    <xsl:value-of select="$new-line"/>
  </xsl:template>

  <xsl:template match="mgmt:response/mgmt:file" mode="dpctl-text-output">
    <xsl:param name="indent" select="''"/>

    <xsl:choose>
      <xsl:when test="$file">
        <xsl:value-of select="concat($new-line,$indent,local-name(),': ',$file)"/>
        <xsl:value-of select="java:invoke($save-data-to-file-fn,$file,string(.),true())"/>
      </xsl:when>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="node()" mode="dpctl-text-output">
    <xsl:param name="indent" select="''"/>

    <xsl:value-of select="concat($new-line,$indent,local-name(),':')"/>

    <xsl:apply-templates select="node()|@*" mode="dpctl-text-output">
      <xsl:with-param name="indent" select="concat($indent,'  ')"/>
    </xsl:apply-templates>
  </xsl:template>

  <xsl:template match="text()" mode="dpctl-text-output">
    <xsl:param name="indent" select="''"/>

    <xsl:variable name="txt" select="normalize-space(.)"/>
    <xsl:choose>
      <xsl:when test="$txt=''"/>
      <xsl:when test="preceding-sibling::text() or position()=1">
        <xsl:value-of select="concat(' ',$txt)"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="concat($new-line,$indent,'- ',$txt)"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="@*" mode="dpctl-text-output">
    <xsl:param name="indent" select="''"/>

    <xsl:value-of select="concat($new-line,$indent,'@',local-name(),' = ',.)"/>
  </xsl:template>
</xsl:stylesheet>
