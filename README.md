# Akka Memcached - Slack Interview

The following documentation contains instructions to run the project, design decisitions and future improvements.

This project was developed as part of the interview process at [Slack](https://slack.com) with the requirements defined in [here](https://slack-files.com/T12KS1G65-F3RUY3WJU-abf35e46b2).

## tl; dr

Here a quick set of instructions to just get started and test the project. If you have [Docker](https://www.docker.com), the project provides a **docker-compose** file that you can use to start the server:

```bash
$ CACHE_SIZE=10000 docker-compose up memcached
```
This will try to bound to the default memcached port (**11211**). Once it builds and starts, you can use any client that supports memcached binary protocol. Here an example using ruby:
```ruby
require 'dalli'
client = Dalli::Client.new('localhost:11211')
client.set("a", "1")
# => 1
client.get("a", "1")
# => 1
client.delete("a")
#=> true
client.set("a", 1)
client.cas("a") { |v| v + 1 }
#=> 2
```
Also, as another Dockerfile, a script is provided that contains integration tests. It tries similar operations as above, but also creates threads and performs concurrent operations to validate that **CAS** operations are working as expected. You can run them like this:

```bash
docker-compose up tests
```

## Design

This implementation of Memcached leverages Akka Actor System and non blocking IO TCP connections to provide
a high throughput Cache server. 

### Main Components
1. **Tcp Connection Creator**: This a singleton actor that it's only responsability is establishing the connection with the client, assigning a connection and  creates a Command Handler actor that will handle all the interactions with the client. 
2. **Command Handler**: This actor is responsible for accepting and parsing commands from the clients. Once a command has been parsed, the work is delegated to a cache actor that will fulfill the command.
3. **LRU Cache**: There will be one LRU Cache for the whole application. The max size will be the value provided as parameter to the application at start. This actor handles the consistency of the Cache and performs the supported operations. Once the operation have been performed in the cache, it delegates sending the response back to the client using the connection actor. 
    
The following diagram, ilustrates how this components fit together in the design.

```

                                  +--------------------+            +--------------------+
                                  | Command Handler 1  +--+         | LRU Cache          |
                                  +--------------------+  |         |                    |
                                                          |         |                    |
 +----------------+                                       +--------->                    |
 |                |               +--------------------+            |                    |
 |TCP Conn Creator+-+ Creates +-> | Command Handler 2  +------------>                    |
 |                |               +--------------------+            |                    |
 +----------------+                         .             +--------->                    |
                                            .             |         |                    |
                                  +--------------------+  |         |                    |
                                  | Command Handler N  +--+         |                    |
                                  +--------------------+            |                    |
                                                                    +--------------------+

```

Once the client has an open connection to the server, a normal flow for a command will look like this:

```
+--------+
|        |               +-------------+
| Client +--+ GET(a) +-> | CMD Handler |
|        |               +------+------+
+---+----+                      |
    ^                           |
    |                       +---v----+
    |                       | Cache  |
    |                       +---+----+
    |                           |
    |                           |
    |                           |
    |                    +------v------+
    +--+GET+Response+----+ Connection  |
                         +-------------+

```

### Protocol

The [binary protocol](https://cloud.github.com/downloads/memcached/memcached/protocol-binary.txt) for memcached was implemented. The main reasoning behind this decision was effienciency and simplification of handling the incoming packets in the tcp connection. At first, it was considered to use the text protocol, but found that it actually makes it harder to implement the protocol when dealing with ByteArray. Per the requirements document the following operations are supported:

1. Set (With support for the CAS field).
2. Delete.
3. Get.
4. Version. (This implemented just to make things easier to test, some memcached clients fetch the version after connecting to the server). 

In the current implementation the following fields of the protocol are ignored: **Expiration, Opaque.**

## Discussion
TODO
### Monitoring 

* We should collect system metrics and keep a very close eye in the following: 
    * TCP connections opened at the OS level
    * Network IO: This could be one of the most important metrics that will tell us that we are saturating the server. 
    * CPU Usage: This just for reference, but CPU shouldn't be over utilized in this system
    
### Limitations
* TODO
* There is no concept of backpressure at the moment:  

## Resources
* [Memached Internals](https://www.adayinthelifeof.nl/2011/02/06/memcache-internals/)
* [Memcached Source Code](https://github.com/memcached/memcached)
* [Memcached Benchmark Tool](https://redislabs.com/blog/memtier_benchmark-a-high-throughput-benchmarking-tool-for-redis-memcached/)
