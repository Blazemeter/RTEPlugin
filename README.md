# JMeter-RTE-plugin

This project implements a JMeter plugin to **support RTE (Remote Terminal Emulation) protocols** by providing config elements and samplers.

Nowadays the plugin supports **IBM protocol's TN5250 and TN3270 (last one not implemented yet)** by using [xtn5250](https://sourceforge.net/projects/xtn5250/) and [JinTN3270](https://sourceforge.net/p/jintn3270/code/HEAD/tree/trunk/) libraries.

People who usually work with these IBM servers interact with it, basically, by sending keystrokes from the terminal keyboard (or emulator) to fill forms or call processes.
Following this, the sampler is designed in a way that the user could specify the position of fields on the screen and the text string to write on them. Besides, the sampler allows to simulate the action buttons existing on the terminal keyboard like ENTER, F1, F2, F3..., ATTN, CLEAR, etc..    

## Usage

### Using the plugin

The plugin adds two different elements to JMeter:

A Config Element (RTE Config)

![alt text](http://gitlab.abstracta.us/BZ/jmeter-rte-plugin/blob/master/readme/RTEConfig.png "RTE Config GUI")

and a Sampler (RTE Sampler)

![alt text](http://gitlab.abstracta.us/BZ/jmeter-rte-plugin/blob/master/readme/RTESampler.png "RTE Sampler GUI")

The RTE Config element sets the parameters to be used by the sampler in order to establish the connection to the server. These parameters are:
- *Server* (required). The url or ip of the IBM server.
- *Port*. The port number to connect, default value is 23.
- *Protocol* (required). The protocol to use in the communication. If the server is an AS400 or iSeries it typically uses TN5250, on the other hand, if it's a mainframe system it uses TN3270.
- *Terminal Type*. The terminal type to emulate from the client. If the server does not supports the chosen one, it will use the default value for the protocol.
- *SSL Type*. The SSL protocol to use if it's required by the server. The keystore file and the keystore password should be specified in *system.properties* file by adding the lines `javax.net.ssl.keyStore=</keystore_path/file.keystore>` and `javax.net.ssl.keyStorePassword=<changeit>`. 
- *Timeout*. The maximum time to wait to establish the connection by the sampler. This time takes into account the time until the client receives a response screen from the server. 

RTE Samplers in a same thread group share the connection in each thread created (different threads use separate connections).The RTE Sampler element checks if exists a connection to send the packets, if there isn't, it uses the RTE Config data to establish a new one. 

This means that it's **always required an RTE Config Element** in order to connect the RTE samplers to a server.

If more than one RTE Config element is used at the same level of the Test Plan, JMeter will take the value of the first one. On the other hand if there are more than one RTE Config used but in different levels, JMeter will use the "closest" (talking about Test Plan tree levels) Config element for each sampler.

The RTE Sampler fields are:
- *RTE Message*
  - *Payload*. Contains a grid in which user can specify Row and Column of a field in the screen, and the value (string) to send in the field starting from that position. Rows and columns starts from Row 1, Column 1 (are 1 indexed).
  - *Actions*. These buttons trigger the action to be sent to the server on each sample. They all represent a key from a terminal's keyboard.
  - *Disconnect*. Disconnects from the server after sending the sampler request. By default this option is unchecked. If a sampler in the middle of a workflow has this field checked, the following sampler will re-connect to the server. 
  - *Just Connect*. This option disables all the information set in Payload and Actions field's so no input or action key will be sent to the server. It's useful in case the user wants to validate the welcome screen or use a waiter (see below) without interact with the server.    
- *Wait for*. Waiters are executed after the sampler sends packets to the server. They wait for a specific condition, if this condition is not reached after a specific time (defined in *Timeout* value) the sampler returns timeout error. There are four defined waiters:
  - *Sync*. Waits for the system to return from X SYSTEM or Input Inhibited mode. Default value is checked, as it's recommended to always check that the system is not in Input Inhibited Mode after a sample (and before the next one) in order to get the correct screen in the sample result (and to ensure that the next sampler is executed from the desired screen). On the other hand, the sampler does an implicit "Wait for sync" each time it connects to a server, which means that if *Just Connect* option is checked it's not needed to check the *Wait for sync* function, unless you want to change the default timeout. 
  - *Cursor*. Waits for the cursor to appear at a specific location in the terminal window.
  - *Silent*. Waits for the client application to be silent for a specified amount of time. The client is considered silent when the terminal emulator does not receive any characters. 
  - *Text*. Waits for a text string, who matches the specified regex, to appear in a specific location.

All the "waiters" have a stable timeout value (in milliseconds) who checks that the system remains at the desired state (or condition, depending on the waiters used) for an specific time. The default value is 1000 milliseconds, but it could be changed by adding the property `RTEConnectionConfig.stableTimeoutMillis=<time_in_millis>` in *jmeter.properties* file. The Wait for Silent feature is not affected by this setting since it has an explicit field for specify this behavior.

### Example

Suppose the user wants to automate the following workflow in an AS400 server (TN5250 system):
1. Connect to the system *myAS400.net* and validate that the screens shows the "Welcome" message.
2. Fill the *user field* (which is in row 7 and column 53 of the screen) and the *password field* (which is in row 9, column 53 of the screen) and press *Enter* key. Validate that the screen shows the message "Login Successful".

To do this, first of all it's required an RTE Config element specifying the server url and the protocol (TN5250). Also are needed two RTE sampler's: one to make the connection and validate the Welcome screen, and the other to do the login.

The Test Plan will look like this:

![alt text](http://gitlab.abstracta.us/BZ/jmeter-rte-plugin/blob/master/readme/example_testplan.png "Test Plan")

The RTE Config element should specify the server url in *Server* field, and the protocol TN5250 in *Protocol* field like it's shown below:

![alt text](http://gitlab.abstracta.us/BZ/jmeter-rte-plugin/blob/master/readme/example_RTECongif.png "RTE Config")

"Just connect" option should be checked in first sampler to not send payload data or actions but get the Welcome screen after the connection. An assertion post processor should also be attached to it to validate the "Welcome" message.

![alt text](http://gitlab.abstracta.us/BZ/jmeter-rte-plugin/blob/master/readme/example_RTESampler1.png "RTE Sampler 1")

Finally, the second sampler should specify in the Payload grid the position on the screen and the value to put on both *user* and *password* fields. Besides, the action button *ENTER* should be selected to simulate the user pressing that key after filling the fields. It should has also an assert who checks for the ¨Login Successful¨ message.

![alt text](http://gitlab.abstracta.us/BZ/jmeter-rte-plugin/blob/master/readme/example_RTESampler2.png "RTE Sampler 2") 

## Contributing

If you find any issue or something that is not supported by this plugin, please report it and we will try to fix it. 

Otherwise you could [contribute](http://gitlab.abstracta.us/BZ/jmeter-rte-plugin/blob/master/CONTRIBUTING.md) to the project. 