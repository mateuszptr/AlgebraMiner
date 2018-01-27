package put.algebraminer.algorithm;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import org.deckfour.xes.in.XParser;
import org.deckfour.xes.in.XesXmlParser;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;

import com.google.common.collect.Lists;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Sets;
import com.google.common.graph.EndpointPair;
import com.google.common.graph.Graph;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.Graphs;
import com.google.common.graph.MutableGraph;
import com.google.common.graph.Traverser;

import put.algebraminer.event.AlgebraGraph;
import put.algebraminer.event.AlgebraNode;
import put.algebraminer.event.AlgebraNode.Type;
import put.algebraminer.event.EventPair;
import put.algebraminer.event.SimpleEvent;

public class AlgebraMiner {
	
	public static void main(String[] args) {
		XLog log = loadLog("example.xes");
		Set<List<SimpleEvent>> utraces = uniqueTraces(log);
		System.out.println(utraces);
		Set<EventPair> pllSet = genPllSet(utraces);
		System.out.println(pllSet);
		List<MutableGraph<SimpleEvent>> initClusters = genInitClusters(utraces);
		System.out.println(initClusters);
		Set<Graph<SimpleEvent>> pllClusters = genPllClusters(initClusters, pllSet);
		System.out.println(pllClusters);
		AlgebraGraph ag = genInitAlgebra(pllClusters);
		System.out.println(ag.getGraph());
		reduceModel(ag);
		System.out.println(ag.getGraph());
		
	}
	
	public static AlgebraGraph generateAlgebraGraph(XLog log) {
		Set<List<SimpleEvent>> utraces = uniqueTraces(log);
		Set<EventPair> pllSet = genPllSet(utraces);
		List<MutableGraph<SimpleEvent>> initClusters = genInitClusters(utraces);
		Set<Graph<SimpleEvent>> pllClusters = genPllClusters(initClusters, pllSet);
		AlgebraGraph ag = genInitAlgebra(pllClusters);
		reduceModel(ag);
		return ag;
	}
	
