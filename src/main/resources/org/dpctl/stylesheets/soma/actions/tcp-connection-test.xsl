<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet
    version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/"
    xmlns:mgmt="http://www.datapower.com/schemas/management"
    xmlns:dpctl="http://www.dpctl.org/"
    dpctl:category="SOMA Actions"
    dpctl:doc="TCP connection test action"
    exclude-result-prefixes="xsl dpctl">
  <xsl:output method="xml" encoding="UTF-8" omit-xml-declaration="no" media-type="text/xml"/>

  <xsl:param name="domain" dpctl:doc="DataPower domain" dpctl:required="true"/>
  <xsl:param name="remote-host" dpctl:doc="Remote host" dpctl:required="true"/>
  <xsl:param name="remote-port" dpctl:doc="Remote port" dpctl:required="true"/>
  <xsl:param name="ip-version" dpctl:doc="IP version [-4|-6|default]" dpctl:default="default"/>

  <xsl:template match="/">
    <soap:Envelope>
      <soap:Body>
        <mgmt:request domain="{$domain}">
          <mgmt:do-action>
            <TCPConnectionTest>
              <RemoteHost><xsl:value-of select="$remote-host"/></RemoteHost>
              <RemotePort><xsl:value-of select="$remote-port"/></RemotePort>
              <xsl:if test="$ip-version">
                <useIPv><xsl:value-of select="$ip-version"/></useIPv>
              </xsl:if>
            </TCPConnectionTest>
          </mgmt:do-action>
        </mgmt:request>
      </soap:Body>
    </soap:Envelope>
  </xsl:template>
</xsl:stylesheet>
