package put.logs.amavis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import guru.nidi.graphviz.model.Factory;
import guru.nidi.graphviz.model.Graph;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.model.MutableNode;
import guru.nidi.graphviz.model.Node;
import put.algebraminer.event.AlgebraNode;
import put.algebraminer.event.LogModel;

public class AMAVis {
	private static Map<AlgebraNode, MutableNode> generateNodeMap(List<LogModel> models) {
		Map<AlgebraNode, MutableNode> nodeMap = new HashMap<>();

		for(LogModel model : models) {
			for(AlgebraNode anode : model.getAg().getGraph().nodes()) {
				MutableNode gnode = Factory.mutNode(anode.toString());
				nodeMap.put(anode, gnode);
			}
		}
		
		return nodeMap;
	}
	
	public static MutableGraph generateVisGraph(List<LogModel> models) {
		
		Map<AlgebraNode, MutableNode> nodeMap = generateNodeMap(models);
		
		for(LogModel model : models) {
			for(AlgebraNode anode : model.getAg().getGraph().nodes()) {
				MutableNode gnode = nodeMap.get(anode);
				for(AlgebraNode anode2 : model.getAg().getGraph().successors(anode)) {
					MutableNode gnode2 = nodeMap.get(anode2);
					gnode.addLink(gnode2);
				}
				for(AlgebraNode anode2 : anode.getSendingTo()) {
					MutableNode gnode2 = nodeMap.get(anode2);
					gnode.addLink(gnode2);
				}
			}
		}
		
		MutableGraph g = Factory.mutGraph();
		g.setDirected(true);
		for(MutableNode node : nodeMap.values()) {
			g = g.add(node);
		}
		
		
		return g;
	}
	
	public static MutableGraph generateVisGraph2(List<LogModel> models) {
		MutableGraph g = Factory.mutGraph().setDirected(true);
		List<MutableGraph> clusters = new ArrayList<>();
		for(LogModel model : models) {
			MutableGraph cluster = Factory.mutGraph(Integer.toString(model.hashCode())).setDirected(true).setCluster(true);
			for(AlgebraNode node : model.getAg().getGraph().nodes()) {
				for(AlgebraNode node2 : model.getAg().getGraph().successors(node)) {
					cluster.add(Factory.node(node.toString()).link(Factory.node(node2.toString())));
				}
				for(AlgebraNode node2 : node.getSendingTo()) {
					g.add(Factory.node(node.toString()).link(Factory.to(Factory.node(node2.toString())).with("constraint", true)));
				}
			}
			clusters.add(cluster);
		}
		for(MutableGraph cluster : clusters) {
			g.add(cluster);
		}
		return g;
	}
}
