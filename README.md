# oreva
OData V2 Implementation using Java, Fork of OData4J for bug fixes and small enhancements.

OData + Rest + Java  ==> shake it ==> o-re-va  (that's it, no other meaning behind it)

This is complete fork of OData4J for the use of Teiid, but I guess anybody who wants to implement OData V2 can use it.

There is no support JPA or JDBC in this version. I always thought they polluted the OData framework, they do not belong in
the core project. The frameworks needs to be about OData. JPA or JDBC can be additional modules.

## Install with
$ mvn install -P release
