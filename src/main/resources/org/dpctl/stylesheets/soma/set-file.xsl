<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet
    version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/"
    xmlns:mgmt="http://www.datapower.com/schemas/management"
    xmlns:dpctl="http://www.dpctl.org/"
    xmlns:java="http://xml.apache.org/xslt/java"
    dpctl:category="SOMA"
    dpctl:doc="Upload file"
    exclude-result-prefixes="xsl dpctl java">
  <xsl:output method="xml" encoding="UTF-8" omit-xml-declaration="no" media-type="text/xml"/>

  <xsl:param name="domain" dpctl:doc="DataPower domain" dpctl:required="true"/>
  <xsl:param name="name" dpctl:doc="Remote file name" dpctl:required="true"/>
  <xsl:param name="file" dpctl:doc="Local file" dpctl:required="true"/>

  <xsl:variable name="read-data-from-file-fn" select="java:clojure.java.api.Clojure.var('org.dpctl.util.io','read-data-from-file')"/>

  <xsl:template match="/">
    <soap:Envelope>
      <soap:Body>
        <mgmt:request domain="{$domain}">
          <mgmt:set-file name="{$name}">
            <xsl:value-of select="java:invoke($read-data-from-file-fn,$file,true())"/>
          </mgmt:set-file>
        </mgmt:request>
      </soap:Body>
    </soap:Envelope>
  </xsl:template>
</xsl:stylesheet>
