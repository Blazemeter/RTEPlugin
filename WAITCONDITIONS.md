# RTE - Recorder Wait conditions.
To begin with the explication of how Wait Conditions Works in RTE-Recorder let start by naming the supported wait conditions and a very descriptive definition.


 _Wait for Silent_, _Wait for Sync_ , 

*Silent wait*: Basically wait for a period of time already defined.

*Sync wait*: This one will wait till the keyboard is unlocked.
 >When they are going to be added to our samplers?
  
 We have to separate in four cases:
 
 *Case 1*: We have connected to the server but we as the terminal emulator have not received any keyboard status change so the keyboard keeps locked. In this case the plugin will add a Silent Wait Conditions with the period of time between the connection and the next iteration.(Even if the iteration is from our side)
 
 *Case 2*: In these case we will change the scenario, now we have received multiple keyboards status changes, so the keyboard has been unlocked and locked few/many times but the difference between those locks and unlocks is grater than **Stable Perdiod** then the plugin will add a Silent Wait Condition in addition with a warning.
 
        Stable Period:  is a stable timeout value (in milliseconds) which specifies the time to wait for the emulator to remain at the desired state. The default value is 1000 milliseconds, but can be changed by adding the property RTEConnectionConfig.stableTimeoutMillis=<time_in_millis> in jmeter.properties file.
        
 *Case 3*: In this case we will invert the previous case, our keyboard state has been changed several times but now the difference between those status changes is lower than stable period and also the difference between last keyboard unlock and last event occurred (like att. keys, inputs, etc) is lower than Stable Period. Then a Sync Wait Condition will be added to that sampler.
 