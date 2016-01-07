package parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import visualize.BuildUML;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseException;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.ModifierSet;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.type.ReferenceType;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

public class UMLparser {
	
	public static HashMap<String,Map<String, List<String>>> Classdata = new HashMap<String,Map<String, List<String>>>();
	public static List<String> extendsRelations = new ArrayList<String>();
	public static List<String> implementsRelations = new ArrayList<String>();
	public static List<String> dependsRelations = new ArrayList<String>();
	public static List<String> usesRelations = new ArrayList<String>();
	public static List<String> classNames = new ArrayList<String>();
	public static List<String> interfaceNames = new ArrayList<String>();
	
	
	
	private static String currentClass;

	public static void main(String[] args) throws FileNotFoundException,
			IOException {
		
		try{
		if (args == null || args.length < 2){
			throw new InvalidArgumentException("Invalid Arguments \n Usage: UMLparser <classpath> <output directory>");			
		}
		}catch(InvalidArgumentException e){
			System.out.println(e.getMessage());
			System.exit(1);
		}

		File parent = new File(args[0].toString());
		 

		try {
			for (File file : parent.listFiles()) {
				
				FileInputStream in = new FileInputStream(file.getAbsoluteFile());

				CompilationUnit cu = null;
				try {
					// parse the file
					cu = JavaParser.parse(in);						
				} catch (ParseException e) {
					
					e.printStackTrace();
					
				} finally {
					
					try {
						in.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
				// visit and collect all components of interest
		        new MethodandAttributeVisitor().visit(cu, null);		        

				
			}
		} catch (IOException x) {
			System.err.println(x);
		}
		
		//adding all class/interface names to an arraylist
		Set<String> keys = Classdata.keySet();
		for(String k: keys){
			classNames.add(k.substring(k.lastIndexOf(" ")+1));			
		}
		
		ClassRelationFinder relfind = new ClassRelationFinder();
		relfind.findDepends();
		relfind.findUses();		
		
		new BuildUML().build(args[1]);

	}
	 
	
	/**
     * Simple visitor implementation for visiting MethodDeclaration nodes. 
     */
    private static class MethodandAttributeVisitor extends VoidVisitorAdapter {  
    	
    	private int currentAttributeModifier;
    	List<String> members = new ArrayList<String>();
    	List<String> methods = new ArrayList<String>();
    	HashSet parameters = new HashSet();
    	Map<String,List<String>> attrMap = new HashMap<String,List<String>>();
    	Boolean isVisitingMethod;
    	
    	@Override
		public void visit(FieldDeclaration n, Object arg) {
			
			currentAttributeModifier = n.getModifiers();
			String attrname = null;
			if (ModifierSet.isPrivate(currentAttributeModifier)
					|| ModifierSet.isPublic(currentAttributeModifier)) {
				attrname = attributeFullName(n);
			}	
			
			super.visit(n, arg);
			if(!(attrname == null)){
				members.add(attrname);				
//				System.out.println(attrname);
			}
			
		}
		
		private String attributeFullName(FieldDeclaration n){
			String temp;
			StringBuilder str1 = new StringBuilder();
			
			if (ModifierSet.isPrivate(currentAttributeModifier)){
				str1.append(" -");
			}else{
				str1.append(" +");				
			}
			temp = n.getVariables().toString();
			str1.append(temp.substring(1, temp.length()-1));
			str1.append(" : " + n.getType());			
			return str1.toString();
			
		}
		
		
		public void visit(ConstructorDeclaration n, Object arg){
			if(ModifierSet.isPublic(n.getModifiers())){
				StringBuilder string = new StringBuilder();
				string.append("+" + n.getName()+"(");
				if (n.getParameters() != null) {
					List<Parameter> params = n.getParameters();
					String pName;
					for (Parameter p : params) {
						String[] parts = p.toString().split(" ");
						pName = parts[parts.length - 1]; // getting the last element
						string.append(pName
								+ " : " + p.getType());
						if(p.getType() instanceof ReferenceType){
							parameters.add(p.getType().toString());
						}

					}
				}
				string.append(")");
				
				methods.add(string.toString());
			}
			
		}
		
		// catch all variable declarations inside a method
		public void visit(VariableDeclarationExpr n, Object arg){
			if(isVisitingMethod){
			parameters.add(n.getType().toString());
			}
			super.visit(n, arg);
			
		}	

        @Override
        public void visit(MethodDeclaration n, Object arg) { 
        	
        	String methodName;
        	Boolean isPublic = false;
        	isVisitingMethod = true;
            // here you can access the attributes of the method.
            // this method will be called for all methods in this 
            // CompilationUnit, including inner class methods
            
            if(ModifierSet.isPublic(n.getModifiers())){
            	// if the method is a setter or getter do not add to method list
            	// instead make the attribute that it sets or gets public
            	
            	String temp;
    			if(members.size() != 0){
    				for( int j = 0; j < members.size(); j++){	
    					temp = members.get(j);
    					if(n.getName().toLowerCase().contains(temp.toLowerCase().substring(3, (temp.indexOf(":")) - 1))){
    						members.set(j, temp.replace("-", "+"));
    						isPublic = true;
    						break;
    					}
    				}
    			}
    			if(!isPublic){
	            	methodName = methodFullName(n);         
	            	methods.add(methodName);
    			}
    			
    			super.visit(n, arg);
    			
    			isVisitingMethod = false;
    			
            }
			
        }
        
        
        private String methodFullName(MethodDeclaration n){
        	StringBuilder str1 = new StringBuilder();
			String temp;
			str1.append("+");
			str1.append(n.getName() +"( " );
			if (n.getParameters() != null) {
				List<Parameter> params = n.getParameters();
				String pName;
				for (Parameter p : params) {
					String[] parts = p.toString().split(" ");
					pName = parts[parts.length - 1]; // getting the last element
					str1.append(pName
							+ " : " + p.getType());
					if(p.getType() instanceof ReferenceType){
						parameters.add(p.getType().toString());
					}

				}
			}
			str1.append(")" + " : " +n.getType());
        	
			return str1.toString();
        	
        }
        
        
        @Override
        public void visit(ClassOrInterfaceDeclaration in, Object arg){
        	
        	if (in.isInterface()){
        		/*CuPrinter.ClassNames.put("Interface " + in.getName(),new HashMap());*/
        		currentClass = ("Interface " + in.getName());
        		interfaceNames.add(in.getName());
        		
        	} else{
        		/*CuPrinter.ClassNames.put("Class " + in.getName(),new HashMap());*/
        		currentClass = ("Class " + in.getName());
        		//extends relationship
        		if(in.getExtends() != null){
        			String temp= (in.getExtends().toString());
        			extendsRelations.add(in.getName() + " --|> " + temp.substring(1,temp.length()-1));
        		}
        		if(in.getImplements() != null){
        			Iterator it = in.getImplements().iterator();
        			while(it.hasNext())
        			implementsRelations.add(in.getName() + " ..|> " + it.next());
        		}
        	}
        	super.visit(in, arg);
        	
        	
        	attrMap.put("attribute", members);
        	attrMap.put("methods", methods);        	
        	attrMap.put("params",new ArrayList<String>(parameters));
        	
        	Classdata.put(currentClass, attrMap);
        	
        }       
       
        
    }
}
