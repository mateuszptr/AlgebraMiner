package put.algebraminer.algorithm;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;

import com.google.common.graph.Graph;

import put.algebraminer.event.AlgebraNode;
import put.algebraminer.event.AlgebraNode.Type;
import put.algebraminer.event.EventType;
import put.algebraminer.event.LogModel;
import put.algebraminer.event.SimpleEvent;

public class GlobalAlgebraMiner {
	public static Set<LogModel> findModelsByURI(String uri, List<LogModel> models) {
		Set<LogModel> modelsSet = new HashSet<>();
		
		for(LogModel model : models) {
			if(model.getUris().contains(uri)) {
				modelsSet.add(model);
			}
		}
		
		return modelsSet;
	}
	
	public static Set<XEvent> findXEvents(AlgebraNode node, XLog xlog) {
		Set<XEvent> xevents = new HashSet<>();
		
		SimpleEvent se = node.getEvent();
		
		for(XTrace trace : xlog) {
			for(XEvent event : trace) {
				SimpleEvent se2 = new SimpleEvent(event);
				if(se.equals(se2)) {
					xevents.add(event);
				}
			}
		}
		
		return xevents;
	}
	
	public static boolean complementaryEvents(XEvent sending, XEvent receiving) {
		
		SimpleEvent send = new SimpleEvent(sending);
		SimpleEvent recv = new SimpleEvent(receiving);
		
		if(EventType.fromString(send.getType()) != EventType.SEND || EventType.fromString(recv.getType()) != EventType.RECV) return false;
		if(send.getCid() == null || !send.getCid().equals(recv.getCid())) return false;
		
		if(send.getLpid().equals(recv.getRpid()) && recv.getLpid().equals(send.getRpid())) return true;
		//if(recv.getLpid().equals(send.getRpid())) return true;
		
		return false;
	}
	
	private static Set<XEvent> findComplementaryEvent(XEvent sendingEvent, XLog xlog) {
		Set<XEvent> complementaryEvents = new HashSet<>();
		
		for(XTrace trace : xlog) {
			for(XEvent event : trace) {
				if(complementaryEvents(sendingEvent, event)) {
					complementaryEvents.add(event);
				}
			}
		}
		
		return complementaryEvents;
	}
	
	public static Set<XEvent> findFittingEvents(Set<XEvent> sends, XLog xlog) {
		Set<XEvent> fittingEvents = new HashSet<>();
		
		for(XEvent event : sends) {
			fittingEvents.addAll(findComplementaryEvent(event, xlog));
		}
		
		return fittingEvents;
	}
	
	private static Set<SimpleEvent> setTransform(Set<XEvent> xevents) {
		Set<SimpleEvent> sevents = new HashSet<>();
		for(XEvent xevent : xevents) {
			sevents.add(new SimpleEvent(xevent));
		}
		return sevents;
	}
	
	public static Set<AlgebraNode> findFittingNodes(AlgebraNode node, LogModel model, List<LogModel> allModels) {
		Set<AlgebraNode> foundNodes = new HashSet<>();
		
		Set<XEvent> xevents = findXEvents(node, model.getXlog());
		Set<LogModel> fittingModels = findModelsByURI(node.getEvent().getDestination(), allModels);
		
		for(LogModel fittingModel : fittingModels) {
			Set<XEvent> fittingEvents = findFittingEvents(xevents, fittingModel.getXlog());
			Set<SimpleEvent> fittingSEvents = setTransform(fittingEvents);
			Graph<AlgebraNode> graph = fittingModel.getAg().getGraph();
			for(AlgebraNode nodeCandidate : graph.nodes()) {
				if(fittingSEvents.contains(nodeCandidate.getEvent())) {
					foundNodes.add(nodeCandidate);
				}
			}
		}
		
		return foundNodes;
	}
	
	public static void findConnections(List<LogModel> allModels) {
		for(LogModel model : allModels) {
			for(AlgebraNode node : model.getAg().getGraph().nodes()) {
				if(node.getType() == Type.SIMPLE && EventType.fromString(node.getEvent().getType()) == EventType.SEND) {
					node.getSendingTo().addAll(findFittingNodes(node, model, allModels));
				}
			}
		}
	}
}
