<xsl:stylesheet version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:idl="http://thrift.apache.org/xml/idl"
    xmlns:txp="http://thrift.apache.org/xml/protocol"
    xmlns:soap="http://www.w3.org/2001/12/soap-envelope"
    exclude-result-prefixes="xsl idl soap">

  <xsl:output method="xml" omit-xml-declaration="yes" indent="no" />

  <xsl:param name="schema" />
  <xsl:param name="verbose" select="false()" />
  <xsl:param name="root_module" select="document($schema)/idl:idl/idl:document[1]/@name" />

  <xsl:variable name="idl" select="document($schema)/idl:idl" />

  <xsl:variable name="txp_ns" select="'http://thrift.apache.org/xml/protocol'" />
  <xsl:variable name="include_names" select="true()" />
  <xsl:variable name="call" select="'call'" />
  <xsl:variable name="reply" select="'reply'" />
  <xsl:variable name="oneway" select="'oneway'" />
  <xsl:variable name="exception" select="'exception'" />
  <xsl:variable name="struct" select="'struct'" />
  <xsl:variable name="field" select="'field'" />
  <xsl:variable name="fname" select="'fname'" />
  <xsl:variable name="list" select="'list'" />
  <xsl:variable name="map" select="'map'" />
  <xsl:variable name="set" select="'set'" />
  <xsl:variable name="i64" select="'i64'" />
  <xsl:variable name="i32" select="'i32'" />
  <xsl:variable name="i16" select="'i16'" />
  <xsl:variable name="i8" select="'i8'" />
  <xsl:variable name="string" select="'string'" />
  <xsl:variable name="double" select="'double'" />
  <xsl:variable name="seqid_attr" select="'seqid'" />
  <xsl:variable name="name_attr" select="'name'" />

  <xsl:template match="/">
    <xsl:apply-templates mode="resolve" />
  </xsl:template>

  <xsl:template mode="resolve" match="soap:Envelope">
    <xsl:apply-templates mode="resolve-service" select="soap:Body/*[1]">
      <xsl:with-param name="metadata" select="current()/soap:Header/*[namespace-uri()=$txp_ns][1]" />
    </xsl:apply-templates>
  </xsl:template>

  <xsl:template mode="resolve" match="*" priority="-1">
    <xsl:variable name="data" select="current()" />
    <xsl:variable name="ns" select="namespace-uri()" />
    <xsl:variable name="doc" select="$idl/idl:document[@targetNamespace=$ns]" />
    <xsl:variable name="name" select="local-name()" />
    <xsl:variable name="typeinfo" select="$doc/idl:struct[@name=$name]  |
                                          $doc/idl:union[@name=$name]   |
                                          $doc/idl:typedef[@name=$name] |
                                          $doc/idl:enum[@name=$name]    " />
    <xsl:if test="not($typeinfo)">
      <xsl:message terminate="yes">could not find typeinfo for <xsl:value-of select="concat('{', $ns, '}', $name)" /></xsl:message>
    </xsl:if>
    <xsl:variable name="typename">
      <xsl:apply-templates mode="typename-for-id-typeinfo" select="$typeinfo" />
    </xsl:variable>
    <xsl:element name="{$typename}" namespace="{$txp_ns}">
      <xsl:apply-templates mode="transform-thrift-id-type" select="$typeinfo">
        <xsl:with-param name="data" select="$data/.." />
      </xsl:apply-templates>
    </xsl:element>
  </xsl:template>

  <xsl:template mode="add-seqid" match="*">
    <xsl:param name="metadata" select="current()" />
    <xsl:attribute name="{$seqid_attr}">
      <xsl:choose>
        <xsl:when test="$metadata/@seqid">
          <xsl:value-of select="$metadata/@seqid" />
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="'1'" />
        </xsl:otherwise>
      </xsl:choose>
    </xsl:attribute>
  </xsl:template>

  <xsl:template mode="resolve-service" match="soap:Fault[string(faultactor)='txp:application-exception']">
    <xsl:param name="metadata" />
    <xsl:param name="message" select="current()/detail/txp:TApplicationException" />
    <xsl:element name="{$exception}" namespace="{$txp_ns}">
      <xsl:attribute name="{$name_attr}" select="$metadata/@name" />
      <xsl:apply-templates mode="add-seqid" select="$metadata" />
      <xsl:element name="{$struct}" namespace="{$txp_ns}">
        <xsl:if test="$include_names">
          <xsl:attribute name="{$name_attr}" select="'TApplicationException'" />
        </xsl:if>
        <xsl:if test="$message/txp:message">
          <xsl:element name="{$string}" namespace="{$txp_ns}">
            <xsl:attribute name="{$field}" select="1" />
            <xsl:if test="$include_names">
              <xsl:attribute name="{$fname}" select="'message'" />
            </xsl:if>
            <xsl:value-of select="$message/txp:message" />
          </xsl:element>
        </xsl:if>
        <xsl:if test="$message/txp:type">
          <xsl:element name="{$i32}" namespace="{$txp_ns}">
            <xsl:attribute name="{$field}" select="2" />
            <xsl:if test="$include_names">
              <xsl:attribute name="{$fname}" select="'type'" />
            </xsl:if>
            <xsl:value-of select="$message/txp:type" />
          </xsl:element>
        </xsl:if>
      </xsl:element>
    </xsl:element>
  </xsl:template>

  <xsl:template mode="resolve-service" match="soap:Fault[string(faultactor)!='txp:application-exception']">
    <xsl:param name="metadata" />
    <xsl:param name="message" select="current()/detail/*" />
    <xsl:variable name="ns" select="namespace-uri($message)" />
    <xsl:variable name="name" select="local-name($message)" />
    <xsl:variable name="methodname" select="substring($name, 0, string-length($name)-7)" />
    <xsl:variable name="svc" select="$idl/idl:document/idl:service[@targetNamespace=$ns]" />
    <xsl:variable name="typeinfo" select="$svc/idl:method[@name=$methodname]" />
    <xsl:if test="not($typeinfo)">
      <xsl:message terminate="yes">could not find typeinfo for <xsl:value-of select="concat('{', $ns, '}', $methodname)" /></xsl:message>
    </xsl:if>
    <xsl:element name="{$reply}" namespace="{$txp_ns}">
      <xsl:attribute name="{$name_attr}" select="$methodname" />
      <xsl:apply-templates mode="add-seqid" select="$metadata" />
      <xsl:element name="{$struct}" namespace="{$txp_ns}">
        <xsl:if test="$include_names">
          <xsl:attribute name="{$name_attr}" select="concat($methodname, '_result')" />
        </xsl:if>
        <xsl:apply-templates mode="transform-fields" select="$message/*[1]">
          <xsl:with-param name="typeinfo" select="$typeinfo" />
          <xsl:with-param name="fieldtype" select="'throws'" />
        </xsl:apply-templates>
      </xsl:element>
    </xsl:element>
  </xsl:template>

  <xsl:template mode="resolve-service" match="*[substring(local-name(), string-length(local-name())-6) = 'Request']">
    <xsl:param name="metadata" />
    <xsl:param name="message" select="current()" />
    <xsl:variable name="ns" select="namespace-uri()" />
    <xsl:variable name="svc" select="$idl/idl:document/idl:service[@targetNamespace=$ns]" />
    <xsl:variable name="name" select="local-name()" />
    <xsl:variable name="methodname" select="substring($name, 0, string-length($name)-6)" />
    <xsl:variable name="typeinfo" select="$svc/idl:method[@name=$methodname]" />
    <xsl:if test="not($typeinfo)">
      <xsl:message terminate="yes">could not find typeinfo for <xsl:value-of select="concat('{', $ns, '}', $methodname)" /></xsl:message>
    </xsl:if>
    <xsl:element name="{$call}" namespace="{$txp_ns}">
      <xsl:attribute name="{$name_attr}" select="$methodname" />
      <xsl:apply-templates mode="add-seqid" select="$metadata" />
      <xsl:element name="{$struct}" namespace="{$txp_ns}">
        <xsl:if test="$include_names">
          <xsl:attribute name="{$name_attr}" select="concat($methodname, '_args')" />
        </xsl:if>
        <xsl:apply-templates mode="transform-fields" select="$message/*">
          <xsl:with-param name="typeinfo" select="$typeinfo" />
          <xsl:with-param name="fieldtype" select="'arg'" />
        </xsl:apply-templates>
      </xsl:element>
    </xsl:element>
  </xsl:template>

  <xsl:template mode="resolve-service" match="*[substring(local-name(), string-length(local-name())-7) = 'Response']">
    <xsl:param name="metadata" />
    <xsl:param name="message" select="current()" />
    <xsl:variable name="data" select="*[1]" />
    <xsl:variable name="ns" select="namespace-uri()" />
    <xsl:variable name="svc" select="$idl/idl:document/idl:service[@targetNamespace=$ns]" />
    <xsl:variable name="name" select="local-name()" />
    <xsl:variable name="methodname" select="substring($name, 0, string-length($name)-7)" />
    <xsl:variable name="typeinfo" select="$svc/idl:method[@name=$methodname]" />
    <xsl:if test="not($typeinfo)">
      <xsl:message terminate="yes">could not find typeinfo for <xsl:value-of select="concat('{', $ns, '}', $methodname)" /></xsl:message>
    </xsl:if>
    <xsl:variable name="returntype" select="$typeinfo/idl:returns" />
    <xsl:variable name="returntypename">
      <xsl:apply-templates mode="typename-for-typeinfo" select="$returntype" />
    </xsl:variable>
    <xsl:element name="{$reply}" namespace="{$txp_ns}">
      <xsl:attribute name="{$name_attr}" select="$methodname" />
      <xsl:apply-templates mode="add-seqid" select="$metadata" />
      <xsl:element name="{$struct}" namespace="{$txp_ns}">
        <xsl:if test="$include_names">
          <xsl:attribute name="{$name_attr}" select="concat($methodname, '_result')" />
        </xsl:if>
        <xsl:apply-templates select="$returntype" mode="transform-field">
          <xsl:with-param name="data" select="$message" />
          <xsl:with-param name="field-id" select="0" />
          <xsl:with-param name="field-name" select="'success'" />
        </xsl:apply-templates>
      </xsl:element>
    </xsl:element>
  </xsl:template>

  <xsl:template mode="resolve-service" match="*" priority="-1">
    <tryToMatch />
  </xsl:template>

  <xsl:template mode="transform-thrift-type" match="*[@type='id']">
    <xsl:param name="data" />
    <xsl:variable name="typeinfo" select="current()" />
    <xsl:variable name="typeId" select="$typeinfo/@type-id" />
    <xsl:variable name="typeModule" select="$typeinfo/@type-module" />
    <xsl:variable name="module" select="$idl/idl:document[@name=$typeModule]" />
    <xsl:apply-templates 
        mode="transform-thrift-id-type" 
        select="$module/idl:struct[@name=$typeId]  |
                $module/idl:union[@name=$typeId]   |
                $module/idl:typedef[@name=$typeId] |
                $module/idl:enum[@name=$typeId]    |
                $module/idl:exception[@name=$typeId]">
      <xsl:with-param name="data" select="$data" />
    </xsl:apply-templates>
  </xsl:template>

  <xsl:template match="idl:struct|idl:exception" mode="transform-thrift-id-type">
    <xsl:param name="data" />
    <xsl:variable name="typeinfo" select="current()" />
    <xsl:if test="$include_names">
      <xsl:attribute name="name" select="$typeinfo/@name" />
    </xsl:if>
    <xsl:apply-templates mode="transform-fields" select="$data/*/*">
      <xsl:with-param name="typeinfo" select="$typeinfo" />
    </xsl:apply-templates>
  </xsl:template>

  <xsl:template match="idl:union" mode="transform-thrift-id-type">
    <xsl:param name="data" />
    <xsl:variable name="typeinfo" select="current()" />
    <xsl:if test="$include_names">
      <xsl:attribute name="name" select="$typeinfo/@name" />
    </xsl:if>
    <xsl:apply-templates mode="transform-fields" select="$data/*[1]/*">
      <xsl:with-param name="typeinfo" select="$typeinfo" />
    </xsl:apply-templates>
  </xsl:template>

  <xsl:template match="idl:enum" mode="transform-thrift-id-type">
    <xsl:param name="data" />
    <xsl:variable name="typeinfo" select="current()" />
    <xsl:value-of select="$typeinfo/idl:member[@name=$data]/@value" />
  </xsl:template>

  <xsl:template match="idl:typedef" mode="transform-thrift-id-type">
    <xsl:param name="data" />
    <xsl:variable name="typeinfo" select="current()" />
    <xsl:apply-templates mode="transform-thrift-type" select="$typeinfo">
      <xsl:with-param name="data" select="$data" />
    </xsl:apply-templates>
  </xsl:template>

  <xsl:template match="*" mode="transform-thrift-id-type" priority="-1">
    <xsl:variable name="typeinfo" select="current()" />
    <xsl:variable name="typeId" select="$typeinfo/@type-id" />
    <xsl:variable name="typeModule" select="$typeinfo/@type-module" />
    <xsl:message terminate="yes">Unhandled type ID '<xsl:value-of select="concat($typeModule, '.', $typeId)" />' for <xsl:copy-of select="$typeinfo" /></xsl:message>
  </xsl:template>

  <xsl:template mode="transform-fields" match="*">
    <xsl:param name="typeinfo" />
    <xsl:param name="fieldtype" select="'field'" />
    <xsl:variable name="data" select="current()" />
    <xsl:variable name="fieldinfo" select="$typeinfo/*[local-name()=$fieldtype and @name=local-name($data)]" />
    <xsl:apply-templates select="$fieldinfo" mode="transform-field">
      <xsl:with-param name="data" select="$data" />
    </xsl:apply-templates>
  </xsl:template>

  <xsl:template mode="transform-field" match="*">
    <xsl:param name="data" />
    <xsl:param name="typeinfo" select="current()" />
    <xsl:param name="field-name" select="$typeinfo/@name" />
    <xsl:param name="field-id" select="$typeinfo/@field-id" />
    <xsl:variable name="typename">
      <xsl:apply-templates mode="typename-for-typeinfo" select="$typeinfo" />
    </xsl:variable>
    <xsl:element name="{$typename}" namespace="{$txp_ns}">
      <xsl:attribute name="{$field}" select="$field-id" />  
      <xsl:if test="$include_names">
        <xsl:attribute name="{$fname}" select="$field-name" />
      </xsl:if>
      <xsl:apply-templates select="$typeinfo" mode="transform-thrift-type">
        <xsl:with-param name="data" select="$data" />
      </xsl:apply-templates>
    </xsl:element>
  </xsl:template>

  <xsl:template mode="transform-thrift-type" match="*[@type='set' or @type='list']">
    <xsl:param name="data" />
    <xsl:variable name="typeinfo" select="current()" />
    <xsl:variable name="valuetypename">
      <xsl:apply-templates mode="typename-for-typeinfo" select="$typeinfo/idl:elemType" />
    </xsl:variable>
    <xsl:attribute name="size" select="count($data/*)" />
    <xsl:attribute name="value" select="$valuetypename" />
    <xsl:apply-templates mode="transform-container-entry" select="$data/*">
      <xsl:with-param name="typeinfo" select="$typeinfo" />
      <xsl:with-param name="typename" select="$valuetypename" />
    </xsl:apply-templates>
  </xsl:template>

  <xsl:template mode="transform-container-entry" match="*">
    <xsl:param name="typeinfo" />
    <xsl:param name="typename" />
    <xsl:variable name="data" select="current()" />
    <xsl:element name="{$typename}" namespace="{$txp_ns}">
      <xsl:apply-templates mode="transform-thrift-type" select="$typeinfo/idl:elemType">
        <xsl:with-param name="data" select="$data" />
      </xsl:apply-templates>
    </xsl:element>
  </xsl:template>

  <xsl:template mode="transform-thrift-type" match="*[@type='map']">
    <xsl:param name="data" />
    <xsl:variable name="typeinfo" select="current()" />
    <xsl:variable name="keytypename">
      <xsl:apply-templates mode="typename-for-typeinfo" select="$typeinfo/idl:keyType" />
    </xsl:variable>
    <xsl:variable name="valuetypename">
      <xsl:apply-templates mode="typename-for-typeinfo" select="$typeinfo/idl:valueType" />
    </xsl:variable>
    <xsl:attribute name="size" select="count($data/*) div 2" />
    <xsl:attribute name="value" select="$valuetypename" />
    <xsl:attribute name="key" select="$keytypename" />
    <xsl:apply-templates mode="transform-map-entry" select="$data/*[position() mod 2 = 1]">
      <xsl:with-param name="typeinfo" select="$typeinfo"/>
      <xsl:with-param name="keytypename" select="$keytypename" />
      <xsl:with-param name="valuetypename" select="$valuetypename" />
    </xsl:apply-templates>
  </xsl:template>

  <xsl:template mode="transform-map-entry" match="*">
    <xsl:param name="data" select="current()" />
    <xsl:param name="typeinfo" />
    <xsl:param name="keytypename" />
    <xsl:param name="valuetypename" />
    <xsl:element name="{$keytypename}" namespace="{$txp_ns}">
      <xsl:apply-templates mode="transform-thrift-type" select="$typeinfo/idl:keyType">
        <xsl:with-param name="data" select="$data" />
      </xsl:apply-templates>
    </xsl:element>
    <xsl:element name="{$valuetypename}" namespace="{$txp_ns}">
      <xsl:apply-templates mode="transform-thrift-type" select="$typeinfo/idl:valueType">
        <xsl:with-param name="data" select="$data/following-sibling::*[1]" />
      </xsl:apply-templates>
    </xsl:element>
  </xsl:template>

  <xsl:template mode="transform-thrift-type" match="*" priority="-1">
    <xsl:param name="data" />
    <xsl:variable name="typeinfo" select="current()" />
    <xsl:value-of select="$data" />
  </xsl:template>

  <xsl:template mode="typename-for-typeinfo" match="*[@type='list']">
    <xsl:value-of select="$list" />
  </xsl:template>

  <xsl:template mode="typename-for-typeinfo" match="*[@type='map']">
    <xsl:value-of select="$map" />
  </xsl:template>

  <xsl:template mode="typename-for-typeinfo" match="*[@type='set']">
    <xsl:value-of select="$set" />
  </xsl:template>

  <xsl:template mode="typename-for-typeinfo" match="*[@type='i8']|*[@type='byte']">
    <xsl:value-of select="$i8" />
  </xsl:template>

  <xsl:template mode="typename-for-typeinfo" match="*[@type='i16']">
    <xsl:value-of select="$i16" />
  </xsl:template>

  <xsl:template mode="typename-for-typeinfo" match="*[@type='i32']">
    <xsl:value-of select="$i32" />
  </xsl:template>

  <xsl:template mode="typename-for-typeinfo" match="*[@type='i64']">
    <xsl:value-of select="$i64" />
  </xsl:template>

  <xsl:template mode="typename-for-typeinfo" match="*[@type='string']|*[@type='binary']">
    <xsl:value-of select="$string" />
  </xsl:template>

  <xsl:template mode="typename-for-typeinfo" match="*[@type='double']">
    <xsl:value-of select="$double" />
  </xsl:template>

  <xsl:template mode="typename-for-typeinfo" match="*" priority="-1">
    <xsl:message terminate="yes">Unknown type: <xsl:value-of select="@type" /></xsl:message>
  </xsl:template>

  <xsl:template mode="typename-for-typeinfo" match="*[@type='id']">
    <xsl:variable name="typeinfo" select="current()" />
    <xsl:variable name="referencedType" select="$idl/idl:document[@name=$typeinfo/@type-module]/*[@name=$typeinfo/@type-id]" />
    <xsl:apply-templates mode="typename-for-id-typeinfo" select="$referencedType" />
  </xsl:template>

  <xsl:template mode="typename-for-id-typeinfo" match="idl:struct|idl:union|idl:exception">
    <xsl:value-of select="$struct" />
  </xsl:template>

  <xsl:template mode="typename-for-id-typeinfo" match="idl:enum">
    <xsl:variable name="typeinfo" select="current()" />
    <xsl:value-of select="$i32" />
  </xsl:template>

  <xsl:template mode="typename-for-id-typeinfo" match="idl:typedef">
    <xsl:variable name="typeinfo" select="current()" />
    <xsl:apply-templates mode="typename-for-typeinfo" select="$typeinfo" />
  </xsl:template>

  <xsl:template mode="from-thrift-id-type" match="*" priority="-1">
    <xsl:message terminate="yes">unhandled id type: <xsl:copy-of select="current()" /></xsl:message>
  </xsl:template>

</xsl:stylesheet>