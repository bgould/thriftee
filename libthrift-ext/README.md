An XML Protocol for Apache Thrift
=================================

This is my modest proposal, proof-of-concept, and playground for a Thrift
protocol implemented on top of XML.  It is my intention to contribute this work
to the Apache Thrift project once I have received some feedback from the
community.  You should know that "Apache", "Thrift", "Apache Thrift", and many
other things are trademarks of the Apache Software Foundation, and that this
project and its code are not part of the ASF.  In the source code you will find
package names like `org.apache.thrift.protocol`; I have chosen to use these
names so that if you build things on top of this project and it later becomes
part of Thrift itself, you might not need to recompile - not to make you think
that this is official software from the ASF. Like Thrift itself, this software
is published and made available for use under the Apache License, version 2.

Rationale
---------

I want Thrift to play nice with existing "enterprise" platforms that are based
on XML, XSD, and SOAP.

Thrift's IDL is much nicer to work with than XSD, and don't even get me started
about the insanity of using JSON for service interfaces... Thrift could be
suitable for defining and designing all sorts of interfaces if it has good
interoperability with XML systems.

Also, a nagging annoyance when using Thrift is the lack of a protocol format
that is both human readable and writeable.  `TSimpleJSONProtocol` is so
tempting... until you realize it is a one-way trip.

Implementation
--------------

Let's say you have an interface like this:

    enum Spinkle {
      HRRR, PPOL, REWT
    }
    
    union Sprat {
      1:  string woobie;
      2:  i32 wowzer;
      3:  Spinkle wheee;
    }
    
    struct Spirfle {
      1:  string giffle;
      2:  i32 flar;
      3:  Spinkle spinkle;
    }
    
    struct Everything {
      1:  string str;
      2:  i64 int64;
      3:  i32 int32;
      4:  i16 int16;
      5:  byte bite;
      6:  double dbl;
      7:  binary bin;
      8:  Spinkle enu;
      9:  Sprat onion;
      10: list<string> str_list;
      11: list<Spinkle> enum_list;
      12: list<Spirfle> obj_list;
      13: list<list<i32>> int_list_list;
      14: map<string, string> str_str_map;
      15: map<i32, string> int_str_map;
      16: map<i32, Spirfle> int_obj_map;
      17: Spirfle obj;
      18: set<string> str_set;
      19: set<Spirfle> obj_set;
    }
    
    service Universe {
      i32 grok(1: Everything arg0);
    }

My first thought was to make a protocol that was compliant with the schemas
produced by the Thrift compiler's XSD generator... then I quickly discovered
that the Thrift compiler does not produce valid schemas.  I won't document the
problems here... try it on the above interface and you'll find out pretty
quickly that at the very least the XSD generator needs some serious bugs fixed
in order to start to approach something that is somewhat useable for this
purpose.

Nevertheless, the schemas that it tries to generate seem close to what I would
like to have in a perfect world... one of the above structs might look like
this:

    <Spirfle>
      <giffle>foobar</giffle>
      <flar>42</flar>
      <Spinkle>1</Spinkle>;
    </Spirfle>

Add in a little namespacing and you could stick that in a SOAP envelope and
send it to someone who cares. In any case, this would make for a very easy to
understand format that could even be authored by hand to craft test messages
and such.

There is a small problem with this format however... it is not quite 
expressive enough to easily comply with the way that Thrift likes to read and
write data.  For example, Thrift implementations seem to care more about
field IDs than field names, and they expect the protocol to specify the data
type as a field is being read... translating names into field IDs and types
is tricky, and is likely a big part of the reason that `TSimpleJSONProtocol`
has no implementation for reading.

Also problematic are the container types; for example, using the "simple" XML
format above, you might implement a `list` like this:

    <Everything>
      <str_list>
        <string>spoggle</string>
        <string>xertiflow</string>
        <string>propplehorse</string>
      </str_list>
    </Everything>

This has similar problems to struct fields however... at the start of the 
container, the protocol should emit the element type, the field ID, and the
size.  Without encoding this data in the start tag for the list, the only way
even come close would be to look ahead in the message.  I really wanted the
protocol format to be able to parsed with a streaming parser that doesn't need
to looks forwards or backwards.

