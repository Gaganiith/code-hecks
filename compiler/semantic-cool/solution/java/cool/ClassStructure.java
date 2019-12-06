package cool;
import java.util.*;
import cool.AST.*;

public class ClassStructure {

	// name of the class
	public String name;
	
	// parent of the class
	// initialized with null for Object class
	public String parent = null;

	// Attribute list of the class
	public HashMap <String, AST.attr> attrlist;

	// Method List of the class
    public HashMap <String, AST.method> methodlist;
    
    ClassStructure(String name_, String parent_name, HashMap<String, AST.attr> attrs, HashMap<String, AST.method> meth){
	   
		name = new String(name_);
		
		if(parent_name != null) 
			parent = new String(parent_name);
			
		attrlist = new HashMap <String, AST.attr>();
		// put all attributes passed to the constructor
		attrlist.putAll(attrs);
		
		methodlist = new HashMap <String, AST.method>();
		// put all methods passed to the constructor
		methodlist.putAll(meth);
    }
}