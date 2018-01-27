package put.algebraminer.event;

import java.util.HashSet;
import java.util.Set;

public class AlgebraNode {
	public enum Type {
		SIMPLE, PLL_START, PLL_END, ALT_START, ALT_END, START, END
	}

	private SimpleEvent event;
	private AlgebraNode related;
	private Type type;
	private Set<AlgebraNode> sendingTo;

	public Set<AlgebraNode> getSendingTo() {
		return sendingTo;
	}

	public SimpleEvent getEvent() {
		return event;
	}

	public void setEvent(SimpleEvent event) {
		this.event = event;
	}

	public AlgebraNode getRelated() {
		return related;
	}

	public void setRelated(AlgebraNode related) {
		this.related = related;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public AlgebraNode(SimpleEvent event) {
		super();
		this.event = event;
		this.type = Type.SIMPLE;
		this.sendingTo = new HashSet<>();
	}

	public AlgebraNode(Type type) {
		super();
		this.event = null;
		this.type = type;
		this.sendingTo = new HashSet<>();
	}

	@Override
	public String toString() {
		if(!sendingTo.isEmpty()) {
			return "(" + event + "$" + sendingTo + ")@" + hashCode();
		}
		
		if(type == Type.SIMPLE)
			return "(" + event + ")@" + hashCode() ;
		else
			return "[" + type + "]@" + hashCode();
	}

	
	
}
