package visualize;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.StringBuilder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import parser.UMLparser;
import net.sourceforge.plantuml.FileFormat;
import net.sourceforge.plantuml.FileFormatOption;
import net.sourceforge.plantuml.SourceStringReader;

public class BuildUML {

	StringBuilder myString = new StringBuilder("@startuml\n");

	public BuildUML() {
	}

	public void build(String arg1) throws IOException {

		Map<String, List<String>> units = new HashMap<String, List<String>>();
		List<String> usesRelations = new ArrayList<>();

		for (Map.Entry<String, Map<String, List<String>>> str : UMLparser.Classdata
				.entrySet()) {

			String head = str.getKey();

			myString.append(head);

			myString.append("{\n");
			units = UMLparser.Classdata.get(head);
			//usesRelations = getUsesRelations(units);
			for (Map.Entry<String, List<String>> e : units.entrySet()) {
				if(e.getKey() != "params"){
					for (String str2 : e.getValue()) {
						myString.append(str2 + "\n");
					}
				}
			}

			myString.append("}\n");
			/*if(!(usesRelations.size() == 0)){
				for(String u: usesRelations){
					myString.append(head + "..>"+ u +"\n");
				}
			}*/

		}

		myString.append(String.join("\n", UMLparser.extendsRelations));
		myString.append("\n");
		myString.append(String.join("\n", UMLparser.implementsRelations));
		myString.append("\n");
		myString.append(String.join("\n", UMLparser.dependsRelations));
		myString.append("\n");
		myString.append(String.join("\n",UMLparser.usesRelations));

		myString.append("\n@enduml");
		//System.out.println(myString);

		SourceStringReader reader = new SourceStringReader(myString.toString());

		FileOutputStream output = new FileOutputStream(new File(arg1.toString()));
		
		System.out.println("Generating class diagram image in " + arg1);

		reader.generateImage(output,
				new FileFormatOption(FileFormat.PNG, false));
	}	
	

}