	public static XLog loadLog(String filename) {
		try {
			InputStream is = new FileInputStream(filename);
			XParser parser = new XesXmlParser();
			XLog xlog = parser.parse(is).get(0);
			return xlog;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	private static List<SimpleEvent> simplifyTrace(XTrace trace) {
		List<SimpleEvent> newTrace = new ArrayList<>(trace.size());
		for (XEvent event : trace) {
			newTrace.add(new SimpleEvent(event));
		}

		return newTrace;
	}

	public static Set<List<SimpleEvent>> uniqueTraces(XLog log) {
		Set<List<SimpleEvent>> traces = new HashSet<>();

		for (XTrace trace : log) {
			List<SimpleEvent> newTrace = simplifyTrace(trace);
			traces.add(newTrace);
		}

		return traces;

	}

	public static void addTraceToPllSet(Set<EventPair> set, List<SimpleEvent> trace) {
		Iterator<SimpleEvent> it = trace.iterator();

		SimpleEvent curr = null, prev = null;

		if (it.hasNext()) {
			curr = it.next();
		}

		while (it.hasNext()) {
			prev = curr;
			curr = it.next();

			EventPair pair = new EventPair(prev, curr);
			set.add(pair);
		}
	}

	public static Set<EventPair> genPllSet(Set<List<SimpleEvent>> traces) {
		Set<EventPair> set = new HashSet<>();

		for (List<SimpleEvent> trace : traces) {
			addTraceToPllSet(set, trace);
		}

		Iterator<EventPair> it = set.iterator();
		while (it.hasNext()) {
			EventPair pair = it.next();
			if (!set.contains(new EventPair(pair.last, pair.first))) {
				it.remove();
			}
		}

		return set;
	}

	public static MutableGraph<SimpleEvent> genCluster(List<SimpleEvent> trace) {
		MutableGraph<SimpleEvent> graph = GraphBuilder.directed().build();

		Iterator<SimpleEvent> it = trace.iterator();
		SimpleEvent curr = null, prev = null;
		if (it.hasNext()) {
			curr = it.next();
		}

		while (it.hasNext()) {
			prev = curr;
			curr = it.next();

			graph.putEdge(prev, curr);
		}
		return graph;
	}

	public static List<MutableGraph<SimpleEvent>> genInitClusters(Set<List<SimpleEvent>> traces) {
		List<MutableGraph<SimpleEvent>> clusters = new LinkedList<>();

		for (List<SimpleEvent> trace : traces) {
			clusters.add(genCluster(trace));
		}

		return clusters;
	}

	public static MutableGraph<SimpleEvent> genPllCluster(MutableGraph<SimpleEvent> cluster, Set<EventPair> pllSet) {

		boolean changed = false;

		do {
			changed = false;
			Set<EndpointPair<SimpleEvent>> edges = cluster.edges();
			Set<EventPair> pairsToPll = new HashSet<>();

			for (EndpointPair<SimpleEvent> ep : edges) {
				EventPair pair = new EventPair(ep.source(), ep.target());
				if (pllSet.contains(pair)) {
					pairsToPll.add(pair);
				}
			}

			for (EventPair pair : pairsToPll) {
				changed = true;
				cluster.removeEdge(pair.first, pair.last);
				for (SimpleEvent event : cluster.predecessors(pair.first)) {
					cluster.putEdge(event, pair.last);
				}
				for (SimpleEvent event : cluster.successors(pair.last)) {
					cluster.putEdge(pair.first, event);
				}
			}

		} while (changed);

		return cluster;
	}

	public static Set<Graph<SimpleEvent>> genPllClusters(List<MutableGraph<SimpleEvent>> clusters,
			Set<EventPair> pllSet) {

		Set<Graph<SimpleEvent>> pllClusters = new HashSet<>();

		for (MutableGraph<SimpleEvent> cluster : clusters) {
			pllClusters.add(genPllCluster(cluster, pllSet));
		}

		return pllClusters;

	}

	private static Set<SimpleEvent> startingEvents(Graph<SimpleEvent> cluster) {
		Set<SimpleEvent> result = new HashSet<>();

		for (SimpleEvent event : cluster.nodes()) {
			if (cluster.predecessors(event).isEmpty()) {
				result.add(event);
			}
		}

		return result;
	}

	public static List<List<SimpleEvent>> genAllTraces(Graph<SimpleEvent> pllCluster) {

		List<List<SimpleEvent>> traces = new ArrayList<>();

		Set<SimpleEvent> se = startingEvents(pllCluster);

		for (SimpleEvent startingEvent : se) {
			Stack<SimpleEvent> trace = new Stack<>();
			Stack<Iterator<SimpleEvent>> its = new Stack<>();

			trace.push(startingEvent);
			its.push(pllCluster.successors(startingEvent).iterator());

			while (!trace.isEmpty()) {

				if (pllCluster.successors(trace.peek()).isEmpty()) {
					traces.add((List<SimpleEvent>) trace.clone());
				}

				SimpleEvent event = trace.peek();
				Iterator<SimpleEvent> it = its.peek();

				if (it.hasNext()) {
					SimpleEvent next = it.next();
					trace.push(next);
					its.push(pllCluster.successors(next).iterator());
				} else {
					trace.pop();
					its.pop();
				}
			}
		}

		return traces;
	}

	private static void emplaceClusterGroup(AlgebraNode startPll, AlgebraNode endPll, AlgebraGraph ag,
			Graph<SimpleEvent> cluster) {
		MutableGraph<AlgebraNode> graph = ag.getGraph();
		List<List<SimpleEvent>> traces = genAllTraces(cluster);

		for (List<SimpleEvent> trace : traces) {
			Iterator<SimpleEvent> it = trace.iterator();
			AlgebraNode prev = startPll;
			while (it.hasNext()) {
				AlgebraNode curr = new AlgebraNode(it.next());
				graph.putEdge(prev, curr);
				prev = curr;
			}
			graph.putEdge(prev, endPll);
		}
	}

	private static Set<AlgebraNode> findNodes(AlgebraGraph ag, AlgebraNode.Type type) {
		Set<AlgebraNode> set = new HashSet<>();

		for (AlgebraNode node : ag.getGraph().nodes()) {
			if (node.getType() == type) {
				set.add(node);
			}
		}

		return set;
	}

	private static Multiset<SimpleEvent> getNodesClasses(Set<AlgebraNode> nodes) {
		Multiset<SimpleEvent> mset = HashMultiset.create();
		
		for(AlgebraNode node : nodes) {
			if(node.getEvent() != null && node.getType() == Type.SIMPLE)
				mset.add(node.getEvent());
		}
		
		
		
		return mset;
	}
	
	private static Set<AlgebraNode> getNodesByEvent(Set<AlgebraNode> set, SimpleEvent event) {
		Set<AlgebraNode> newSet = new HashSet<>();
		
		for(AlgebraNode node : set) {
			if(node.getEvent().equals(event)) {
				newSet.add(node);
			}
		}
		
		return newSet;
	}

	private static void createNewSpecialNodeDown(AlgebraGraph ag, AlgebraNode starter, Set<AlgebraNode> children) {
		MutableGraph<AlgebraNode> graph = ag.getGraph();
		
		AlgebraNode special = new AlgebraNode(starter.getType());
		AlgebraNode related = new AlgebraNode(relatedType(starter.getType()));
		graph.putEdge(starter, special);
		for(AlgebraNode child : children) {
			graph.removeEdge(starter, child);
			graph.putEdge(special, child);
			
			Traverser<AlgebraNode> traverser = Traverser.forGraph(graph);
			Iterator<AlgebraNode> it = traverser.depthFirstPreOrder(child).iterator();
			AlgebraNode prev=child;
			while(it.hasNext()) {
				AlgebraNode curr = it.next();
				if(curr.getRelated() == starter) {
					break;
				}
				prev = curr;
			}
			graph.putEdge(prev, related);
			graph.removeEdge(prev, starter.getRelated());
		}
		graph.putEdge(related, starter.getRelated());
	}
	
	private static void createNewSpecialNodeUp(AlgebraGraph ag, AlgebraNode starter, Set<AlgebraNode> children) {
		MutableGraph<AlgebraNode> graph = ag.getGraph();
		
		AlgebraNode special = new AlgebraNode(starter.getType());
		AlgebraNode related = new AlgebraNode(relatedType(starter.getType()));
		graph.putEdge(special, starter);
		for(AlgebraNode child : children) {
			graph.removeEdge(child, starter);
			graph.putEdge(child, special);
			
			Traverser<AlgebraNode> traverser = Traverser.forGraph(Graphs.transpose(graph));
			Iterator<AlgebraNode> it = traverser.depthFirstPreOrder(child).iterator();
			AlgebraNode prev=child;
			while(it.hasNext()) {
				AlgebraNode curr = it.next();
				if(curr.getRelated() == starter) {
					break;
				}
				prev = curr;
			}
			graph.putEdge(related, prev);
			graph.removeEdge(starter.getRelated(), prev);
		}
		graph.putEdge(starter.getRelated(),related);
	}
	
	private static AlgebraNode.Type relatedType(AlgebraNode.Type type) {
		switch(type){
		case ALT_START: return Type.ALT_END;
		case ALT_END: return Type.ALT_START;
		case PLL_START: return Type.PLL_END;
		case PLL_END: return Type.PLL_START;
		default: return Type.SIMPLE;
		}
	}
	
	private static boolean goDownByType(AlgebraNode.Type type) {
		switch (type) {
		case ALT_START:
		case PLL_START: return true;
		default: return false;
		}
	}
	
	private static Set<AlgebraNode> specialChildren(AlgebraGraph ag, Set<AlgebraNode> children) {
		Graph<AlgebraNode> graph = ag.getGraph();
		Set<AlgebraNode> result = new HashSet<>();
		
		for(AlgebraNode child : children) {
			if(child.getType() != Type.SIMPLE) result.add(child);
		}
		
		return result;
	}
	
	private static boolean pushDown(AlgebraGraph ag, AlgebraNode.Type type) {
		boolean changed = false;
		Set<AlgebraNode> nodes = findNodes(ag, type);
		
		MutableGraph<AlgebraNode> graph = ag.getGraph();
			

		for (AlgebraNode node : nodes) {
			Set<AlgebraNode> children = graph.successors(node);
			if (children.size() == 1 && children.iterator().next().getRelated() == node) {
				AlgebraNode adj = children.iterator().next();
				AlgebraNode first = graph.predecessors(node).iterator().next();
				AlgebraNode last = graph.successors(adj).iterator().next();
				graph.removeNode(node);
				graph.removeNode(adj);
				graph.putEdge(first, last);
				changed = true;
			} else {
				Multiset<SimpleEvent> classes = getNodesClasses(graph.successors(node));
				if(classes.entrySet().size() == 1 && specialChildren(ag, children).isEmpty()) {
					AlgebraNode pushed = new AlgebraNode(classes.iterator().next());
					AlgebraNode pred = graph.predecessors(node).iterator().next();
					graph.putEdge(pred, pushed);
					graph.putEdge(pushed, node);
					graph.removeEdge(pred, node);
					Set<AlgebraNode> children2 = new HashSet<>();
					children2.addAll(children);
					for(AlgebraNode child : children2) {
						AlgebraNode succ = graph.successors(child).iterator().next();
						graph.putEdge(node, succ);
						graph.removeNode(child);
					}
					changed = true;
				} else {
					Iterator<SimpleEvent> it = classes.elementSet().iterator();
					while(it.hasNext()) {
						SimpleEvent e = it.next();
						if(classes.count(e) <= 1) {
							it.remove();
						}
					}
					for(SimpleEvent event : classes.elementSet()) {
						Set<AlgebraNode> nodesByEvent = getNodesByEvent(children, event);
						
						createNewSpecialNodeDown(ag, node, nodesByEvent);
						changed = true;
					}
				}
			}
		}

		return changed;
	}
	
	private static boolean pushUp(AlgebraGraph ag, AlgebraNode.Type type) {
		boolean changed = false;
		Set<AlgebraNode> nodes = findNodes(ag, type);
		
		MutableGraph<AlgebraNode> graph = ag.getGraph();
			

		for (AlgebraNode node : nodes) {
			Set<AlgebraNode> children = graph.predecessors(node);
			if (children.size() == 1 && children.iterator().next().getRelated() == node) {
				AlgebraNode adj = children.iterator().next();
				AlgebraNode first = graph.successors(node).iterator().next();
				AlgebraNode last = graph.predecessors(adj).iterator().next();
				graph.removeNode(node);
				graph.removeNode(adj);
				graph.putEdge(last, first);
				changed = true;
			} else {
				Multiset<SimpleEvent> classes = getNodesClasses(graph.predecessors(node));
				if(classes.entrySet().size() == 1 && specialChildren(ag, children).isEmpty()) {
					AlgebraNode pushed = new AlgebraNode(classes.iterator().next());
					AlgebraNode pred = graph.successors(node).iterator().next();
					graph.putEdge(pushed,pred);
					graph.putEdge(node, pushed);
					graph.removeEdge(node, pred);
					Set<AlgebraNode> children2 = new HashSet<>();
					children2.addAll(children);
					for(AlgebraNode child : children2) {
						AlgebraNode succ = graph.predecessors(child).iterator().next();
						graph.putEdge(succ, node);
						graph.removeNode(child);
					}
					changed = true;
				} else {
					Iterator<SimpleEvent> it = classes.elementSet().iterator();
					while(it.hasNext()) {
						SimpleEvent e = it.next();
						if(classes.count(e) <= 1) {
							it.remove();
						}
					}
					for(SimpleEvent event : classes.elementSet()) {
						Set<AlgebraNode> nodesByEvent = getNodesByEvent(children, event);
						
						createNewSpecialNodeUp(ag, node, nodesByEvent);
						changed = true;
					}
				}
			}
		}

		return changed;
	}

	public static AlgebraGraph reduceModel(AlgebraGraph ag) {

		boolean changed = false, change = false;
		do {
			change=false;
			changed = pushDown(ag, Type.PLL_START);
			change = changed || change;
			changed = pushUp(ag, Type.PLL_END);
			change = changed || change;
			changed = pushDown(ag, Type.ALT_START);
			change = changed || change;
			changed = pushUp(ag, Type.ALT_END);
			change = changed || change;
		} while(change);
		return ag;
	}

	public static AlgebraGraph genInitAlgebra(Set<Graph<SimpleEvent>> pllClusters) {

		AlgebraGraph ag = new AlgebraGraph();
		ag.setGraph(GraphBuilder.directed().build());
		MutableGraph<AlgebraNode> graph = ag.getGraph();
		AlgebraNode startNode = new AlgebraNode(Type.START);
		AlgebraNode endNode = new AlgebraNode(Type.END);

		AlgebraNode altStart = new AlgebraNode(Type.ALT_START);
		AlgebraNode altEnd = new AlgebraNode(Type.ALT_END);

		altStart.setRelated(altEnd);
		altEnd.setRelated(altStart);

		graph.putEdge(startNode, altStart);
		graph.putEdge(altEnd, endNode);

		for (Graph<SimpleEvent> cluster : pllClusters) {
			AlgebraNode pllStart = new AlgebraNode(Type.PLL_START);
			AlgebraNode pllEnd = new AlgebraNode(Type.PLL_END);
			pllStart.setRelated(pllEnd);
			pllEnd.setRelated(pllStart);

			emplaceClusterGroup(pllStart, pllEnd, ag, cluster);
			
			graph.putEdge(altStart, pllStart);
			graph.putEdge(pllEnd, altEnd);
		}

		return ag;
	}
}
