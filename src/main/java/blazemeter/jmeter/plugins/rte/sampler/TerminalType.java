package blazemeter.jmeter.plugins.rte.sampler;

public enum TerminalType {
	IBM_3278_2("IBM-3278-2","24x80",Protocol.TN3270),
	IBM_3278_2_E("IBM-3278-2-E","24x80",Protocol.TN3270),
	IBM_3278_3("IBM-3278-3","32x80",Protocol.TN3270),
	IBM_3278_3_E("IBM-3278-3-E","32x80",Protocol.TN3270),
	IBM_3278_4("IBM-3278-4","43x80",Protocol.TN3270),
	IBM_3278_4_E("IBM-3278-4-E","43x80",Protocol.TN3270),
	IBM_3278_5("IBM-3278-5","27x132",Protocol.TN3270),
	IBM_3278_5_E("IBM-3278-5-E","27x132",Protocol.TN3270),
	IBM_3179_2("IBM-3179-2", "24x80 color display",Protocol.TN5250),
	IBM_3477_FC("IBM-3477-FC", "27x132 color display",Protocol.TN5250);
	
	private final String type;
	private final String description;
	private final Protocol protocol;

	TerminalType(String type, String description, Protocol protocol){
		this.type = type;
		this.description = description;
		this.protocol = protocol;
	}
	
	public String getType() {
		return type;
	}

	public String getDescription() {
		return description;
	}

	public String toString() {
		return type + ": " + description;
	}

	public Protocol getProtocol() {
		return protocol;
	}

}
