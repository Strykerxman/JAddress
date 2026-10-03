JAddress
--------

A custom Java TCP Socket Server using a lightweight, proprietary Binary Wire Protocol. The system loads geospatial address records from a relational structure into thread-safe, in-memory data structures for sub-millisecond lookups.

## Run the demo
Firstly, travel to the *src* directory and compile the Java files.
```bash
cd src
javac *.java
```
After a successful compilation, open two separate terminals that are in the *src* directory.
In the first, run the server:
```java
java Server
```
Then run a client:
```java
java Client
```

You may input any string in the client interface, the server will capture it and send a response back to the client.
