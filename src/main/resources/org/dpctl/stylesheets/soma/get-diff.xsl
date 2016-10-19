<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet
    version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/"
    xmlns:mgmt="http://www.datapower.com/schemas/management"
    xmlns:dpctl="http://www.dpctl.org/"
    dpctl:category="SOMA"
    dpctl:doc="Config diff"
    exclude-result-prefixes="xsl dpctl">
  <xsl:output method="xml" encoding="UTF-8" omit-xml-declaration="no" media-type="text/xml"/>

  <xsl:param name="domain" dpctl:doc="DataPower domain"/>
  <xsl:param name="from-export-file" dpctl:doc="Export file"/>
  <xsl:param name="from-backup-file" dpctl:doc="Backup file"/>
  <xsl:param name="from-object-class" dpctl:doc="Object class"/>
  <xsl:param name="from-object-name" dpctl:doc="Object name"/>
  <xsl:param name="from-recursive" dpctl:doc="Recursive [true|false]"/>
  <xsl:param name="from-persisted" dpctl:doc="Persisted [true|false]"/>
  <xsl:param name="to-export-file" dpctl:doc="Export file"/>
  <xsl:param name="to-backup-file" dpctl:doc="Backup file"/>
  <xsl:param name="to-object-class" dpctl:doc="Object class"/>
  <xsl:param name="to-object-name" dpctl:doc="Object name"/>
  <xsl:param name="to-recursive" dpctl:doc="Recursive [true|false]"/>
  <xsl:param name="to-persisted" dpctl:doc="Persisted [true|false]"/>

  <xsl:template match="/">
    <soap:Envelope>
      <soap:Body>
        <xsl:call-template name="run-task">
          <xsl:with-param name="url" select="$url"/>
          <xsl:with-param name="username" select="$username"/>
          <xsl:with-param name="password" select="$password"/>
          <xsl:with-param name="raw" select="$raw"/>
        </xsl:call-template>
      </soap:Body>
    </soap:Envelope>
  </xsl:template>

  <xsl:template name="build-request">
    <xsl:call-template name="build-get-diff-request">
      <xsl:with-param name="domain" select="$domain"/>
      <xsl:with-param name="from-export-file" select="$from-export-file"/>
      <xsl:with-param name="from-backup-file" select="$from-backup-file"/>
      <xsl:with-param name="from-object-class" select="$from-object-class"/>
      <xsl:with-param name="from-object-name" select="$from-object-name"/>
      <xsl:with-param name="from-recursive" select="$from-recursive"/>
      <xsl:with-param name="from-persisted" select="$from-persisted"/>
      <xsl:with-param name="to-export-file" select="$to-export-file"/>
      <xsl:with-param name="to-backup-file" select="$to-backup-file"/>
      <xsl:with-param name="to-object-class" select="$to-object-class"/>
      <xsl:with-param name="to-object-name" select="$to-object-name"/>
      <xsl:with-param name="to-recursive" select="$to-recursive"/>
      <xsl:with-param name="to-persisted" select="$to-persisted"/>
    </xsl:call-template>
  </xsl:template>

  <xsl:template name="build-get-diff-request">
    <xsl:param name="domain"/>
    <xsl:param name="from-export-file"/>
    <xsl:param name="from-backup-file"/>
    <xsl:param name="from-object-class"/>
    <xsl:param name="from-object-name"/>
    <xsl:param name="from-recursive"/>
    <xsl:param name="from-persisted"/>
    <xsl:param name="to-export-file"/>
    <xsl:param name="to-backup-file"/>
    <xsl:param name="to-object-class"/>
    <xsl:param name="to-object-name"/>
    <xsl:param name="to-recursive"/>
    <xsl:param name="to-persisted"/>

    <mgmt:request>
      <xsl:if test="$domain">
        <xsl:attribute name="domain"><xsl:value-of select="$domain"/></xsl:attribute>
      </xsl:if>
      <mgmt:get-diff>
        <xsl:choose>
          <xsl:when test="($from-export-file and $from-backup-file) or ($from-export-file and ($from-object-class or $from-object-name)) or ($from-backup-file and ($from-object-class or $from-object-name))">
            <xsl:message terminate="yes">The 'from' input must be a backup file, an export file or object class/name.</xsl:message>
          </xsl:when>
          <xsl:when test="($from-export-file and $from-backup-file) or ($from-export-file and ($from-object-class or $from-object-name)) or ($from-backup-file and ($from-object-class or $from-object-name))">
            <xsl:message terminate="yes">The 'to' input must be a backup file, an export file or object class/name.</xsl:message>
          </xsl:when>
          <xsl:when test="($from-object-class=$to-object-class) and ($from-object-name=$to-object-name) and ($from-recursive=$to-recursive)">
            <mgmt:object>
              <xsl:if test="$from-object-class">
                <xsl:attribute name="class"><xsl:value-of select="$from-object-class"/></xsl:attribute>
              </xsl:if>
              <xsl:if test="$from-object-name">
                <xsl:attribute name="name"><xsl:value-of select="$from-object-name"/></xsl:attribute>
              </xsl:if>
              <xsl:if test="$from-recursive">
                <xsl:attribute name="recursive"><xsl:value-of select="$from-recursive"/></xsl:attribute>
              </xsl:if>
              <xsl:if test="$from-persisted">
                <xsl:attribute name="from-persisted"><xsl:value-of select="$from-persisted"/></xsl:attribute>
              </xsl:if>
              <xsl:if test="$to-persisted">
                <xsl:attribute name="to-persisted"><xsl:value-of select="$to-persisted"/></xsl:attribute>
              </xsl:if>
            </mgmt:object>
          </xsl:when>
          <xsl:otherwise>
            <mgmt:from>
              <xsl:if test="$from-export-file">
                <mgmt:export>
                  <xsl:value-of select="java:invoke($base64-encode-data-from-file-fn,$from-export-file)"/>
                </mgmt:export>
              </xsl:if>
              <xsl:if test="$from-backup-file">
                <mgmt:backup>
                  <xsl:value-of select="java:invoke($base64-encode-data-from-file-fn,$from-backup-file)"/>
                </mgmt:backup>
              </xsl:if>
              <xsl:if test="$from-object-class or $from-object-name">
                <mgmt:object>
                  <xsl:if test="$from-object-class">
                    <xsl:attribute name="class"><xsl:value-of select="$from-object-class"/></xsl:attribute>
                  </xsl:if>
                  <xsl:if test="$from-object-name">
                    <xsl:attribute name="name"><xsl:value-of select="$from-object-name"/></xsl:attribute>
                  </xsl:if>
                  <xsl:if test="$from-recursive">
                    <xsl:attribute name="recursive"><xsl:value-of select="$from-recursive"/></xsl:attribute>
                  </xsl:if>
                  <xsl:if test="$from-persisted">
                    <xsl:attribute name="persisted"><xsl:value-of select="$from-persisted"/></xsl:attribute>
                  </xsl:if>
                </mgmt:object>
              </xsl:if>
            </mgmt:from>
            <mgmt:to>
              <xsl:if test="$to-export-file">
                <mgmt:export>
                  <xsl:value-of select="java:invoke($base64-encode-data-from-file-fn,$to-export-file)"/>
                </mgmt:export>
              </xsl:if>
              <xsl:if test="$to-backup-file">
                <mgmt:backup>
                  <xsl:value-of select="java:invoke($base64-encode-data-from-file-fn,$to-backup-file)"/>
                </mgmt:backup>
              </xsl:if>
              <xsl:if test="$to-object-class or $to-object-name">
                <mgmt:object>
                  <xsl:if test="$to-object-class">
                    <xsl:attribute name="class"><xsl:value-of select="$to-object-class"/></xsl:attribute>
                  </xsl:if>
                  <xsl:if test="$to-object-name">
                    <xsl:attribute name="name"><xsl:value-of select="$to-object-name"/></xsl:attribute>
                  </xsl:if>
                  <xsl:if test="$to-recursive">
                    <xsl:attribute name="recursive"><xsl:value-of select="$to-recursive"/></xsl:attribute>
                  </xsl:if>
                  <xsl:if test="$to-persisted">
                    <xsl:attribute name="persisted"><xsl:value-of select="$to-persisted"/></xsl:attribute>
                  </xsl:if>
                </mgmt:object>
              </xsl:if>
            </mgmt:to>
          </xsl:otherwise>
        </xsl:choose>
      </mgmt:get-diff>
    </mgmt:request>
  </xsl:template>
</xsl:stylesheet>
