package put.algebraminer.algorithm;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.deckfour.xes.in.XParser;
import org.deckfour.xes.in.XesXmlParser;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XAttributeMapImpl;
import org.deckfour.xes.model.impl.XLogImpl;
import org.deckfour.xes.out.XesXmlSerializer;

public class LogConcat {

	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String directory = args[0];
		String filename = args[1];
		
		XLog concatLog = new XLogImpl(new XAttributeMapImpl());
		XParser parser = new XesXmlParser();
		try {
			Files.list(Paths.get(directory)).filter(Files::isRegularFile).forEach(file -> {
				XLog log = null;
				try {
					log = parser.parse(file.toFile()).get(0);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				for(XTrace trace : log) {
					concatLog.add(trace);
				}
			});
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		XesXmlSerializer serializer = new XesXmlSerializer();
		try {
			serializer.serialize(concatLog, new BufferedOutputStream(new FileOutputStream(filename)));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
