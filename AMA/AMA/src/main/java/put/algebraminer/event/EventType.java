package put.algebraminer.event;

public enum EventType {
	LOCAL, SEND, RECV;
	
	public static EventType fromString(String type) {
		switch (type) {
		case "executionResponse":
		case "executionNotResponse":
		case "serviceReturn":
		case "externalReturn":
			return RECV;
		case "returningResponse":
		case "returningNotResponse":
		case "serviceCall":
		case "externalCall":
			return SEND;

		default:
			return LOCAL;
		}
	}
}