For all of these reasons, I came to the conclusion that one approach would be
to design an XML format that is in line with Thrift semantics, rather than 
modeled after the data it is carrying.  For example, this message has all of 
the data that a protocol would need and can be parsed in a streaming manner:

    <call xmlns="http://thrift.apache.org/xml/protocol" name="grok" seqid="1">
      <struct name="grok_args">
        <struct field="1" fname="arg0" name="Everything">
          <string field="1" fname="str">foobar</string>
          <i64 field="2" fname="int64">10000000000</i64>
          <i32 field="3" fname="int32">64000</i32>
          <i16 field="4" fname="int16">1024</i16>
          <i8 field="5" fname="bite">42</i8>
          <double field="6" fname="dbl">10.4</double>
          <string field="7" fname="bin">c2VjcmV0X3Bhc3N3b3Jk</string>
          <list field="10" fname="str_list" size="3" value="string">
            <string>wibble</string>
            <string>snork</string>
            <string>spiffle</string>
          </list>
          <list field="11" fname="enum_list" size="2" value="i32">
            <i32>0</i32>
            <i32>2</i32>
          </list>
          <list field="12" fname="obj_list" size="3" value="struct">
            <struct name="Spirfle">
              <string field="1" fname="giffle">blat</string>
              <i32 field="2" fname="flar">17</i32>
              <i32 field="3" fname="spinkle">0</i32>
            </struct>
            <struct name="Spirfle">
              <string field="1" fname="giffle">yarp</string>
              <i32 field="2" fname="flar">89</i32>
              <i32 field="3" fname="spinkle">2</i32>
            </struct>
            <struct name="Spirfle">
              <string field="1" fname="giffle">trop</string>
              <i32 field="2" fname="flar">9</i32>
            </struct>
          </list>
          <map field="14" fname="str_str_map" size="2" value="string" key="string">
            <string>foo</string>
            <string>bar</string>
            <string>graffle</string>
            <string>florp</string>
          </map>
          <map field="16" fname="int_obj_map" size="3" value="struct" key="i32">
            <i32>1</i32>
            <struct name="Spirfle">
              <string field="1" fname="giffle">blat</string>
              <i32 field="2" fname="flar">17</i32>
              <i32 field="3" fname="spinkle">0</i32>
            </struct>
            <i32>2</i32>
            <struct name="Spirfle">
              <string field="1" fname="giffle">yarp</string>
              <i32 field="2" fname="flar">89</i32>
              <i32 field="3" fname="spinkle">2</i32>
            </struct>
            <i32>3</i32>
            <struct name="Spirfle">
              <string field="1" fname="giffle">trop</string>
              <i32 field="2" fname="flar">9</i32>
            </struct>
          </map>
          <struct field="17" fname="obj" name="Spirfle">
            <string field="1" fname="giffle">blat</string>
            <i32 field="2" fname="flar">17</i32>
            <i32 field="3" fname="spinkle">0</i32>
          </struct>
          <set field="18" fname="str_set" size="3" value="string">
            <string>wibble</string>
            <string>snork</string>
            <string>spiffle</string>
          </set>
          <set field="19" fname="obj_set" size="3" value="struct">
            <struct name="Spirfle">
              <string field="1" fname="giffle">blat</string>
              <i32 field="2" fname="flar">17</i32>
              <i32 field="3" fname="spinkle">0</i32>
            </struct>
            <struct name="Spirfle">
              <string field="1" fname="giffle">yarp</string>
              <i32 field="2" fname="flar">89</i32>
              <i32 field="3" fname="spinkle">2</i32>
            </struct>
            <struct name="Spirfle">
              <string field="1" fname="giffle">trop</string>
              <i32 field="2" fname="flar">9</i32>
            </struct>
          </set>
        </struct>
      </struct>
    </call>

Note that actually this is _more_ data than the protocol needs... the struct 
names and field names can be eliminated for example, but the names make it more
readable and it is more obvious which structures and fields are used.

