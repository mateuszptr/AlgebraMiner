package put.algebraminer.algorithm;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.deckfour.xes.in.XParser;
import org.deckfour.xes.in.XesXmlParser;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XAttributeMapImpl;
import org.deckfour.xes.model.impl.XLogImpl;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;

import put.algebraminer.event.AlgebraGraph;
import put.algebraminer.event.EventType;
import put.algebraminer.event.LogModel;
import put.algebraminer.event.SimpleEvent;

public class LogImporter {
//	public static Multimap<String, XTrace> importLogsFromDirectory(String directory) {
//		ListMultimap<String, XTrace> tracesByResource = MultimapBuilder.hashKeys().arrayListValues().build();
//		
//		XParser parser = new XesXmlParser();
//		try {
//			Files.list(Paths.get(directory)).filter(Files::isRegularFile).forEach(file->{
//				XLog log = parser.parse(file.toFile()).get(0);
//			});
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//		return null;
//	}
	
	
	public static Map<String, XLog> importLogsFromDir(String directory) throws IOException {
		Map<String, XLog> xlogMap = new HashMap<>();
		
		XParser parser = new XesXmlParser();
		Files.list(Paths.get(directory)).filter(Files::isRegularFile).forEach(file -> {
			XLog log = null;
			try {
				log = parser.parse(file.toFile()).get(0);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			for(XTrace trace : log) {
				String resource = trace.get(0).getAttributes().get("r_id").toString();
				XLog xlog;
				if(xlogMap.containsKey(resource)) {
					xlog = xlogMap.get(resource);
				} else {
					xlog = new XLogImpl(new XAttributeMapImpl());
					xlogMap.put(resource, xlog);
				}
				xlog.add(trace);
			}
		});
				
		return xlogMap;
	}
	
	private static LogModel populateURIs(LogModel model) {
		XLog xlog = model.getXlog();
		Set<String> uris = new HashSet<>();
		for(XTrace trace : xlog) {
			for(XEvent event : trace) {
				SimpleEvent sevent = new SimpleEvent(event);
				EventType type = EventType.fromString(sevent.getType());
				
				String uri = null;
				if(type == EventType.RECV) {
					uri = sevent.getDestination();
				}
				if(type == EventType.SEND) 
					uri = sevent.getSource();
				if(uri != null) {
					uris.add(uri);
				}
			}
		}
		
		model.setUris(uris);
		
		return model;
	}

	private static LogModel generateLogModel(XLog xlog, String res) {
		AlgebraGraph ag = AlgebraMiner.generateAlgebraGraph(xlog);
		LogModel model = new LogModel(xlog);
		model.setAg(ag);
		populateURIs(model);
		model.setResource(res);
		
		return model;
	}
	
	public static List<LogModel> generateLogModels(Map<String, XLog> xlogMap) {
		List<LogModel> models = new ArrayList<>();
		xlogMap.forEach((res,xlog)-> {
			LogModel model = generateLogModel(xlog, res);
			models.add(model);
		});
		
		return models;
	}
	
	public static void main(String[] args) {
		Map<String, XLog> xlogMap = null;
		try {
			xlogMap = importLogsFromDir("example_logs");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		List<LogModel> models = generateLogModels(xlogMap);
		for(LogModel model : models) {
			System.out.println(model.getAg().getGraph());
			System.out.println(model.getUris());
		}
		GlobalAlgebraMiner.findConnections(models);
		for(LogModel model : models) {
			System.out.println(model.getAg().getGraph());
			System.out.println(model.getUris());
		}
	}
}
