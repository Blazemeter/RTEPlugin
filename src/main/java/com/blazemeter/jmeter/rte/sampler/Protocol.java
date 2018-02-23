package com.blazemeter.jmeter.rte.sampler;


import java.util.function.Supplier;
import com.blazemeter.jmeter.rte.core.RteProtocolClient;
import com.blazemeter.jmeter.rte.protocols.tn5250.Tn5250Client;

public enum Protocol {
	TN5250(Tn5250Client::new),
	TN3270(null);
	
	private final Supplier<RteProtocolClient> factory; 
	
	private Protocol(Supplier<RteProtocolClient> s){
		this.factory = s;
	}

	public RteProtocolClient createProtocolClient(){
		return factory.get();
	}
}

