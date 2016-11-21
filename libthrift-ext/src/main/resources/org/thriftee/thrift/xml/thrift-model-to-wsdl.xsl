<xsl:stylesheet version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:idl="http://thrift.apache.org/xml/idl"
    xmlns:xsd="http://www.w3.org/2001/XMLSchema"
    xmlns:wsdl="http://schemas.xmlsoap.org/wsdl/"
    xmlns:soap="http://schemas.xmlsoap.org/wsdl/soap/"
    exclude-result-prefixes="xsl idl">

  <!-- TODO: add support for parent services -->
  <xsl:import href="thrift-model-to-xsd.xsl" />

  <xsl:output method="xml" omit-xml-declaration="yes" indent="yes" />
  <xsl:strip-space elements="*" />

  <xsl:param name="service_module" />
  <xsl:param name="service_name" />

  <xsl:variable name="idl" select="/idl:idl" />
  <xsl:variable name="doc" select="$idl/idl:document[@name=$service_module]" />
  <xsl:variable name="svc" select="$doc/idl:service[@name=$service_name]" />
  <xsl:variable name="tns" select="$svc/namespace::*[name()='tns']" />

  <xsl:template match="/">
    <xsl:if test="not($svc)">
      <xsl:message terminate="yes">Invalid service: {$service_module}.{$service_name}</xsl:message>
    </xsl:if>
    <wsdl:definitions targetNamespace="{string($tns)}">
      <xsl:copy-of select="$tns"/>
      <xsl:copy-of select="$doc/namespace::*[name()=$doc/@name]" />
      <xsl:apply-templates mode="copy-included-namespaces" select="$doc/idl:include" />
      <wsdl:types>
        <xsd:schema targetNamespace="{string($tns)}" elementFormDefault="unqualified">
          <xsl:apply-templates mode="process-includes-as-imports" select="$doc/idl:include" />
          <xsd:import schemaLocation="{$doc/@name}.xsd" namespace="{string($doc/@targetNamespace)}" />
          <xsl:apply-templates select="$svc/*" mode="wsdl-method-types" />
        </xsd:schema>
      </wsdl:types>
      <xsl:apply-templates mode="wsdl-method-messages" select="$svc/*" />
      <wsdl:portType name="{$service_name}Port">
        <xsl:apply-templates mode="wsdl-operations" select="$svc/*" />
      </wsdl:portType>
      <wsdl:binding name="{$svc/@name}Binding" type="{name($tns)}:{$svc/@name}Port">
        <soap:binding style="document" transport="http://schemas.xmlsoap.org/soap/http" />
        <xsl:apply-templates mode="wsdl-binding-operations" select="$svc/*" />
      </wsdl:binding>
      <wsdl:service name="{$svc/@name}">
        <wsdl:port name="{$svc/@name}Port" binding="{name($tns)}:{$svc/@name}Binding">
          <soap:address location="" />
        </wsdl:port>
      </wsdl:service>
    </wsdl:definitions>
  </xsl:template>

  <xsl:template match="idl:include" mode="copy-included-namespaces">
    <xsl:variable name="name" select="@name" />
    <xsl:copy-of select="$idl/idl:document[@name=$name]/namespace::*[name()=$name]" />
  </xsl:template>

  <xsl:template match="idl:include" mode="process-includes-as-imports">
    <xsl:variable name="name" select="@name" />
    <xsd:import schemaLocation="{$name}.xsd" namespace="{string($idl/idl:document[@name=$name]/namespace::*[name()=$name])}" />
  </xsl:template>

  <xsl:template mode="wsdl-method-types" match="idl:method">
    <xsl:variable name="service" select="ancestor::idl:service[1]" />
    <xsl:variable name="method" select="current()" />
    <xsd:element name="{$method/@name}Request">
      <xsl:if test="$method/idl:arg">
        <xsd:complexType>
          <xsd:sequence>
            <xsl:apply-templates mode="wsdl-method-args" select="$method/idl:arg" />
          </xsd:sequence>
        </xsd:complexType>
      </xsl:if>
    </xsd:element>
    <xsl:if test="not(string($method/@oneway) = 'true')">
      <xsl:apply-templates mode="element-for-type" select="$method/idl:returns">
        <xsl:with-param name="element-name" select="concat($method/@name, 'Response')" />
        <xsl:with-param name="skip-min-max-occurs" select="true()" />
      </xsl:apply-templates>
    </xsl:if>
  </xsl:template>

  <xsl:template mode="wsdl-method-messages" match="idl:method">
    <xsl:variable name="service" select="ancestor::idl:service[1]" />
    <xsl:variable name="method" select="current()" />
    <wsdl:message name="{$method/@name}Input">
      <wsdl:part name="parameters" element="{name($tns)}:{$method/@name}Request" />
    </wsdl:message>
    <xsl:if test="not(string($method/@oneway) = 'true')">
      <wsdl:message name="{$method/@name}Output">
        <wsdl:part name="parameters" element="{name($tns)}:{$method/@name}Response" />
      </wsdl:message>
    </xsl:if>
  </xsl:template>

  <xsl:template mode="wsdl-method-args" match="idl:arg">
    <xsl:apply-templates mode="element-for-type" select="current()">
      <xsl:with-param name="element-name" select="@name" />
    </xsl:apply-templates>
  </xsl:template>

  <xsl:template mode="arg-schema" match="*">
    <xsl:apply-templates mode="element-for-type" select="current()">
      <xsl:with-param name="element-name" select="@name" />
    </xsl:apply-templates>
  </xsl:template>

  <xsl:template mode="wsdl-operations" match="idl:method">
    <xsl:variable name="service" select="ancestor::idl:service[1]" />
    <xsl:variable name="method" select="current()" />
    <wsdl:operation name="{$method/@name}">
      <wsdl:input message="{name($tns)}:{$method/@name}Input" />
      <xsl:if test="not(string($method/@oneway) = 'true')">
        <wsdl:output message="{name($tns)}:{$method/@name}Output" />
      </xsl:if>
    </wsdl:operation>
  </xsl:template>

  <xsl:template mode="wsdl-binding-operations" match="idl:method">
    <xsl:variable name="service" select="ancestor::idl:service[1]" />
    <xsl:variable name="method" select="current()" />
    <wsdl:operation name="{$method/@name}">
      <soap:operation soapAction="" />
      <wsdl:input><soap:body use="literal" /></wsdl:input>
      <xsl:if test="not(string($method/@oneway) = 'true')">
        <wsdl:output><soap:body use="literal" /></wsdl:output>
      </xsl:if>
    </wsdl:operation>
  </xsl:template>

</xsl:stylesheet>