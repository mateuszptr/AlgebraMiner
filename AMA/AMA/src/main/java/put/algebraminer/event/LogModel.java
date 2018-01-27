package put.algebraminer.event;

import java.util.Set;

import org.deckfour.xes.model.XLog;

public class LogModel {
	private XLog xlog;
	private AlgebraGraph ag;
	private String resource;
	private Set<String> uris;
	
	
	public Set<String> getUris() {
		return uris;
	}


	public void setUris(Set<String> uris) {
		this.uris = uris;
	}


	public LogModel(XLog xlog) {
		super();
		this.xlog = xlog;
	}


	public XLog getXlog() {
		return xlog;
	}


	public void setXlog(XLog xlog) {
		this.xlog = xlog;
	}


	public AlgebraGraph getAg() {
		return ag;
	}


	public void setAg(AlgebraGraph ag) {
		this.ag = ag;
	}


	public String getResource() {
		return resource;
	}


	public void setResource(String resource) {
		this.resource = resource;
	}
	
	
}