I also designed a more compact format... the message as above might be 
condensed into this for example:

    <m1 xmlns="http://thrift.apache.org/xml/protocol" n="grok" q="1">
      <t12>
        <t12 i="1">
          <t11 i="1">foobar</t11>
          <t10 i="2">10000000000</t10>
          <t8 i="3">64000</t8>
          <t6 i="4">1024</t6>
          <t3 i="5">42</t3>
          <t4 i="6">10.4</t4>
          <t11 i="7">c2VjcmV0X3Bhc3N3b3Jk</t11>
          <t15 i="10" z="3" v="t11">
            <t11>wibble</t11>
            <t11>snork</t11>
            <t11>spiffle</t11>
          </t15>
          <t15 i="11" z="2" v="t8">
            <t8>0</t8>
            <t8>2</t8>
          </t15>
          <t15 i="12" z="3" v="t12">
            <t12>
              <t11 i="1">blat</t11>
              <t8 i="2">17</t8>
              <t8 i="3">0</t8>
            </t12>
            <t12>
              <t11 i="1">yarp</t11>
              <t8 i="2">89</t8>
              <t8 i="3">2</t8>
            </t12>
            <t12>
              <t11 i="1">trop</t11>
              <t8 i="2">9</t8>
            </t12>
          </t15>
          <t13 i="14" z="2" v="t11" k="t11">
            <t11>foo</t11>
            <t11>bar</t11>
            <t11>graffle</t11>
            <t11>florp</t11>
          </t13>
          <t13 i="16" z="3" v="t12" k="t8">
            <t8>1</t8>
            <t12>
              <t11 i="1">blat</t11>
              <t8 i="2">17</t8>
              <t8 i="3">0</t8>
            </t12>
            <t8>2</t8>
            <t12>
              <t11 i="1">yarp</t11>
              <t8 i="2">89</t8>
              <t8 i="3">2</t8>
            </t12>
            <t8>3</t8>
            <t12>
              <t11 i="1">trop</t11>
              <t8 i="2">9</t8>
            </t12>
          </t13>
          <t12 i="17">
            <t11 i="1">blat</t11>
            <t8 i="2">17</t8>
            <t8 i="3">0</t8>
          </t12>
          <t14 i="18" z="3" v="t11">
            <t11>wibble</t11>
            <t11>snork</t11>
            <t11>spiffle</t11>
          </t14>
          <t14 i="19" z="3" v="t12">
            <t12>
              <t11 i="1">blat</t11>
              <t8 i="2">17</t8>
              <t8 i="3">0</t8>
            </t12>
            <t12>
              <t11 i="1">yarp</t11>
              <t8 i="2">89</t8>
              <t8 i="3">2</t8>
            </t12>
            <t12>
              <t11 i="1">trop</t11>
              <t8 i="2">9</t8>
            </t12>
          </t14>
        </t12>
      </t12>
    </m1>

The struct and field names have been removed, the attribute names shortened, 
and the types are now based on the byte constants defined on `o.a.t.p.TType`
and `o.a.t.p.TMessageType` (avoids converting to byte values via a lookup
table).

In the source tree of this project is an implementation for this protocol 
written in Java, using the standard "streaming API for XML" (StAX) available
as part of the JVM.  It relies on no external libraries other than libthrift
and the Java 7 APIs.  In `src/main/resources/org/apache/thrift/protocol` you 
can find XSD describing both of the above formats.

If you have suggestions for improvements to this format please feel free to 
create an issue ticket for this project or send me a pull request.

For me, the next steps for this protocol are to modify the Thrift compiler to
output valid XSD for the "simple" XML format described above - not quite unlike
the current XSD generator, just need to make it work correctly :) - and to add
a generator for XSLT that can convert between the streaming format and the 
"simple" format.

For `Total Protonic Reversal`, even more XSLT can probably be used to generate
WSDLs and a TSOAPTransport can be developed, so that Thrift can seamlessly play
nice with "legacy" SOA systems.

Status:
-------

First stab at the Java implementation of the streaming protocols is working, 
but is under development and subject to change.

Also, XSLT for converting from streaming format to "simple" format is working,
albeit with a short list of TODOs.

`git clone` and `mvn clean install` will build it for you if you have Java 7
and Maven, as well as Thrift 0.9.3 on your `$PATH`.

Todo:
-----
  * Implement XSLT generator(s) for converting from simple format to streaming
  * Ask on thrift dev mailing list about un-deprecating `xsd_namespace`
  * Fix Thrift compiler's XSD generator
  * `TSOAPTransport` in Java
  * Investigate creating C++ implementation using [`libstudxml`][libstudxml]
  
[libstudxml]: http://www.codesynthesis.com/projects/libstudxml/
