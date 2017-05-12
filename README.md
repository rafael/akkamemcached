# Akka Memcached - Slack Interview

The following documentation contains instructions to run the project, design decisitions and future improvements.

This project was developed as part of the interview process at [Slack](https://slack.com) with the requirements defined in [here](https://slack-files.com/T12KS1G65-F3RUY3WJU-abf35e46b2)

## Design

This implementation of Memcached leverages Akka Actor System and non blocking IO TCP connections to provide
a high throughput Cache server. 

### Main Components
1. **Tcp Connection Creator**: This a singleton actor that it's only responsability is establishing the connection with the client, assigning and connection and spun a Command Handler actor that will handle all the interactions with the client.
2. **Command Handler**: This actor is responsible for accepting and parsing commands from the clients. Once a command has been parsed, the work is delegated to a consistent hashing router that will delegate the work of perfoming the request to a given LRU Cache Bucket. It is guarantee that subsequent requests for the same key, will be handled by the same Cache Bucket.
3. **LRU Cache**: 
    1. There will be N Cache buckets. 
    2. The numbers of buckets is defined by configuration. This will shard the keyspace and divide the load for the request among different actors. 
    3. The total capacity of the cache is divided evenly among the number of buckets.
    
The following diagram, ilustrates how this components fit together in the design.

```

                                  +--------------------+                      +--------------------+
                                  | Command Handler 1  +--+                   | LRU Cache          |
                                  +--------------------+  |                   | +----------------+ |
                                                          |  +------------+   | | Cache Bucket 1 | |
 +----------------+                                       +-->            |   | +----------------+ |
 |                |               +--------------------+     |            |   | +----------------+ |
 |TCP Conn Creator+-- Creates --> | Command Handler 2  +-----> C.H Router |-->| | Cache Bucket 2 | |
 |                |               +--------------------+     |            |   | +----------------+ |
 +----------------+                         .             +-->            |   |         .          |
                                            .             |  +------------+   |         .          |
                                  +--------------------+  |                   | +----------------+ |
                                  | Command Handler N  +--+                   | | Cache Bucket N | |
                                  +--------------------+                      | +----------------+ |
                                                                              +--------------------+

```

Once the client has an open connection with the server, a normal flow for a command will look like this:

```
+--------+
|        |               +-------------+
| Client +--+ GET(a) +-> | CMD Handler |
|        |               +------+------+    +----------------+
+---+----+                      |           | Cache Bucket 1 |
    ^                           |           +----------------+
    |                    +------v-----+     +----------------+
    |                    | C.H Router +-+   | Cache Bucket 2 |
    |                    +------------+ |   +----------------+
    |                                   |   +----------------+
    |                                   +---> Cache Bucket N |
    |                                       +--------+-------+
    |                                                |
    +-------------------GET Response-----------------+
```

### Protocol

The [binary protocol](https://cloud.github.com/downloads/memcached/memcached/protocol-binary.txt) for memcached was implemented. The main reasoning behind this decision was effienciency and simplification of handling the incoming packets in the tcp connection. At first, it was considered to use the text protocol, but found that it actually makes it harder to implement the protocol when dealing with ByteArray. Per the requirements document the following operations are supported:

1. Set (With support for the CAS field).
2. Delete.
3. Get.
4. Version

In the current implementation the following fields of the protocol are ignored: **Expiration, Opaque and flags.**

## Discussion

### Monitoring 

* The system already provides the following metrics using statsd: Transactions Per Second, Memory Used by the Cached, Clients Connected. This metrics should be visiualized with tools like grafana.
* On top of these metrics, we should collect system metrics and keep a very close eye in the following: 
    * TCP connections opened at the OS level: 
    * Network IO: This could be one of the most important metrics that will tell us that we are saturating the server. 
    * CPU Usage: This just for reference, but CPU shouldn't be over utilized in this system
    
### Limitations
* There is no concept of backpressure at the moment:  
