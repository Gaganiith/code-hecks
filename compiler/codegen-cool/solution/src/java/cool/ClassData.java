package cool;
import java.util.*;
import java.util.Map.Entry;
import cool.AST;

public class ClassData{

    // Hashmap from class name to class structure
    HashMap<String,ClassStructure> classes_list;

    // Hashmap for class height in inheritance graph
    HashMap<String, Integer> height;

    // for error messages
    Semantic error;
  

    ClassData(){

        classes_list = new HashMap<String,ClassStructure>();

        height = new HashMap<String, Integer>();

        error = new Semantic();

        addBasicClasses();

    }

    public void addBasicClasses(){
        // Insert Basic Classes:
        // Note : height of the class is the level of class in the inheritance hierarchy
        // 1. Object - methods : abort,type_name and copy , height : 0.
        // 2. IO - methods : out_string,out_int,in_string and in_int , height : 1
        // 3. Int - methods : NO explicit METHODS , height : 1
        // 4. Bool - methods : NO explicit METHODS ,height : 1
        // 5. String - methods : length,concat and substr, height : 1

       //Inserting Object class and its methods to  methodlist of the class Structure 

		classes_list.put("Object", new ClassStructure("Object", null, new HashMap<String, AST.attr>(), new HashMap <String, AST.method>(),new HashMap<String,String>(),0));
		classes_list.get("Object").methodlist.put("abort", new AST.method("abort", new ArrayList<AST.formal>(), "Object", new AST.no_expr(0), 0));
		classes_list.get("Object").methodlist.put("type_name", new AST.method("type_name", new ArrayList<AST.formal>(), "String", new AST.no_expr(0), 0));
        classes_list.get("Object").methodlist.put("copy", new AST.method("copy", new ArrayList<AST.formal>(), "Object", new AST.no_expr(0), 0));
        classes_list.get("Object").methodToMethodName.put("abort", "@_ZN6Object5abort");
		classes_list.get("Object").methodToMethodName.put("type_name", "@_ZN6Object9type_name");
		classes_list.get("Object").methodToMethodName.put("copy", "@_ZN6Object4copy");
        height.put("Object", 0); 
        
		//inserting IO class and its methods into hash table of class_list
		List <AST.formal> int_formals = new ArrayList<AST.formal>();
		int_formals.add(new AST.formal("out_string", "String", 0));
		List <AST.formal> string_formals = new ArrayList<AST.formal>();
		string_formals.add(new AST.formal("out_int", "Int", 0));		
		HashMap <String, String> methodName = new HashMap <String, String>();
		methodName.putAll(classes_list.get("Object").methodToMethodName);
		methodName.put("out_string", "@_ZN2IO10out_string");
		methodName.put("in_string", "@_ZN2IO7out_int");
		methodName.put("in_string", "@_ZN2IO9in_string");
		methodName.put("in_int", "@_ZN2IO9in_int");
		classes_list.put("IO", new ClassStructure("IO", "Object", new HashMap<String, AST.attr>(), new HashMap<String, AST.method>(),methodName,0));
		classes_list.get("IO").methodlist.put("out_string", new AST.method("out_string", int_formals, "IO", new AST.no_expr(0), 0));
		classes_list.get("IO").methodlist.put("out_int", new AST.method("out_int", string_formals, "IO", new AST.no_expr(0), 0));
		classes_list.get("IO").methodlist.put("in_string", new AST.method("in_string", new ArrayList<AST.formal>(), "String", new AST.no_expr(0), 0));
		classes_list.get("IO").methodlist.put("in_int", new AST.method("in_int", new ArrayList<AST.formal>(), "Int", new AST.no_expr(0), 0));
        // inherited methods of Object class
        classes_list.get("IO").methodlist.put("abort", new AST.method("abort", new ArrayList<AST.formal>(), "Object", new AST.no_expr(0), 0));
		classes_list.get("IO").methodlist.put("type_name", new AST.method("type_name", new ArrayList<AST.formal>(), "String", new AST.no_expr(0), 0));
		classes_list.get("IO").methodlist.put("copy", new AST.method("copy", new ArrayList<AST.formal>(), "Object", new AST.no_expr(0), 0));
        height.put("IO", 1);
        
        //inserting Int class to the class_list
        HashMap <String, String> methodIntName = new HashMap <String, String>();
		methodIntName.putAll(classes_list.get("Object").methodToMethodName);
		classes_list.put("Int", new ClassStructure("Int", "Object", new HashMap<String, AST.attr>(), new HashMap<String, AST.method>(),methodIntName,4));
        // inherited methods of Object class
        classes_list.get("Int").methodlist.put("abort", new AST.method("abort", new ArrayList<AST.formal>(), "Object", new AST.no_expr(0), 0));
		classes_list.get("Int").methodlist.put("type_name", new AST.method("type_name", new ArrayList<AST.formal>(), "String", new AST.no_expr(0), 0));
		classes_list.get("Int").methodlist.put("copy", new AST.method("copy", new ArrayList<AST.formal>(), "Object", new AST.no_expr(0), 0));
        height.put("Int", 1);
        
        //Inserting Bool class and its methods to the class_list 
        HashMap <String, String> methodBoolName = new HashMap <String, String>();
		methodBoolName.putAll(classes_list.get("Object").methodToMethodName);
		classes_list.put("Bool", new ClassStructure("Bool", "Object", new HashMap<String, AST.attr>(), new HashMap<String, AST.method>(),methodBoolName,1));
        // inherited methods of Object class
        classes_list.get("Bool").methodlist.put("abort", new AST.method("abort", new ArrayList<AST.formal>(), "Object", new AST.no_expr(0), 0));
		classes_list.get("Bool").methodlist.put("type_name", new AST.method("type_name", new ArrayList<AST.formal>(), "String", new AST.no_expr(0), 0));
		classes_list.get("Bool").methodlist.put("copy", new AST.method("copy", new ArrayList<AST.formal>(), "Object", new AST.no_expr(0), 0));
		height.put("Bool", 1);
        
        // inserting String class to the class list
		List<AST.formal> f_concat = new ArrayList<AST.formal>();
		f_concat.add(new AST.formal("s", "String", 0));
		
		List<AST.formal> f_substr = new ArrayList<AST.formal>();
		f_substr.add(new AST.formal("i", "Int", 0));
		f_substr.add(new AST.formal("l", "Int", 0));
		HashMap <String, String> methodStringName = new HashMap <String, String> ();
		methodStringName.putAll(classes_list.get("Object").methodToMethodName);
		methodStringName.put("length", "@_ZN6String6length");
		methodStringName.put("concat", "@_ZN6String6concat");
		methodStringName.put("substr", "@_ZN6String6substr");
		classes_list.put("String", new ClassStructure("String", "Object", new HashMap<String, AST.attr>(), new HashMap<String, AST.method>(),methodStringName,8));
		classes_list.get("String").methodlist.put("length", new AST.method("length", new ArrayList<AST.formal>(), "Int", new AST.no_expr(0), 0));
		classes_list.get("String").methodlist.put("concat", new AST.method("concat", f_concat, "String", new AST.no_expr(0), 0));
		classes_list.get("String").methodlist.put("substr", new AST.method("substr", f_substr, "String", new AST.no_expr(0), 0));
		// inherited methods of Object class
		classes_list.get("String").methodlist.put("abort", new AST.method("abort", new ArrayList<AST.formal>(), "Object", new AST.no_expr(0), 0));
		classes_list.get("String").methodlist.put("type_name", new AST.method("type_name", new ArrayList<AST.formal>(), "String", new AST.no_expr(0), 0));
		classes_list.get("String").methodlist.put("copy", new AST.method("copy", new ArrayList<AST.formal>(), "Object", new AST.no_expr(0), 0));
        
        height.put("String", 1);
        
        
    }

   
    public void addClassDetails(AST.class_ cl){

        // Create a class structure object
        ClassStructure clsStructure = new ClassStructure(cl.name, cl.parent, classes_list.get(cl.parent).attrlist, classes_list.get(cl.parent).methodlist,classes_list.get(cl.parent).methodToMethodName,0);
        // Attribute list for a class
        HashMap<String, AST.attr> classAttributes = new HashMap<String, AST.attr>();
        // Method list for a class
        HashMap<String, AST.method> classMethods = new HashMap<String, AST.method>();
        // Size of Class object
        int class_size = 0;
        // for each feature of class
        for(AST.feature ft : cl.features) {
            if(ft.getClass() == AST.attr.class) {
                // if it is a attribute
                AST.attr attrtemp = (AST.attr) ft;

                if(classAttributes.containsKey(attrtemp.name)) {
                    // if the attribute is already present
                    error.reportError(cl.filename, attrtemp.lineNo, "Attribute " + attrtemp.name + " is multiply defined in class.");
                    
                } else {
                    // else adding the attribute to the attrlist
                    classAttributes.put(attrtemp.name, attrtemp);
                    if (attrtemp.typeid == "Int" ) class_size += 4;
                    else if (attrtemp.typeid == "Bool") class_size += 1;
				    else class_size += 8;
                }
            } else if(ft.getClass() == AST.method.class) {
                AST.method mthdTemp = (AST.method) ft;
                if(classMethods.containsKey(mthdTemp.name)) {
                    // if the method is already present
                    error.reportError(cl.filename, mthdTemp.lineNo, "Method " + mthdTemp.name + " is multiply defined.");
                } else {
                    // else adding that method to the methodlist
                    classMethods.put(mthdTemp.name, mthdTemp);
                    // adding method name for LLVM IR
                    clsStructure.methodToMethodName.put(mthdTemp.name, "@_ZN"+cl.name.length()+cl.name+mthdTemp.name.length()+mthdTemp.name);
                }
            }
        }

         // Check errors for attributes
         for(Entry<String, AST.attr> entryS : classAttributes.entrySet()) {
            if(clsStructure.attrlist.containsKey(entryS.getKey())) {
                // if parent class attributes contains the attribute of the present class
                error.reportError(cl.filename, entryS.getValue().lineNo , "Attribute " + entryS.getValue().name + " is an attribute of an inherited class.");
            } else {
                // else adding the attribute to the attrlist of the class Structure
                clsStructure.attrlist.put(entryS.getKey(), entryS.getValue());
            }
        }
        // Initialising errCheck for method errors
        boolean errCheck = false;
        // Check errors for methods
        for(Entry<String, AST.method> entryS : classMethods.entrySet()) {
            
            errCheck = false;
            if(clsStructure.methodlist.containsKey(entryS.getKey())) {
                // if parent class method contains the method of present class
                // get the parent method from the methodlist
                AST.method parentMethod = clsStructure.methodlist.get(entryS.getKey());
                // get the current method
                AST.method presentMethod = entryS.getValue();

                if(presentMethod.formals.size() != parentMethod.formals.size()) {
                    // if the Parent method parameters size is not equal to the current method parameters size
                    error.reportError(cl.filename, presentMethod.lineNo, "Incompatible number of formal parameters in redefined method " + presentMethod.name + ".");
                    errCheck = true;
                } else {
                    if(presentMethod.typeid.equals(parentMethod.typeid) == false) {
                        //if the return type of inherited function is different
                        error.reportError(cl.filename, presentMethod.lineNo, "In redefined method " + presentMethod.name + ", return type " + presentMethod.typeid + " is different from original return type " + parentMethod.typeid + ".");
                        errCheck = true;
                    }

                    // for the typeid of parameters of the method
                    for(int i=0; i<presentMethod.formals.size(); i++) {
                        if(presentMethod.formals.get(i).typeid.equals(parentMethod.formals.get(i).typeid) == false) {
                            // Means the parameter typeid does not match with the corresponding parameter of parents type id
                            error.reportError(cl.filename, presentMethod.lineNo, "In redefined method " + presentMethod.name + ", parameter type " + presentMethod.formals.get(i).typeid + " is different from original type " + parentMethod.formals.get(i).typeid + ".");
                            errCheck = true;
                        }
                    }
                }
            }
            
            if(errCheck == false) {
                // if the error is not found while checking for methods 
                clsStructure.methodlist.put(entryS.getKey(), entryS.getValue());
            }
        }

        // Adding the class height , which is the height of one greater than the parent
        height.put(cl.name, height.get(cl.parent) + 1);
        clsStructure.size = class_size;
        // Insert the class into the classes_list
        classes_list.put(cl.name, clsStructure);

    }
    public boolean isConforming(String type1, String type2) {
        if(type1.equals(type2)) {
            // If both class names are same
            return true;
        } 
         else {
            // check for the parent class of type1
            type1 = classes_list.get(type1).parent;
            if(type1 == null) {
                // if parent does'nt exist
                return false;
            } else {
                return isConforming(type1, type2);
            }
        }
    }

    public String LCA(String type1, String type2) {
        // if both types are equal
        if(type1.equals(type2)) {
            return type1;
        } else if(height.get(type1) < height.get(type2)) {
            // type1 class is at a lower height than type2 class
            return LCA(type2, type1);
        } else {
            // type2 class is at a lower height than type1 class
            return LCA(classes_list.get(type1).parent, type2);
        }
    }


    
}