<xsl:stylesheet version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:idl="http://thrift.apache.org/xml/idl"
    xmlns:txp="http://thrift.apache.org/xml/protocol"
    xmlns:xsd="http://www.w3.org/2001/XMLSchema"
    exclude-result-prefixes="xsl idl txp">

  <xsl:output method="xml" omit-xml-declaration="yes" indent="yes" />
  <xsl:strip-space elements="*"/>

  <xsl:param name="root_module" select="/idl:idl/idl:document[1]/@name" />

  <xsl:variable name="idl" select="/idl:idl" />
  <xsl:variable name="doc" select="$idl/idl:document[@name=$root_module]" />
  <xsl:variable name="tns" select="$doc/namespace::*[name()=$root_module]" />

  <xsl:template match="/">
    <xsd:schema targetNamespace="{string($tns)}" elementFormDefault="unqualified">
      <xsl:copy-of select="$tns"/>
      <xsl:apply-templates mode="copy-included-namespaces" select="$doc/idl:include" />
      <xsl:apply-templates mode="process-includes-as-imports" select="$doc/idl:include" />
      <xsl:apply-templates mode="process-documentation" select="$doc" />
      <xsl:apply-templates select="$doc" />
      <xsd:complexType name="string">
        <xsd:simpleContent>
          <xsd:extension base="xsd:string">
            <xsd:attribute name="enc">
              <xsd:simpleType>
                <xsd:restriction base="xsd:string">
                  <xsd:enumeration value="base64" />
                </xsd:restriction>
              </xsd:simpleType>
            </xsd:attribute>
          </xsd:extension>
        </xsd:simpleContent>
      </xsd:complexType>
    </xsd:schema>
  </xsl:template>

  <xsl:template match="idl:include" mode="copy-included-namespaces">
    <xsl:variable name="name" select="@name" />
    <xsl:copy-of select="$idl/idl:document[@name=$name]/namespace::*[name()=$name]" />
  </xsl:template>

  <xsl:template match="idl:include" mode="process-includes-as-imports">
    <xsl:variable name="name" select="@name" />
    <xsd:import schemaLocation="{$name}.xsd" namespace="{string($idl/idl:document[@name=$name]/@targetNamespace)}" />
  </xsl:template>

  <xsl:template match="*[@doc]" mode="process-documentation">
    <xsd:annotation>
      <xsd:documentation>
        <xsl:value-of select="@doc" />
      </xsd:documentation>
    </xsd:annotation>
  </xsl:template>

  <xsl:template match="idl:const" />

  <xsl:template match="idl:typedef[@type='id']">
    <xsd:complexType name="{@name}">
      <xsl:apply-templates mode="process-documentation" select="current()" />
      <xsd:complexContent>
        <xsd:extension>
          <xsl:apply-templates mode="xsd-type" select="current()">
            <xsl:with-param name="type-attribute-name" select="'base'" />
          </xsl:apply-templates>
        </xsd:extension>
      </xsd:complexContent>
    </xsd:complexType>
  </xsl:template>

  <xsl:template match="idl:typedef[@type='set' or @type='list' or @type='map']">
    <xsl:apply-templates mode="xsd-type" select="current()">
      <xsl:with-param name="typename" select="@name" />
    </xsl:apply-templates> 
  </xsl:template>

  <xsl:template match="idl:typedef" priority="-1">
    <xsd:simpleType name="{@name}">
      <xsl:apply-templates mode="process-documentation" select="current()" />
      <xsd:restriction>
        <xsl:apply-templates mode="xsd-type" select="current()">
          <xsl:with-param name="type-attribute-name" select="'base'" />
        </xsl:apply-templates>
      </xsd:restriction>
    </xsd:simpleType>
  </xsl:template>

  <xsl:template match="idl:enum">
    <xsd:simpleType name="{@name}">
      <xsl:apply-templates mode="process-documentation" select="current()" />
      <xsd:restriction base="xsd:string">
        <xsl:apply-templates mode="enum-value" select="idl:member" />
      </xsd:restriction>
    </xsd:simpleType>
  </xsl:template>

  <xsl:template mode="enum-value" match="idl:member">
    <xsd:enumeration value="{@name}">
      <xsl:apply-templates mode="process-documentation" select="current()" />
    </xsd:enumeration>
  </xsl:template> 

  <xsl:template match="idl:struct|idl:exception">
    <xsd:element name="{@name}" type="{name($tns)}:{@name}" />
    <xsd:complexType name="{@name}">
      <xsl:apply-templates mode="process-documentation" select="current()" />
      <xsd:sequence>
        <xsl:apply-templates mode="field-schema" />
      </xsd:sequence>
    </xsd:complexType>
  </xsl:template>

  <xsl:template match="idl:union">
    <xsd:element name="{@name}" type="{name($tns)}:{@name}" />
    <xsd:complexType name="{@name}">
      <xsl:apply-templates mode="process-documentation" select="current()" />
      <xsd:choice minOccurs="1" maxOccurs="1">
        <xsl:apply-templates mode="field-schema" />
      </xsd:choice>
    </xsd:complexType>
  </xsl:template>

  <xsl:template mode="field-schema" match="idl:field">
    <xsl:apply-templates mode="element-for-type" select="current()">
      <xsl:with-param name="element-name" select="@name" />
    </xsl:apply-templates>
  </xsl:template>

  <xsl:template mode="xsd-type" match="*[@type='list' or @type='set']">
    <xsl:param name="typename" select="null" />
    <xsd:complexType>
      <xsl:if test="$typename">
        <xsl:attribute name="name">
          <xsl:value-of select="$typename" />
        </xsl:attribute>
      </xsl:if>
      <xsl:apply-templates mode="process-documentation" select="current()" />
      <xsd:sequence minOccurs="0" maxOccurs="unbounded">
        <xsl:apply-templates mode="element-for-type" select="idl:elemType">
          <xsl:with-param name="element-name" select="'item'" />
        </xsl:apply-templates>
      </xsd:sequence>
    </xsd:complexType>
  </xsl:template>

  <xsl:template mode="xsd-type" match="*[@type='map']">
    <xsl:param name="typename" select="null" />
    <xsd:complexType>
      <xsl:if test="$typename">
        <xsl:attribute name="name">
          <xsl:value-of select="$typename" />
        </xsl:attribute>
      </xsl:if>
      <xsl:apply-templates mode="process-documentation" select="current()" />
      <xsd:sequence minOccurs="0" maxOccurs="unbounded">
        <xsd:element name="entry">
          <xsd:complexType>
            <xsd:sequence>
              <xsl:apply-templates mode="element-for-type" select="idl:keyType">
                <xsl:with-param name="element-name" select="'key'" />
              </xsl:apply-templates>
              <xsl:apply-templates mode="element-for-type" select="idl:valueType">
                <xsl:with-param name="element-name" select="'value'" />
              </xsl:apply-templates>
            </xsd:sequence>
          </xsd:complexType>
        </xsd:element>
      </xsd:sequence>
    </xsd:complexType>
  </xsl:template>

  <xsl:template mode="xsd-type" match="*[@type='void']" />

  <xsl:template mode="xsd-type" match="idl:annotation" />

  <xsl:template mode="xsd-type" match="idl:default" />

  <xsl:template mode="xsd-type" match="*" priority="-1">
    <xsl:param name="type-attribute-name" select="'type'" />
    <xsl:attribute name="{$type-attribute-name}">
      <xsl:apply-templates mode="xsd-type-name" select="current()" />
    </xsl:attribute>
  </xsl:template>

  <xsl:template mode="element-for-type" match="*[@type='list' or @type='set' or @type='map']">
    <xsl:param name="element-name" />
    <xsl:param name="skip-min-max-occurs" select="false()" />
    <xsd:element name="{$element-name}">
      <xsl:if test="not($skip-min-max-occurs)">
        <xsl:attribute name="minOccurs">
          <xsl:value-of select="'0'" />
        </xsl:attribute>
        <xsl:attribute name="maxOccurs">
          <xsl:value-of select="'1'" />
        </xsl:attribute>
      </xsl:if>
      <xsl:apply-templates mode="xsd-type" select="current()" />
    </xsd:element>
  </xsl:template>

  <xsl:template mode="element-for-type" match="*[@type='id']">
    <xsl:param name="element-name" />
    <xsl:param name="skip-min-max-occurs" select="false()" />
    <xsl:apply-templates mode="element-for-id-type" select="$idl/idl:document[@name=current()/@type-module]/idl:*[@name=current()/@type-id]">
      <xsl:with-param name="skip-min-max-occurs" select="$skip-min-max-occurs" />
      <xsl:with-param name="element-name" select="$element-name" />
      <xsl:with-param name="typeinfo" select="current()" />
    </xsl:apply-templates>
  </xsl:template>

  <xsl:template mode="element-for-type" match="*" priority="-1">
    <xsl:param name="element-name" />
    <xsl:param name="skip-min-max-occurs" select="false()" />
    <xsd:element name="{$element-name}">
      <xsl:if test="not($skip-min-max-occurs)">
        <xsl:attribute name="minOccurs">
          <xsl:value-of select="'0'" />
        </xsl:attribute>
        <xsl:attribute name="maxOccurs">
          <xsl:value-of select="'1'" />
        </xsl:attribute>
      </xsl:if>
      <xsl:apply-templates mode="xsd-type" select="current()" />
    </xsd:element>
  </xsl:template>

  <xsl:template mode="element-for-id-type" match="idl:struct|idl:union">
    <xsl:param name="typeinfo" />
    <xsl:param name="element-name" />
    <xsl:param name="skip-min-max-occurs" />
    <xsd:element name="{$element-name}">
      <xsl:if test="not($skip-min-max-occurs)">
        <xsl:attribute name="minOccurs">
          <xsl:value-of select="'0'" />
        </xsl:attribute>
        <xsl:attribute name="maxOccurs">
          <xsl:value-of select="'1'" />
        </xsl:attribute>
      </xsl:if>
      <xsl:attribute name="type">
        <xsl:apply-templates mode="xsd-type-name" select="$typeinfo" />
      </xsl:attribute>
    </xsd:element>
  </xsl:template>

  <xsl:template mode="element-for-id-type" match="idl:enum">
    <xsl:param name="element-name" />
    <xsl:param name="skip-min-max-occurs" />
    <xsd:element name="{$element-name}" type="xsd:string">
       <xsl:if test="not($skip-min-max-occurs)">
        <xsl:attribute name="minOccurs">
          <xsl:value-of select="'0'" />
        </xsl:attribute>
        <xsl:attribute name="maxOccurs">
          <xsl:value-of select="'1'" />
        </xsl:attribute>
      </xsl:if>
    </xsd:element>
  </xsl:template>

  <xsl:template mode="element-for-id-type" match="idl:typedef">
    <xsl:param name="element-name" />
    <xsl:apply-templates mode="element-for-type" select="current()">
      <xsl:with-param name="element-name" select="$element-name" />
    </xsl:apply-templates>
  </xsl:template>

  <xsl:template mode="xsd-type-name" match="*[@type='id']">
    <xsl:value-of select="concat(@type-module, ':', @type-id)" />
  </xsl:template>

  <xsl:template mode="xsd-type-name" match="*[@type='string']">
    <xsl:value-of select="concat($doc/@name, ':string')" />
  </xsl:template>

  <xsl:template mode="xsd-type-name" match="*[@type='i8'    ]">xsd:byte</xsl:template>
  <xsl:template mode="xsd-type-name" match="*[@type='i16'   ]">xsd:short</xsl:template>
  <xsl:template mode="xsd-type-name" match="*[@type='i32'   ]">xsd:int</xsl:template>
  <xsl:template mode="xsd-type-name" match="*[@type='i64'   ]">xsd:long</xsl:template>
  <xsl:template mode="xsd-type-name" match="*[@type='byte'  ]">xsd:byte</xsl:template>
  <xsl:template mode="xsd-type-name" match="*[@type='bool'  ]">xsd:boolean</xsl:template>
  <xsl:template mode="xsd-type-name" match="*[@type='binary']">xsd:base64Binary</xsl:template>
  <xsl:template mode="xsd-type-name" match="*[@type='double']">xsd:double</xsl:template>

  <xsl:template mode="xsd-type-name" match="*" priority="-1">
    <xsl:message terminate="yes">Unhandled type: <xsl:copy-of select="current()" /></xsl:message>
  </xsl:template>

</xsl:stylesheet>