# Akka Memcached - Slack Interview

The following documentation contains instructions to run the project, design decisitions and future improvements.

This project was developed as part of the interview process at [Slack](https://slack.com) with the requirements defined in [here](https://slack-files.com/T12KS1G65-F3RUY3WJU-abf35e46b2)

## Design

This implementation of Memcached leverages Akka Actor System model and non blocking IO TCP connections to provide
a high throughput Cache server. 

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

### Protocol

The [binary protocol](https://cloud.github.com/downloads/memcached/memcached/protocol-binary.txt) for memcached was implemented. The main reasoning behind this decision was effienciency and simplification of handling the incoming packets in the tcp connection. At first, it was considered to use the text protocol, but found that it actually makes it harder to implement the protocol when dealing with ByteArray. Per the requirements document the following operations are supported:

1. Set (With support for the CAS field).
2. Delete.
3. Get.
4. Version
