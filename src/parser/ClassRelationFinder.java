package parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ClassRelationFinder {

	public void findDepends() {
		// Set<String> keys = UMLparser.Classdata.keySet();
		Map<String, List<String>> value = new HashMap<String, List<String>>();
		List<String> atvalues = new ArrayList<String>();
		
		String multirelation = " \"1\"" + " -- " + "\"*\" ";
		String unirelation = " -- ";
		for (Map.Entry<String, Map<String, List<String>>> str : UMLparser.Classdata
				.entrySet()) {
			//String objType = getFirstWord(str.getKey()," ");
			String ClassorInterface = getLastWord(str.getKey()," ");
			
			//System.out.println(ClassorInterface);
			value = str.getValue();
			atvalues = value.get("attribute");
			if (atvalues != null && atvalues.size()!= 0) {
				Iterator<String> iterator = atvalues.iterator();
				while (iterator.hasNext()) {
					String name = iterator.next();
					int t = 0, t1 = 0;
					name = getLastWord(name,":");
						if(checkContainsClass(name)){
							String temp;
							if (name.contains("Collection")	|| name.contains("Set") ) {
								
								//check if inverse relation exists and delete it because N multiplicity has higher priority
								if(UMLparser.dependsRelations.size() != 0){
									temp = (name.replaceAll("\\s","")).substring(name.indexOf("<") + 1 ,name.indexOf(">")) + unirelation + ClassorInterface + "\n";
									t = UMLparser.dependsRelations.indexOf(temp);
									if(t != -1){
										UMLparser.dependsRelations.remove(t);						
									}									
								}
								
								//write the new relation
								UMLparser.dependsRelations.add(ClassorInterface
										+ multirelation
										+ name.substring(name.indexOf("<") + 1,
												name.indexOf(">")) + "\n");
								iterator.remove(); // remove Dependency indicators from attribute list
								
							} else {
								//check if inverse relation exists and delete it Cuz N multiplicity has higher priority
								if(UMLparser.dependsRelations.size() != 0){
								   temp = name.replaceAll("\\s","") + multirelation + ClassorInterface + "\n";								   
								   t = UMLparser.dependsRelations.indexOf(temp);
								   temp = name.replaceAll("\\s","") + unirelation + ClassorInterface + "\n";
								   t1 = UMLparser.dependsRelations.indexOf(temp);
								}
								
							if ((t == -1 && t1 == -1) || UMLparser.dependsRelations.size() == 0) {
								UMLparser.dependsRelations.add(ClassorInterface
										+ unirelation + name.replaceAll("\\s","") + "\n");
							}
								iterator.remove(); // remove Dependency indicators from attribute list
							}
						}

				}

			}		

		}		

	}
	
	public void findUses(){
		
		Map<String, List<String>> value = new HashMap<String, List<String>>();
		List<String> usevalues = new ArrayList<String>();
		
		for (Map.Entry<String, Map<String, List<String>>> str : UMLparser.Classdata
				.entrySet()) {
			String objType = getFirstWord(str.getKey()," ");
			if (objType.contains("Interface") ) continue;
			String ClassorInterface = getLastWord(str.getKey()," ");
			
			//System.out.println(ClassorInterface);
			value = str.getValue();
			usevalues = value.get("params");
			if(usevalues!= null && usevalues.size() != 0){
				for(int i = 0; i < usevalues.size(); i++){
					if(UMLparser.interfaceNames.contains(usevalues.get(i))){
						UMLparser.usesRelations.add(ClassorInterface + " ..> " + usevalues.get(i));
					}
				}
				
			}
			
		}
		
	}
	
	public String getLastWord(String input, String separator) {

		String output = input.substring(input.lastIndexOf(separator) + 1);
		return output;

	}
	
	public String getFirstWord(String input, String separator) {

		String output = input.substring(0,input.indexOf(separator));
		return output;

	}
	
	public Boolean checkContainsClass(String name) {
		for (String token : UMLparser.classNames) {
			if (name.contains(token)) {
				return true;
			}
		}

		return false;

	}

}
