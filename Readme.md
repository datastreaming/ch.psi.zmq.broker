
# Overview
ZMQ broker for routing ZMQ messages. Currently only PUSH/PULL is supported.

``
                                                                            +----------------+
                                                                            |                |
        +--------------+                    +---------------+    +----->PULL| destination 1  |
        |              |                    |               |    |          |                |
        | source       |                    | broker        |    |          +----------------+
        |              |PUSH+---------->PULL|               |PUSH+
        |              |                    |               |    |          +----------------+
        +--------------+                    +---------------+    |          |                |
                                                                 +----->PULL| destination 2  |
                                                                            |                |
                                                                            +----------------+
``

To start the broker use `java -Xmx1024m -jar ch.psi.zmq.broker.jar yourConfigFile.xml`

To terminate the broker use `ctrl+c`. If it does not with the first `ctrl+c` (normal shutdown) issue a second one. This will force the termination of the virtual machine.

## Configuration
The broker is configured via a xml configuration file. The content of the configuration is as follows:

``
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
	<routing name="">
		<source address="tcp://localhost:8080" />
		<destination address="tcp://*:9090"/>
		<destination address="tcp://*:9091"/>
	</routing>
</configuration>
``

You can specify (zero,) one or more destinations.

## Development
To build the broker jar use `mvn clean compile assembly:single`