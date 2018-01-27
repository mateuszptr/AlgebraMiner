package put.logs.amavis;

import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.Graph;
import guru.nidi.graphviz.model.MutableGraph;
import put.algebraminer.algorithm.LogImporter;
import put.algebraminer.event.LogModel;

import static guru.nidi.graphviz.model.Factory.*;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class Main {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String dir = "examples3";

		List<LogModel> models = LogImporter.processLogsFromDir(dir);
		MutableGraph g = AMAVis.generateVisGraph(models);

		try {
			Graphviz.fromGraph(g).width(2000).render(Format.PNG).toFile(new File("example/e2.png"));
			System.out.println(g);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
