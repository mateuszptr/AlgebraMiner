package put.algebraminer.event;

import org.deckfour.xes.model.XEvent;

public class SimpleEvent {

	private static final String ACTIVITY_NAME = "a_id";
	private static final String RESOURCE_NAME = "r_id";
	private static final String TYPE_NAME = "e_type";
	private static final String SOURCE_NAME = "source";
	private static final String DEST_NAME = "destination";
	private static final String LPID_NAME = "l_p_id";
	private static final String RPID_NAME = "r_p_id";
	private static final String CID_NAME = "c_id";
	
	private String activity;
	private String resource;
	private String type;
	private String source=null;
	private String destination=null;
	private String lpid=null;
	private String rpid=null;
	private String cid=null;

	private XEvent xevent;

	public SimpleEvent(XEvent xevent) {
		this.activity = xevent.getAttributes().get(ACTIVITY_NAME).toString();
		this.resource = xevent.getAttributes().get(RESOURCE_NAME).toString();
		this.type = xevent.getAttributes().get(TYPE_NAME).toString();
		if(xevent.getAttributes().get(SOURCE_NAME) != null)
			this.source = xevent.getAttributes().get(SOURCE_NAME).toString();
		if(xevent.getAttributes().get(DEST_NAME) != null)
			this.destination = xevent.getAttributes().get(DEST_NAME).toString();
		if(xevent.getAttributes().get(LPID_NAME) != null)
			this.lpid = xevent.getAttributes().get(LPID_NAME).toString();
		if(xevent.getAttributes().get(RPID_NAME) != null)
			this.rpid = xevent.getAttributes().get(RPID_NAME).toString();
		if(xevent.getAttributes().get(CID_NAME) != null)
			this.cid = xevent.getAttributes().get(CID_NAME).toString();
		
		this.xevent = xevent;
	}

	
	public String getDestination() {
		return destination;
	}

	public String getSource() {
		return source;
	}

	public String getType() {
		return type;
	}

	public String getLpid() {
		return lpid;
	}


	public String getRpid() {
		return rpid;
	}


	public String getCid() {
		return cid;
	}


	public String getActivity() {
		return activity;
	}


	public String getResource() {
		return resource;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((activity == null) ? 0 : activity.hashCode());
		result = prime * result + ((destination == null) ? 0 : destination.hashCode());
		result = prime * result + ((resource == null) ? 0 : resource.hashCode());
		result = prime * result + ((source == null) ? 0 : source.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SimpleEvent other = (SimpleEvent) obj;
		if (activity == null) {
			if (other.activity != null)
				return false;
		} else if (!activity.equals(other.activity))
			return false;
		if (destination == null) {
			if (other.destination != null)
				return false;
		} else if (!destination.equals(other.destination))
			return false;
		if (resource == null) {
			if (other.resource != null)
				return false;
		} else if (!resource.equals(other.resource))
			return false;
		if (source == null) {
			if (other.source != null)
				return false;
		} else if (!source.equals(other.source))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}


	@Override
	public String toString() {
		return activity;
	}

}
