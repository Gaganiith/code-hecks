package cool;
import java.util.*;
import cool.AST.*;

public class ClassStructure {

	// name of the class
	public String name;
	
	// parent of the class
	// initialized with null for Object class
	public String parent = null;

	public int size = 0;
	// Attribute list of the class
	public HashMap <String, AST.attr> attrlist;

	// Method List of the class
	public HashMap <String, AST.method> methodlist;

	// New Method name mapping in LLVM IR
	public HashMap <String, String> methodToMethodName;
	
	ClassStructure(String name_, String parent_name, HashMap<String, AST.attr> attrs, HashMap<String, AST.method> meth,HashMap<String, String> methodName,int s){
		
	name = new String(name_);
	
	if(parent_name != null) 
		parent = new String(parent_name);
		
	attrlist = new HashMap <String, AST.attr>();
	// put all attributes passed to the constructor
	attrlist.putAll(attrs);
	
	methodlist = new HashMap <String, AST.method>();
	// put all methods passed to the constructor
	methodlist.putAll(meth);

	methodToMethodName = new HashMap <String, String>();
	methodToMethodName.putAll(methodName);
	size = s;
	}
}