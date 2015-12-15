# Installation

To "install" the broker just extract the distribution zip file.

The only dependency you have is JVM 1.7 or greater.

# Overview
The ZMQ Broker is a message broker for brokering, buffering and reducing ZMQ data streams. It can be used with any
message format.

```
                                                                            +----------------+
                                                                            |                |
        +--------------+                    +---------------+    +----->PULL| destination 1  |
        |              |                    |               |    |          |                |
        | source       |                    | broker        |    |          +----------------+
        |              |PUSH+---------->PULL|               |PUSH+                 ...
        |              |                    |               |    |          +----------------+
        +--------------+                    +---------------+    |          |                |
                                                                 +----->PULL| destination 2  |
                                                                            |                |
                                                                            +----------------+
```

The broker comes with a REST API to be able to dynamically (re)configure the brokers routing and settings.
To start the broker use

```
bin/broker
```

Optionally you can already specify a configuration file at startup via the `-c <yourConfigFile.xml>` option.
The default port of the web server serving the REST API is 8080. If you need/want to specify a different port than this use the `-p <port>` option.

To terminate the broker use `ctrl+c`. If it does not terminate with the first `ctrl+c` (normal shutdown) issue a second one. This will force the
termination of the virtual machine.

After the startup there the web ui can be accessed via (note that the trailing / is important!)

```
http://<host>:<port>/static/
```


# Configuration
The broker can be configured via config file and/or via REST API.

## File
The configuration file of the broker can be specified via the `-c` option at the startup of the broker.
The basic structure of the configuration is as follows:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
	<routing name="">
		<source address="tcp://localhost:8080" type="PULL"/>
		<destination address="tcp://*:9090" type="PUSH"/>
		<destination address="tcp://*:9091" frequency="500" type="PUB"/>
	</routing>
</configuration>
```

Currently following methods are supported for sources: PULL, SUB. For destinations PUSH and PUB are supported.
For destinations you can also configure a (max) frequency the messages are delivered. The `frequency` is in millisceconds and can be configured in the `destination` tag.

You can specify (zero,) one or more destinations.

A buffer size can be configured for both incomming (source) and outgoing (destination) side (queue/topic). This can be done by specifying the attribute `buffer="<size>"` on the `source` and `destination` tag.
The default buffer size (if attribute is omitted) is on both side 5. This means that the broker is keeping maximum 10 messages inside its buffer (max memory consumed ~ 10 times message size).

This feature can be used to use the broker as kind of online buffer when receiving parties are not able to keep up the speed the source delivers data but still need to get all messages. If using the broker in this kind of mode tuning to the explicit setup is necessary.


## REST

Get current configuration:

```
GET http://<broker>:<port>/broker
```

Load new XML configuration:

```
PUT http://<broker>:<port>/broker
HEADER -  Content-Type: application/xml

<configuration>
	<routing name="BB">
		<source address="tcp://localhost:8888" />
		<destination address="tcp://*:9999" type="PUSH"/>
	</routing>
</configuration>
```

Load new JSON configuration:

```
PUT http://<broker>:<port>/broker
HEADER Content-Type: application/json

{
    "routing": [
        {
            "name": "BB",
            "source": {
                "address": "tcp://localhost:7777",
                "type": "PULL"
            },
            "destination": [
                {
                    "address": "tcp://*:8888",
                    "type": "PUSH"
                }
            ]
        }
    ]
}
```

Delete current configuration:

```
DELETE http://<broker>:<port>/broker
```

Add new routing:

```
PUT http://<broker>:<port>/broker/<id>

HEADER Content-Type: application/json

{
    "source": {
        "address": "tcp://localhost:6666",
        "type": "PULL"
    },
    "destination": [
        {
            "address": "tcp://*:8989",
            "type": "PUSH"
        }
     ]
}
```

Delete configured routing:

```
DELETE http://<broker>:<port>/broker/<id>
```

### Curl Commands

```
# Get current configuration
curl http://<broker>:<port>/broker

# Delete current configuration
curl -X DELETE http://<broker>:<port>/broker

# Load new configuration
curl -X PUT -H "Content-Type: application/json" --data '{"routing":[{ "name": "BB", "source":{ "address": "tcp://localhost:7777", "type": "PULL"},"destination": [{"address": "tcp://*:8888","type": "PUSH"}]}]}' http://<broker>:port/broker

# Add new routing
curl -X PUT -H "Content-Type: application/json" --data '{ "name": "BB", "source":{ "address": "tcp://localhost:7777", "type": "PULL"},"destination": [{"address": "tcp://*:8888","type": "PUSH"}]}' http://<broker>:<port>/broker/<myid>
```


# Development
## Build
The project is build via Gradle. It can be easily build via

```bash
./gradlew build
```

__Notes:__ You don't have to have gradle installed on your machine. All you need is a Java JDK version >= 1.7 .

The installation zip package can be build by

```bash
./gradlew distribution
```

Afterwards the installable zip file is available in the `build/distributions` directory.


#References

  * http://www.zeromq.org/
  * https://github.com/zeromq/jeromq

  * https://learning-0mq-with-pyzmq.readthedocs.org/en/latest/pyzmq/patterns/pubsub.html
  * http://zeromq.org/whitepapers:brokerless
  * http://java.dzone.com/category/tags/zeromq
  * http://nichol.as/zeromq-an-introduction
  * http://ruudud.github.io/presentations/zeromq/#/
  * http://java.dzone.com/articles/distributed-systems-zeromq

# TODO
  * High-watermark
  * Error checking when adding new routings
  * Clean termination of routes / no blocking, ...
