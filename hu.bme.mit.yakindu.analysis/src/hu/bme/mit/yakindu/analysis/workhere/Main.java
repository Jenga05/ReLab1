package hu.bme.mit.yakindu.analysis.workhere;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;
import java.util.List;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EObject;
import org.junit.Test;
import org.yakindu.base.types.Direction;
import org.yakindu.sct.model.sgraph.State;
import org.yakindu.sct.model.sgraph.Statechart;
import org.yakindu.sct.model.sgraph.Transition;
import org.yakindu.sct.model.stext.stext.EventDefinition;
import org.yakindu.sct.model.stext.stext.InterfaceScope;
import org.yakindu.sct.model.stext.stext.VariableDefinition;

import hu.bme.mit.model2gml.Model2GML;
import hu.bme.mit.yakindu.analysis.RuntimeService;
import hu.bme.mit.yakindu.analysis.TimerService;
import hu.bme.mit.yakindu.analysis.example.ExampleStatemachine;
import hu.bme.mit.yakindu.analysis.example.IExampleStatemachine;
import hu.bme.mit.yakindu.analysis.modelmanager.ModelManager;

public class Main {
	@Test
	public void test() {
		main(new String[0]);
	}
	
	public static void main(String[] args) {
		ModelManager manager = new ModelManager();
		Model2GML model2gml = new Model2GML();
		
		// Loading model
		EObject root = manager.loadModel("model_input/example.sct");
		
		// Reading model
		Statechart s = (Statechart) root;
		TreeIterator<EObject> iterator = s.eAllContents();
		

		ArrayList<String> variables = new ArrayList<String>();
		ArrayList<String> events = new ArrayList<String>();
		
		while (iterator.hasNext()) {
			EObject content = iterator.next();

			if(content instanceof VariableDefinition) {
				VariableDefinition variable = (VariableDefinition) content;
				String name = variable.getName();
				variables.add(name);
			}
			if(content instanceof EventDefinition) {
				EventDefinition event = (EventDefinition) content;
				if(event.getDirection() == Direction.IN)
					events.add(event.getName());
			}
		}
		System.out.println("public static void print(IExampleStatemachine s) {");
		System.out.println(
				"public class RunStatechart {\r\n" + 
				"	\r\n" + 
				"	public static void main(String[] args) throws IOException {\r\n" + 
				"		ExampleStatemachine s = new ExampleStatemachine();\r\n" + 
				"		s.setTimer(new TimerService());\r\n" + 
				"		RuntimeService.getInstance().registerStatemachine(s, 200);\r\n" + 
				"		\r\n" + 
				"		s.init();\r\n" + 
				"		s.enter();\r\n" + 
				"		\r\n" + 
				"		String input = \"\";\r\n" + 
				"		Scanner sc = new Scanner(System.in);\r\n" + 
				"		while(!input.equals(\"exit\")) {\r\n" + 
				"			switch (input){\r\n" );
		
		for(String name : events) {
			String upperName = name.substring(0,1).toUpperCase() + name.substring(1);
			System.out.println(
					"			case \""+name+"\": 	s.raise"+upperName+"();\r\n" + 
					"					s.runCycle();\r\n" + 
					"					break;\r\n");
		}
		System.out.println(
				"			print(s);\r\n" + 
				"			input = sc.nextLine();\r\n" + 
				"		}\r\n" + 
				"		System.exit(0);\r\n" + 
				"	}\r\n" + 
				"\r\n" + 
				"	public static void print(IExampleStatemachine s) {\r\n" );
		
		for(String variable : variables) {
			String upperName = variable.substring(0,1).toUpperCase() + variable.substring(1);
				System.out.println(
				"		System.out.println(\""+upperName.substring(0,1)+" = \" + s.getSCInterface().get"+upperName+"());"); 
		}
		System.out.println(
				"	}\r\n" + 
				"}");
		
		// Transforming the model into a graph representation
		String content = model2gml.transform(root);
		// and saving it
		manager.saveFile("model_output/graph.gml", content);
	}
}
