# Akka Memcached - Slack Interview

The following documentation contains instructions to run the project, design decisions and future improvements.

This project was developed as part of the interview process at [Slack](https://slack.com) with the requirements 
defined in [here](https://slack-files.com/T12KS1G65-F3RUY3WJU-abf35e46b2).

## tl; dr

Here a quick set of instructions to just get started and test the project. 
If you have [Docker](https://www.docker.com), the project provides a **docker-compose** file 
that you can use to start the server:

```bash
# Size should be set in bytes (This is 1MB)
$ CACHE_SIZE=1000000 docker-compose up memcached
```
This will try to bound to the default Memcached port (**11211**). Once it builds and starts, you can use any client that supports memcached binary protocol. Here an example using ruby:
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
Also, as another Dockerfile, a script is provided that contains integration tests. It tries similar operations as above,
but also creates threads and performs concurrent operations to validate that **CAS** operations are working as expected.
You can run them like this:

```bash
docker-compose up tests
```

## Design

This implementation of Memcached leverages Akka Actor System and non blocking IO TCP connections to provide
a high throughput Cache server. 

### Main Components
1. **Tcp Connection Creator**: This a singleton actor that it's only responsibility is establishing the connection
     with the client, assigning a connection and  creates a Command Handler actor that will handle all the interactions
     with the client.
2. **Command Handler**: This actor is responsible for accepting and parsing commands from the clients.
     Once a command has been parsed, the work is delegated to a cache actor that will fulfill the command.
3. **LRU Cache**: There will be one LRU Cache for the whole application. The max size will be the value provided as
     parameter to the application at start. This actor handles the consistency of the Cache and performs the supported
     operations. Once the operation have been performed in the cache, it delegates sending the response back to the
     client using the connection actor.
    
The following diagram, illustrates how this components fit together in the design.

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

The [binary protocol](https://cloud.github.com/downloads/memcached/memcached/protocol-binary.txt) for Memcached was
implemented. The main reasoning behind this decision was efficiency and simplification of handling the incoming packets
in the tcp connection. At first, it was considered to use the text protocol, but found that it actually makes it harder
to implement the protocol when dealing with ByteArray. Per the requirements document the following operations are
supported:

1. Set (With support for the CAS field).
2. Delete.
3. Get.
4. Version. (This implemented just to make things easier to test, some Memcached clients fetch the version after connecting to the server). 

In the current implementation the following fields of the protocol are ignored: **Expiration, Opaque.**

## Discussion

### Monitoring 

* We should collect system metrics and keep a very close eye in the following: 
    * TCP connections opened at the OS level.
    * Network IO: This could be one of the most important metrics that will tell us that we are saturating the server. 
    * Memory Usage: This will tell us how much of the cache is full.
    * JVM stats: Specially GC Cpu Time and Heap Memory usage
    * CPU Usage: This just for reference, but CPU shouldn't be over utilized in this system.
    
### Limitations
* There is single cache object for the whole application. This will be a bottleneck. Future work here
  will be related to this areas:
  1. Better isolation of the cache actor compared to all the other actors. It will be interesting to test if 
    having a dedicated thread and dispatcher for this actor yields better performance.
  2. Divide the full size of the cache into different buckets to give more granularity to the data accessed in the cache. 
     In this way there could be multiple concurrent keys being accessed at the same time).
  3. Improve the LRU algorithm. At the moment the implementation is really simple. But there are other caching strategies
     and improvements to the algorithm itself that would make the service better.
  4. Fragment the memory depending on the size of the value. Memcached does something similar to this with the use of 
     their slabs. 
    
* There is no concept of backpressure. The server will try to accept as many connections as they come in.
  This could create DDOS scenarios. 
  
### Load Testing
A load testing was performed in the solution. This was done in a computer with the following characteristics: 
2.6 GHz Intel Core i7 and 16 GB 1600 MHz DDR3. This test was run without using docker. The test was performed against 
both this solution and the real Memcached implementation. The tool used for the test is in the resources area. 
Both servers were started with a max memory usage of 500 MB.

The test was run with the following parameters:

``` bash
# This will send random payload sizes
$ memtier_benchmark -n 10000 -s 192.168.0.100 -p 11212 -P memcache_binary -c 50 -R  
```

These are the results for Memcached:

```
4         Threads
50        Connections per thread
10000     Requests per thread


ALL STATS
========================================================================
Type        Ops/sec     Hits/sec   Misses/sec      Latency       KB/sec
------------------------------------------------------------------------
Sets        1342.98          ---          ---     13.49900       103.50
Gets       13415.01         1.48     13413.54     13.43500       509.42
Waits          0.00          ---          ---      0.00000          ---
Totals     14757.99         1.48     13413.54     13.44100       612.92

```

The following are the results for this implementation:

```
4         Threads
50        Connections per thread
10000     Requests per thread


ALL STATS
========================================================================
Type        Ops/sec     Hits/sec   Misses/sec      Latency       KB/sec
------------------------------------------------------------------------
Sets        1142.99          ---          ---     16.17700        88.09
Gets       11417.38         1.26     11416.13     16.40300       433.57
Waits          0.00          ---          ---      0.00000          ---
Totals     12560.38         1.26     11416.13     16.38200       521.65
```

We can see from this results that even though the real Memcached has better performance, the Akka actor system
also provides a very powerful concurrency model that with not too much effort very performance systems can be built. 

## Resources
* [Memached Internals](https://www.adayinthelifeof.nl/2011/02/06/memcache-internals/)
* [Memcached Source Code](https://github.com/memcached/memcached)
* [Memcached Benchmark Tool](https://redislabs.com/blog/memtier_benchmark-a-high-throughput-benchmarking-tool-for-redis-memcached/)
