ThriftEE
========

ThriftEE brings enterprisey features to applications built on top of the 
[Apache Thrift][thrift] platform.

This is a set of Thrift tools that I've cobbled together for my own use and am making available with the Apache Software License version 2.0.

It is under active development - feedback and contributions are definitely welcome (no CLA, but contributions must be Apache-licensed, or able to be redistributed under Apache for inclusion).

Buzzwords:
----------
 * api
 * rpc
 * apache thrift
 * java ee
 * facebook swift
 * microservices
 * polyglot

Features:
---------
 * Define Thrift services be annotating POJOs, similar to JAX-WS or JAX-RS.
 * ThriftEE structs can be integrated nicely with other Java EE features, such as EJB, JPA, etc.
 * Expose Thrift services as SOAP web services in order to integrate with "legacy" systems, test with existing tools like SOAP UI, etc.
 * Provide downloadable Thrift clients to obviate the need for API users to install the Thrift compiler
 * A fork of [Facebook's Swift tool][swift] that [allows annotating interfaces and abstract classes][swift-fork] (Facebook's version does not).  Also ThriftEE's fork works on Java 7, while Swift proper requires Java 8.
 * Pure Java build of the Thrift compiler, built with NestedVM as a single JAR file that can function as a drop-in replacement for the native executable

Wish List:
----------
 * Expose Thrift services as JSON API, similar to what we do for SOAP/XML
 * Rate limiting/throttling
 * Analytics
 * clustering/load balancing
 * proxy services
 * fine-grained security

[thrift]: http://thrift.apache.org/
[swift]: https://github.com/facebook/swift
[swift-fork]: https://github.com/facebook/swift/pull/257
