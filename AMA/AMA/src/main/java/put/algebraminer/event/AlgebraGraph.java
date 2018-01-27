package put.algebraminer.event;

import com.google.common.graph.Graph;
import com.google.common.graph.MutableGraph;

public class AlgebraGraph {
	private MutableGraph<AlgebraNode> graph;
	private AlgebraNode start, end;
	public MutableGraph<AlgebraNode> getGraph() {
		return graph;
	}
	public void setGraph(MutableGraph<AlgebraNode> graph) {
		this.graph = graph;
	}
	public AlgebraNode getStart() {
		return start;
	}
	public void setStart(AlgebraNode start) {
		this.start = start;
	}
	public AlgebraNode getEnd() {
		return end;
	}
	public void setEnd(AlgebraNode end) {
		this.end = end;
	}
}
