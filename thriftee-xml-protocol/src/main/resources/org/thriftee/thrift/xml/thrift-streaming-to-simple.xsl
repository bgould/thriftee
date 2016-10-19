<xsl:stylesheet version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:idl="http://thrift.apache.org/xml/idl"
    xmlns:txp="http://thriftee.org/xml/protocol"
    xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/"
    exclude-result-prefixes="xsl idl soap">

  <xsl:output method="xml" omit-xml-declaration="yes" indent="no" />

  <xsl:param name="schema" />
  <xsl:param name="service_name" select="null" />
  <xsl:param name="root_struct" select="null" />
  <xsl:param name="root_module" select="document($schema)/idl:idl/idl:document[1]/@name" />

  <xsl:variable name="idl" select="document($schema)/idl:idl" />

  <xsl:template match="/txp:r">
    <xsl:apply-templates mode="match-service" select="$idl/idl:document[$root_module]/idl:service[@name=$service_name]">
      <xsl:with-param name="call" select="current()" />
    </xsl:apply-templates>
  </xsl:template>

  <xsl:template match="/txp:s">
    <xsl:apply-templates mode="match-service" select="$idl/idl:document[$root_module]/idl:service[@name=$service_name]">
      <xsl:with-param name="call" select="current()" />
    </xsl:apply-templates>
  </xsl:template>

  <xsl:template match="/txp:t">
    <xsl:variable name="name" select="@n|@name" />
    <xsl:variable name="seqid" select="@q|@seqid" />
    <xsl:variable name="data" select="*[1]/*[1]" />
    <soap:Envelope>
      <soap:Header>
        <txp:exception name="{$name}" seqid="{$seqid}" />
      </soap:Header>
      <soap:Body>
        <soap:Fault>
          <faultcode>soap:Server</faultcode>
          <faultstring>An unexpected exception occurred.</faultstring>
          <faultactor>txp:application-exception</faultactor>
          <detail>
            <txp:TApplicationException>
            <xsl:if test="*/*[@field='1']|*/*[@i='1']">
              <txp:message><xsl:value-of select="*/*[@field='1']|*/*[@i='1']" /></txp:message>
            </xsl:if>
            <xsl:if test="*/*[@field='2']|*/*[@i='2']">
              <txp:type><xsl:value-of select="*/*[@field='2']|*/*[@i='2']" /></txp:type>
            </xsl:if>
            </txp:TApplicationException>
          </detail>
        </soap:Fault>
      </soap:Body>
    </soap:Envelope>
  </xsl:template>

  <xsl:template match="/txp:u">
    <xsl:apply-templates mode="match-service" select="$idl/idl:document[1]/idl:service[@name=$service_name]">
      <xsl:with-param name="call" select="current()" />
    </xsl:apply-templates>
  </xsl:template>

  <xsl:template match="/txp:m">
    <xsl:if test="not($root_struct)">
      <xsl:message terminate="yes">root_struct parameter must be specified if the root element is a struct</xsl:message>
    </xsl:if>
    <xsl:variable name="typeinfo" select="$idl/idl:document[@name=$root_module]/idl:struct[@name=$root_struct]" />
    <xsl:if test="not($typeinfo)">
      <xsl:message terminate="yes">no schema definition found for <xsl:value-of select="concat($root_module, '.', $root_struct)" /></xsl:message>
    </xsl:if>
    <xsl:apply-templates mode="transform-thrift-id-type" select="$typeinfo">
      <xsl:with-param name="data" select="current()" />
    </xsl:apply-templates>
  </xsl:template>

  <xsl:template match="/*" priority="-1">
    <xsl:message terminate="yes">Unhandled top level element: <xsl:value-of select="name(child::node())" /></xsl:message>
  </xsl:template>

  <xsl:template mode="match-service" match="idl:service">
    <xsl:param name="call" />
    <xsl:variable name="seqid" select="$call/@q|$call/@seqid" />
    <xsl:variable name="method" select="$call/@n|$call/@name" />
    <xsl:variable name="service" select="current()" />
    <xsl:variable name="methodinfo" select="$service/idl:method[@name=$method]" />
    <xsl:choose>
      <xsl:when test="$methodinfo">
        <xsl:apply-templates mode="match-service-method" select="$methodinfo">
          <xsl:with-param name="call" select="$call" />
          <xsl:with-param name="service" select="$service" />
        </xsl:apply-templates>
      </xsl:when>
      <xsl:otherwise>
        <xsl:apply-templates mode="match-parent-method" select="$service">
          <xsl:with-param name="call" select="$call" />
        </xsl:apply-templates>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template mode="match-service" match="*" priority="-100">
    <xsl:param name="call" />
    <xsl:message terminate="yes">Unable to locate service (or possibly a parent service) schema for (<xsl:value-of select="$service_name"/>): <xsl:copy-of select="current()" /></xsl:message>
  </xsl:template>

  <xsl:template mode="match-service-method" match="idl:method">
    <xsl:param name="call" />
    <xsl:apply-templates mode="dispatch-method" select="$call">
      <xsl:with-param name="method" select="current()" />
    </xsl:apply-templates>
  </xsl:template>

  <xsl:template mode="match-service-method" match="*" priority="-1">
    <xsl:param name="call" />
    <xsl:param name="service" />
    <xsl:apply-templates mode="match-parent-method" select="$service">
      <xsl:with-param name="call" select="$call" />
    </xsl:apply-templates>
  </xsl:template>

  <xsl:template mode="match-parent-method" match="idl:service[@parent-module!='' and @parent-id!='']">
    <xsl:param name="call" />
    <xsl:variable name="service" select="current()" />
    <xsl:apply-templates mode="match-service" select="$idl/idl:document[@name=$service/@parent-module]/idl:service[@name=$service/@parent-id]">
      <xsl:with-param name="call" select="$call" />
    </xsl:apply-templates>
  </xsl:template>

  <xsl:template mode="match-parent-method" match="*" priority="-100">
    <xsl:param name="call" />
    <xsl:message terminate="yes">Could not find method for: <xsl:copy-of select="$call" /></xsl:message>
  </xsl:template>

  <xsl:template mode="dispatch-method" match="/txp:r">
    <xsl:param name="method" />
    <xsl:variable name="seqid" select="@q|@seqid" />
    <xsl:variable name="data" select="*[1]" />
    <xsl:variable name="ns">
      <xsl:apply-templates mode="namespace-for-type" select="$method" />
    </xsl:variable>
    <soap:Envelope>
      <soap:Header>
        <txp:call method="{$method/@name}" seqid="{$seqid}" />
      </soap:Header>
      <soap:Body>
        <xsl:element name="{$method/@name}Request" namespace="{string($ns)}">
          <xsl:apply-templates mode="transform-fields" select="$data/*">
            <xsl:with-param name="fieldtype" select="'arg'" />
            <xsl:with-param name="typeinfo" select="$method" />
          </xsl:apply-templates>
        </xsl:element>
      </soap:Body>
    </soap:Envelope>
  </xsl:template>

  <xsl:template mode="dispatch-method" match="/txp:s">
    <xsl:param name="method" />
    <xsl:apply-templates mode="dispatch-reply" select="current()">
      <xsl:with-param name="method" select="$method" />
    </xsl:apply-templates>
  </xsl:template>

  <xsl:template mode="dispatch-reply" match="/txp:s[txp:m/*/@i='0']">
    <xsl:param name="method" />
    <xsl:variable name="seqid" select="@q|@seqid" />
    <xsl:variable name="data" select="*[1]/*[1]" />
    <xsl:variable name="ns">
      <xsl:apply-templates mode="namespace-for-type" select="$method" />
    </xsl:variable>
    <soap:Envelope>
      <soap:Header>
        <txp:reply method="{$method/@name}" seqid="{$seqid}" />
      </soap:Header>
      <soap:Body>
        <xsl:element name="{$method/@name}Response" namespace="{string($ns)}">
          <xsl:apply-templates select="$method/idl:returns" mode="transform-thrift-type">
            <xsl:with-param name="elemname" select="'success'" />
            <xsl:with-param name="data" select="$data" />
          </xsl:apply-templates>
        </xsl:element>
      </soap:Body>
    </soap:Envelope>
  </xsl:template>

  <xsl:template mode="dispatch-reply" match="*" priority="-100">
    <xsl:param name="method" />
    <xsl:variable name="call" select="current()" />
    <xsl:variable name="field" select="*[1]/*[1]/@i|*[1]/*[1]/@field"/>
    <xsl:apply-templates mode="dispatch-throws" select="$method/idl:throws[@field-id=$field]">
      <xsl:with-param name="call" select="$call" />
    </xsl:apply-templates>
  </xsl:template>

  <xsl:template mode="dispatch-throws" match="idl:throws">
    <xsl:param name="call" />
    <xsl:variable name="throws" select="current()" />
    <xsl:apply-templates mode="dispatch-exception" select="$idl/idl:document[@name=$throws/@type-module]/idl:exception[@name=$throws/@type-id]">
      <xsl:with-param name="call" select="$call" />
      <xsl:with-param name="throws" select="$throws" />
    </xsl:apply-templates>
  </xsl:template>

  <xsl:template mode="dispatch-throws" match="*" priority="-100">
    <xsl:param name="call" />
    <xsl:message terminate="yes">Could not find throws clause for: <xsl:copy-of select="$call" /></xsl:message>
  </xsl:template>

  <xsl:template mode="dispatch-exception" match="idl:exception">
    <xsl:param name="call" />
    <xsl:param name="throws" />
    <xsl:variable name="seqid" select="$call/@q|$call/@seqid" />
    <xsl:variable name="data" select="*[1]/*[1]" />
    <xsl:variable name="method" select="$throws/ancestor::idl:method[1]" />
    <xsl:variable name="service" select="$method/ancestor::idl:service[1]" />
    <xsl:variable name="ns">
      <xsl:apply-templates mode="namespace-for-type" select="$method" />
    </xsl:variable>
    <soap:Envelope>
      <soap:Header>
        <txp:exception method="{$method/@name}" seqid="{$seqid}" />
      </soap:Header>
      <soap:Body>
        <soap:Fault>
          <faultcode>soap:Server</faultcode>
          <faultstring>An application error occurred.</faultstring>
          <faultactor>txp:<xsl:value-of select="concat($service/@name,'.',$method/@name)" /></faultactor>
          <detail>
            <xsl:element name="{concat($method/@name, 'Response')}" namespace="{$ns}">
              <xsl:element name="{$throws/@name}" namespace="{$ns}">
                <xsl:apply-templates mode="transform-thrift-id-type" select="current()">
                  <xsl:with-param name="data" select="$call/*[1]/*[1]" />
                </xsl:apply-templates>
              </xsl:element>
            </xsl:element>
          </detail>
        </soap:Fault>
      </soap:Body>
    </soap:Envelope>
  </xsl:template>

  <xsl:template mode="dispatch-exception" match="*" priority="-100">
    <xsl:param name="call" />
    <xsl:message terminate="yes">Could not find exception clause for: <xsl:copy-of select="$call" /></xsl:message>
  </xsl:template>

  <xsl:template mode="application-exception" match="/txp:t">
    <xsl:param name="method" />
    <xsl:message terminate="yes">not implemented yet</xsl:message>
  </xsl:template>

  <xsl:template mode="dispatch-method" match="/txp:t">
    <xsl:param name="method" />
    <xsl:message terminate="yes">not implemented yet</xsl:message>
  </xsl:template>

  <xsl:template mode="namespace-for-type" match="*">
    <xsl:choose>
      <xsl:when test="ancestor::idl:service">
        <xsl:value-of select="string(ancestor::idl:service/@targetNamespace)" />
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="string(ancestor::idl:document/@targetNamespace)" />
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template mode="transform-thrift-type" match="*[@type='id']">
    <xsl:param name="data" />
    <xsl:param name="elemname" />
    <xsl:variable name="typeinfo" select="current()" />
    <xsl:variable name="typeId" select="$typeinfo/@type-id" />
    <xsl:variable name="typeModule" select="$typeinfo/@type-module" />
    <xsl:variable name="module" select="$idl/idl:document[@name=$typeModule]" />
    <xsl:apply-templates 
        mode="transform-thrift-id-type" 
        select="$module/idl:struct[@name=$typeId]  |
                $module/idl:union[@name=$typeId]   |
                $module/idl:typedef[@name=$typeId] |
                $module/idl:enum[@name=$typeId]    ">
      <xsl:with-param name="data" select="$data" />
      <xsl:with-param name="elemname" select="$elemname" />
    </xsl:apply-templates>
  </xsl:template>

  <xsl:template match="idl:struct|idl:exception" mode="transform-thrift-id-type">
    <xsl:param name="elemname" />
    <xsl:param name="data" />
    <xsl:variable name="typeinfo" select="current()" />
    <xsl:variable name="ns">
      <xsl:apply-templates mode="namespace-for-type" select="$typeinfo" />
    </xsl:variable>
    <xsl:element name="{$typeinfo/@name}" namespace="{string($ns)}">
      <xsl:apply-templates mode="transform-fields" select="$data/*">
        <xsl:with-param name="typeinfo" select="$typeinfo" />
      </xsl:apply-templates>
    </xsl:element>
  </xsl:template>

  <xsl:template match="idl:union" mode="transform-thrift-id-type">
    <xsl:param name="data" />
    <xsl:variable name="typeinfo" select="current()" />
    <xsl:variable name="ns">
      <xsl:apply-templates mode="namespace-for-type" select="$typeinfo" />
    </xsl:variable>
    <xsl:element name="{$typeinfo/@name}" namespace="{string($ns)}">
      <xsl:apply-templates mode="transform-fields" select="$data/*[1]">
        <xsl:with-param name="typeinfo" select="$typeinfo" />
      </xsl:apply-templates>
    </xsl:element>
  </xsl:template>

  <xsl:template match="*" mode="transform-fields">
    <xsl:param name="typeinfo" />
    <xsl:param name="fieldtype" select="'field'" />
    <xsl:variable name="data" select="current()" />
    <xsl:variable name="fieldinfo" select="$typeinfo/*[local-name()=$fieldtype and @field-id=($data/@i|$data/@field)[1]]" />
    <xsl:apply-templates select="$fieldinfo" mode="transform-field">
      <xsl:with-param name="elemname" select="$fieldinfo/@name" />
      <xsl:with-param name="data" select="$data" />
    </xsl:apply-templates>
  </xsl:template>

  <xsl:template mode="transform-field" match="*">
    <xsl:param name="elemname" />
    <xsl:param name="data" />
    <xsl:variable name="typeinfo" select="current()" />
    <xsl:variable name="ns">
      <xsl:apply-templates mode="namespace-for-type" select="$typeinfo" />
    </xsl:variable>
    <xsl:element name="{$elemname}" namespace="{string($ns)}">
      <xsl:apply-templates select="$typeinfo" mode="transform-thrift-type">
        <xsl:with-param name="elemname" select="$elemname" />
        <xsl:with-param name="data" select="$data" />
      </xsl:apply-templates>
    </xsl:element>
  </xsl:template>

  <xsl:template match="*[local-name()='enum']" mode="transform-thrift-id-type">
    <xsl:param name="data" />
    <xsl:param name="elemname" />
    <xsl:variable name="typeinfo" select="current()" />
    <!-- <xsl:apply-templates mode="copy-namespace" select="$typeinfo" /> -->
    <xsl:value-of select="normalize-space(string($typeinfo/idl:member[@value=$data/text()]/@name))" />
  </xsl:template>

  <xsl:template match="*[local-name()='typedef']" mode="transform-thrift-id-type">
    <xsl:param name="data" />
    <xsl:param name="elemname" />
    <xsl:variable name="typeinfo" select="current()" />
    <xsl:apply-templates mode="transform-thrift-type" select="$typeinfo">
      <xsl:with-param name="elemname" select="$elemname" />
      <xsl:with-param name="data" select="$data" />
    </xsl:apply-templates>
  </xsl:template>

  <xsl:template match="*" mode="transform-thrift-id-type" priority="-1">
    <xsl:variable name="typeinfo" select="current()" />
    <xsl:variable name="typeId" select="$typeinfo/@type-id" />
    <xsl:variable name="typeModule" select="$typeinfo/@type-module" />
    <xsl:message terminate="yes">Unhandled type ID '<xsl:value-of select="concat($typeModule, '.', $typeId)" />' for <xsl:copy-of select="$typeinfo" /></xsl:message>
  </xsl:template>

  <xsl:template mode="transform-thrift-type" match="*[@type='set' or @type='list']">
    <xsl:param name="data" />
    <xsl:param name="elemname" />
    <xsl:variable name="typeinfo" select="current()" />
    <xsl:apply-templates mode="transform-container-entry" select="$data/*">
      <xsl:with-param name="typeinfo" select="$typeinfo" />
    </xsl:apply-templates>
  </xsl:template>

  <xsl:template mode="transform-container-entry" match="*">
    <xsl:param name="data" select="current()" />
    <xsl:param name="typeinfo" />
    <xsl:variable name="ns">
      <xsl:apply-templates mode="namespace-for-type" select="$typeinfo" />
    </xsl:variable>
    <xsl:element name="entry" namespace="{string($ns)}">
      <xsl:apply-templates mode="transform-thrift-type" select="$typeinfo/idl:elemType">
        <xsl:with-param name="elemname" select="'entry'" />
        <xsl:with-param name="data" select="$data" />
      </xsl:apply-templates>
    </xsl:element>
  </xsl:template>

  <xsl:template mode="transform-thrift-type" match="*[@type='map']">
    <xsl:param name="data" />
    <xsl:param name="elemname" />
    <xsl:variable name="typeinfo" select="current()" />
    <xsl:apply-templates mode="transform-map-entry" select="$data/*[position() mod 2 = 1]">
      <xsl:with-param name="typeinfo" select="$typeinfo"/>
    </xsl:apply-templates>
  </xsl:template>

  <xsl:template mode="transform-map-entry" match="*">
    <xsl:param name="data" select="current()" />
    <xsl:param name="typeinfo" />
    <xsl:variable name="ns">
      <xsl:apply-templates mode="namespace-for-type" select="$typeinfo" />
    </xsl:variable>
    <xsl:element name="key" namespace="{string($ns)}">
      <xsl:apply-templates mode="transform-thrift-type" select="$typeinfo/idl:keyType">
        <xsl:with-param name="elemname" select="'key'" />
        <xsl:with-param name="data" select="$data" />
      </xsl:apply-templates>
    </xsl:element>
    <xsl:element name="value" namespace="{string($ns)}">
      <xsl:apply-templates mode="transform-thrift-type" select="$typeinfo/idl:valueType">
        <xsl:with-param name="elemname" select="'value'" />
        <xsl:with-param name="data" select="$data/following-sibling::*[1]" />
      </xsl:apply-templates>
    </xsl:element>
  </xsl:template>

  <xsl:template mode="transform-thrift-type" match="*[@type='bool']">
    <xsl:param name="data" />
    <xsl:variable name="typeinfo" select="current()" />
    <xsl:choose>
      <xsl:when test="string($data) = '1'">true</xsl:when>
      <xsl:otherwise>false</xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template mode="transform-thrift-type" match="*" priority="-1">
    <xsl:param name="data" />
    <xsl:variable name="typeinfo" select="current()" />
    <xsl:value-of select="$data" />
  </xsl:template>

</xsl:stylesheet>