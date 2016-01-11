<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:output method="html" />

  <xsl:template match="/">
    <xsl:variable name="model" select="directoryListingModel" />
    <xsl:variable name="title" select="$model/title" />
    <xsl:text disable-output-escaping='yes'>&lt;!doctype html&gt;
</xsl:text><html>
      <head>
        <title><xsl:value-of select="$title" /></title>
      </head>
      <body>
        <h2><xsl:value-of select="$title" /></h2>
        <hr />
        <ul>
          <xsl:for-each select="$model/files/entry">
            <xsl:variable name="file" select="current()" />
            <li>
            <xsl:element name="a">
              <xsl:attribute name="href" select="concat($model/baseRef, $file/key)" />
              <xsl:value-of select="$file/value" />
            </xsl:element>
            </li>
          </xsl:for-each>
        </ul>
        <hr />
        <xsl:for-each select="$model/downloads/entry">
          <xsl:variable name="download" select="current()" />
          <xsl:element name="a">
            <xsl:attribute name="href" select="$download/key" />
            <xsl:value-of select="$download/value" />
          </xsl:element>
        </xsl:for-each>
        <em>
          <xsl:choose>
            <xsl:when test="$model/serverLine">
              <xsl:value-of select="$model/serverLine" />
            </xsl:when>
            <xsl:otherwise>
              <xsl:text>ThriftEE</xsl:text>
            </xsl:otherwise>
          </xsl:choose>
        </em>
      </body>
    </html>
  </xsl:template>

</xsl:stylesheet>