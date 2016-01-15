<xsl:stylesheet version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:soap="http://schemas.xmlsoap.org/wsdl/soap/">

  <xsl:param name="wsdl_location" select="''" />

  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="soap:address">
    <soap:address location="{$wsdl_location}" />
  </xsl:template>

</xsl:stylesheet>