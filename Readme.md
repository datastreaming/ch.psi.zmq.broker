
# Overview
The ZMQ Broker is a message broker for brokering, buffering and reducing ZMQ data streams. It can be used with any message format.

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
To start the broker use `java -Xmx1024m -jar ch.psi.zmq.broker.jar`

Optionally you can already specify a configuration file at startup via the `-c <yourConfigFile.xml>` option.
The default port of the web server serving the REST api is 8080. If you need/want to specify a different port than this use the `-p <port>` option. 

To terminate the broker use `ctrl+c`. If it does not terminate with the first `ctrl+c` (normal shutdown) issue a second one. This will force the termination of the virtual machine.



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

Currently following methods are supported for sources: PULL, SUB. PUSH and PUB are supported for destinations.
For destinations you can also configure a (max) frequency the messages are delivered. The `frequency` is in millisceconds and can be configured in the `destination` tag.

You can specify (zero,) one or more destinations.

## REST

Get current configuration:
```
GET http://<broker>:<port>/broker
```

Load given configuration:
```
GET http://<broker>:<port>/broker
BODY
<configuration>
	<routing name="BB">
		<source address="tcp://localhost:8888" />
		<destination address="tcp://*:9999" type="PUSH"/>
	</routing>
</configuration>
```

```
GET http://<broker>:<port>/broker

HEADER Content-Type: application/json
BODY
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


Configure new routing:
```
PUT http://<broker>:<port>/broker/<id>

HEADER Content-Type: application/json
BODY
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



# Development
ZMQ Broker is based on https://github.com/zeromq/jeromq the Java implementation of ZMQ.

To build the broker jar use `mvn clean compile assembly:single`


#References
- Main Page: http://zeromq.org/
- Java Library: https://github.com/zeromq/jeromq
- Education: https://learning-0mq-with-pyzmq.readthedocs.org/en/latest/pyzmq/patterns/pubsub.html
