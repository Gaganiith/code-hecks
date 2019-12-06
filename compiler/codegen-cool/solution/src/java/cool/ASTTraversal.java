package cool;

import java.util.*;
import cool.AST.*;

public class ASTTraversal implements VisitorPattern {

	// filename for error reporting 
	public static String filename;

	// class data object for saving class information
	ClassData clsData;

	// Scope table object stores attributes of the current scope
	public ScopeTable<AST.attr> scopeTbl;

	// Inheritance graph object
	InheritanceGraph ig;

	// error object for reportError method
	Semantic error;

    ASTTraversal(){
		
		clsData = new ClassData();

		scopeTbl = new ScopeTable<AST.attr>();

		ig = new InheritanceGraph();

		error = new Semantic();
		
	}
	@Override
	public void visit(AST.program prog) {

        // get the filename from the classes
		filename = prog.classes.get(0).filename;
		
		
		// add classes to the inheritance graph
		ig.addClassesforInheritance(prog);
		
		// add adjacent parent to inheritance graph
		ig.checkParentandAddAdjacent(prog);
		
        // Check for cycles in the inheritance graph 
        if(ig.checkCycles(filename)) {
			// if found errors in inheritance graph
			 System.out.println("Compilation halted due to static semantic errors.");
             System.exit(1);	
		}

		// add classes to class Data object
		ig.addClasses(clsData);

		// To check if the main class and main method exists
		ClassStructure mainC = clsData.classes_list.get("Main");
		if(mainC == null) {
			error.reportError(filename, 1, "Class Main is not defined.");
		} else if(!mainC.methodlist.containsKey("main")) {
			// if main function is absent
			error.reportError(filename, 1, "No 'main' method in class Main.");
		}

		// if errors occured during adding classes halt the error checking
		if(clsData.error.getErrorFlag()){
			System.out.println("Compilation halted due to static semantic errors.");
            System.exit(1);
		}

		// second pass
		for(AST.class_ cl : prog.classes) {
            
            // enterScope for the current class
            scopeTbl.enterScope();
            
            // 'self' is inserted in the scope table when it is present as an attribute in a class 
            scopeTbl.insert("self", new AST.attr("self", cl.name, new AST.no_expr(cl.lineNo), cl.lineNo));
            
            // insertAll method inserts all the attributes of its ancestors and its attributes
            scopeTbl.insertAll(clsData.classes_list.get(cl.name).attrlist);

            // visiting the current class using accept method
			cl.accept(this);

            // exitScope for the current class
            scopeTbl.exitScope();
		}

		// if errors occured while traversing the AST
		if(error.getErrorFlag()){
			System.out.println("Compilation halted due to static semantic errors.");
            System.exit(1);
		}
	}

	@Override
	public void visit(AST.class_ cl) {
		
		// visiting all features of the class

        for(AST.feature ft: cl.features) {
			// Comparing feature object and AST object
			if(ft.getClass() == AST.method.class) {
				// visit the method 
				((AST.method)ft).accept(this);
            } else if(ft.getClass() == AST.attr.class) {
				// visit the attribute
				((AST.attr)ft).accept(this);
            }
        }
	}
	
	@Override
	public void visit(AST.attr att){

		// if self is redefined
		if("self".equals(att.name)) {
            error.reportError(filename, att.lineNo, "'self' cannot be the name of an attribute.");
		}
		ClassStructure clS = clsData.classes_list.get(att.typeid);
		if(clS==null) {
            // using undefined type
            error.reportError(filename, att.lineNo,"Class "+ att.typeid + " of attribute "+ att.name+" is undefined.");
			
        } else {
            
            if(!(att.value instanceof AST.no_expr)) { 
				// if attribute is valid -> visit the value of attr
				att.value.accept(this);
                // checking conformance of type of variable and assignment
                if(!clsData.isConforming(att.typeid, att.value.type)) {
                    error.reportError(filename, att.lineNo,"Expression doesn't conform to the declared type of attribute "+att.name+":"+att.typeid+"");
                }
            }
        }
	}

	@Override
	public void visit(AST.method method) {
		
		// Entering the scope of a current method
        scopeTbl.enterScope();

        // for each formal in the formal list of the method
        for(AST.formal form : method.formals) {
            // if formal is present in scopetable 
            AST.feature feat = (AST.feature)scopeTbl.lookUpLocal(form.name); 
			// if this formal is already in the scope
			if(feat != null){
				if(feat.getClass() == AST.attr.class){
                // formal parameter is multiply defined
                AST.attr attrfeat = (AST.attr) feat;
                error.reportError(filename, attrfeat.lineNo, "Formal parameter "+ attrfeat.name +" is multiply defined.");
				}
			}
            // insert the formal in scope table 
            scopeTbl.insert(form.name, new AST.attr(form.name, form.typeid, new AST.no_expr(form.lineNo), form.lineNo));

        }

        // visit method body
        (method.body).accept(this);

        if(!clsData.isConforming(method.body.type, method.typeid)) {
            // if the return type and method body type are different
            error.reportError(filename, method.body.lineNo, "Inferred return type "+ method.body.type + " of method "+ method.name +" does not conform to declared return type " + method.typeid + ".");
        }

		// exit the scope of the current method
        scopeTbl.exitScope();
	}

    @Override
	public void visit(AST.no_expr expr) {
		// if expression has no type
        expr.type = "No_type";
	}

	
	@Override
	public void visit(AST.int_const int_const) {
		// if expression has int type
        int_const.type = "Int";
	}

	@Override
	public void visit(AST.string_const string_const) {
		// if expression has string type
		string_const.type = "String";
	}

	@Override
	public void visit(AST.bool_const bool_const) {
		// if expression has bool type
		bool_const.type = "Bool"; 
	}
	
	@Override
	public void visit(AST.expression expr) {
	 
		// visit the expression
		expr.accept(this);
		
	}

	@Override
	public void visit(AST.assign asgn) {

		// visit the assignment expression
		asgn.e1.accept(this); 

		// if the assignment is self then report error
        if("self".equals(asgn.name)) {
			error.reportError(filename, asgn.lineNo, "Cannot assign to 'self'");
        } else {
			// lookup for the assignment expression name globally
            AST.attr type = scopeTbl.lookUpGlobal(asgn.name);

            if(type==null) {
                // if the variable is not found in the scope
				error.reportError(filename, asgn.lineNo,"Assignment to undeclared variable "+asgn.name+".");
            } else if(!clsData.isConforming(type.typeid, asgn.e1.type)) {
                // type mismatch 
				error.reportError(filename, asgn.lineNo,"Type "+ asgn.e1.type+ " of assigned expression does not conform to declared type "+type.typeid+" of identifier "+asgn.name+".");
            }
		}
		// assignment type is its assignment expression type
        asgn.type = asgn.e1.type;
	}

	@Override
	public void visit(AST.dispatch disp) {
        
        // visit the caller expression 
		disp.caller.accept(this);
		
		// if the caller is called from no method classes i.e, int and bool
		if(disp.caller.type.equals("Int") || disp.caller.type.equals("Bool")) {
			error.reportError(filename, disp.lineNo, "Undefined method "+disp.name);
            return;
        }
        for(AST.expression exp : disp.actuals) {
            // for each actuals expression -> visit the expression
            exp.accept(this);
		}

		ClassStructure clsStruct = clsData.classes_list.get(disp.caller.type);
		if(clsStruct==null){
			// if caller class is undefined
			error.reportError(filename, disp.lineNo, "Undefined class " + disp.caller.type + " of dispatch caller type.");
			disp.type = "Object";
		}
		else 
		{    
			if(clsStruct.methodlist.containsKey(disp.name)) {
				// if the dispatch method is present 
			
				AST.method mthd = clsStruct.methodlist.get(disp.name);
				
				// Compare the method formal list and number of parameters to the dispatch method
				if(disp.actuals.size() != mthd.formals.size()) {
					// Different number of parameters in the called static dispatch
					error.reportError(filename, disp.lineNo, "Method "+ mthd.name +" called with wrong number of arguments.");
					
				}

				disp.type = mthd.typeid;

			}else {
				// if the method is undefined
				error.reportError(filename, disp.lineNo, "Dispatch to undefined method " + disp.name + ".");
				disp.type = "Object";
		    }

		 }
	}

	@Override
	public void visit(AST.static_dispatch staticdis) {

        // This is similar to dispatch method,in addition we check conformance with caller expression
        // Calling the visit node on caller expression
        staticdis.caller.accept(this);

        for(AST.expression exp : staticdis.actuals) {
            // actuals is the list of expressions
            exp.accept(this);
		}
		// if the caller is called from no method classes i.e, int and bool
		if(staticdis.caller.type.equals("Int") || staticdis.caller.type.equals("Bool")) {
			error.reportError(filename, staticdis.lineNo, "Undefined method "+staticdis.name);
            return;
        }
		ClassStructure clsStruct = clsData.classes_list.get(staticdis.typeid);
		if(clsStruct==null){
			error.reportError(filename, staticdis.lineNo, "Undefined class " + staticdis.typeid + " of Static dispatch type.");
		   // recover from error and assign type Object
		   staticdis.typeid = "Object";
		   staticdis.type = "Object";
		} 
		  else if(!clsData.isConforming(clsStruct.name, staticdis.caller.type)) {
            // if expr type does not conform to type
			error.reportError(filename, staticdis.lineNo, "The declared static dispatch type " + clsStruct.name + " is different from the expression type " + staticdis.caller.type + ".");
			staticdis.type = "Object";

        } else {    
			if(clsStruct.methodlist.containsKey(staticdis.name)) {
				// if the static dispatch method is present in the method list of class structure
				
				AST.method mthd = clsStruct.methodlist.get(staticdis.name);
				
				// Now we will compare the method formal list and number of parameters to the static dispatch method
				if(staticdis.actuals.size() != mthd.formals.size()) {
					// Different number of parameters in the called static dispatch
					error.reportError(filename, staticdis.lineNo, "Method "+ mthd.name +" called with wrong number of arguments.");
					

				}
				staticdis.type = mthd.typeid;
			}else {
				// if the method is undefined
				error.reportError(filename, staticdis.lineNo, "Dispatch to undefined method " + staticdis.name + ".");
				staticdis.type = "Object";
			}

		 }

    }

	@Override
	public void visit(AST.cond cond) {

		// visit the predicate expression of the condition
		cond.predicate.accept(this);
		// visit the if-body of the condition
		cond.ifbody.accept(this);
		// visit the else-body of the condition
		cond.elsebody.accept(this);

		String boolTy = "Bool";
		// if the expression type is not bool
        if(!boolTy.equals(cond.predicate.type)) {
            error.reportError(filename, cond.lineNo, "Predicate return type of condition must be of Bool type");
        }
        cond.type = clsData.LCA(cond.ifbody.type, cond.elsebody.type);
	}

	@Override
	public void visit(AST.loop lp) {

		// visit the predicate expression of the loop
		lp.predicate.accept(this);
		// visit the body of the loop
		lp.body.accept(this);

		String boolTy = "Bool";
		// if the expression type is not bool
        if(!boolTy.equals(lp.predicate.type)) {
            error.reportError(filename, lp.lineNo, "Loop condition does not have type Bool.");
        }
        lp.type = "Object";
	}

	@Override
	public void visit(AST.block blck) {

		for(AST.expression exp: blck.l1) {
			// for each block expression l1 -> visit the expression
            exp.accept(this);
        }
        // get the last expression 
		int lastExpression = blck.l1.size()-1;

		// type of block is type of last expression
        blck.type = blck.l1.get(lastExpression).type;
	}
	
	@Override
	public void visit(AST.let let){

		// if the let attribute name is self
		if("self".equals(let.name)) {
            error.reportError(filename, let.lineNo, "'self' cannot be bound in a let expression");
        }
		if(let.value.getClass() != AST.no_expr.class) {
            // if the let's value member is an expression
            let.value.accept(this);
            if(!clsData.isConforming(let.value.type, let.typeid)) {
				// if let declared type does not match with let value type
                error.reportError(filename, let.lineNo, "Inferred type "+ let.value.type +" of initialization of "+ let.name +" does not conform to identifier's declared type " + let.typeid +".");
            }
		}
        // Entering new scope for the current let block
		scopeTbl.enterScope();
		
        // for the new scope insert the attributes of the let expression
        scopeTbl.insert(let.name, new AST.attr(let.name, let.typeid, let.value, let.lineNo));
        
        // visit the let body
        let.body.accept(this);

		// assign the type of let expression
		let.type = let.body.type;
		
        // Exiting the scope for the let block
        scopeTbl.exitScope();
	}

	@Override
	public void visit(AST.typcase typcs){

		// type of typecase is the join of type of all branches
		typcs.predicate.accept(this);

		// there is atleast 1 branch
		typcs.branches.get(0).accept(this);
		typcs.type = typcs.branches.get(0).value.type;

		// get the branch size
		int branchsize = typcs.branches.size();

		// Hashmap from Branch name to Boolean
		HashMap <String, Boolean> bMap = new HashMap<String, Boolean>();
		
		// accepting and joining types of other branches
		for(int i=1; i<branchsize; i++) {
			// visit the next branches if exist
			typcs.branches.get(i).accept(this);
			// check if branch is redefined
			if(!bMap.containsKey(typcs.branches.get(i).type)) {
				bMap.put(typcs.branches.get(i).type, true);
            } else {
                error.reportError(filename, typcs.branches.get(i).lineNo, "Duplicate branch "+ typcs.branches.get(i).type + " in case statement.");
            }
			// assign branch type
			typcs.type = clsData.LCA(typcs.type, typcs.branches.get(i).value.type);
		}
	}

	@Override
	public void visit(AST.branch br) {

		// branches of the typcase expression
		// enter branch scope
		scopeTbl.enterScope();

		// get the class structure for branch type
		ClassStructure clS = clsData.classes_list.get(br.type);

		if(clS == null) {
			error.reportError(filename, br.lineNo, " Class "+ br.type +" of case branch is undefined.");
			br.type = "Object";
			// To recover from the error, we add this unidentified class
			scopeTbl.insert(br.name, new AST.attr(br.name, "Object", br.value, br.lineNo));
		} else {
			// if the branch is found
			scopeTbl.insert(br.name, new AST.attr(br.name, br.type, br.value, br.lineNo));
		}
		

		// branch value visitor
		br.value.accept(this);

		// exit the scope of the branch
		scopeTbl.exitScope();
    }

	@Override
	public void visit(AST.new_ nw_){

		 // retrieving the class data for class associated with new
		 ClassStructure clS = clsData.classes_list.get(nw_.typeid);
		if(clS==null) {
			// if the new class is undefined
			error.reportError(filename, nw_.lineNo, "'new' used with undefined class "+nw_.typeid+".");
			// to recover from error
            nw_.type = "Object";
            
        } else {
			// if the class data exists for new_ type 
			nw_.type = nw_.typeid;
        }
	}

	@Override
	public void visit(AST.isvoid isvoid){
		// visit the void expression
		isvoid.e1.accept(this);
		// set the type to bool
        isvoid.type = "Bool";
	}

	@Override
	public void visit(AST.plus pls){
		// visit the left side expression of plus operator
		pls.e1.accept(this);
		// visit the right side expression of plus operator
		pls.e2.accept(this);
		String IntTy = "Int";
		// check if expressions are integer
        if(!IntTy.equals(pls.e1.type)||!IntTy.equals(pls.e2.type)) {
			error.reportError(filename, pls.lineNo, "non-Int arguments: "+ pls.e1.type +" + " + pls.e2.type);
			pls.type = "Object";
		}else{
			pls.type = IntTy;
		}
       
	}

	@Override
	public void visit(AST.sub sub) {
		// visit the left side expression of sub operator
		sub.e1.accept(this);
		// visit the right side expression of sub operator
		sub.e2.accept(this);
		String IntTy = "Int";
		// check if expressions are integer
        if(!IntTy.equals(sub.e1.type) || !IntTy.equals(sub.e2.type)) {
            error.reportError(filename, sub.lineNo, "non-Int arguments: "+ sub.e1.type +" - " + sub.e2.type);
			sub.type = "Object";
		}else{
			sub.type = IntTy;
		}
	}

	@Override
	public void visit(AST.mul mul) {
		// visit the left side expression of multiply operator
		mul.e1.accept(this);
		// visit the right side expression of multiply operator
		mul.e2.accept(this);
		String IntTy = "Int";
		// check if expressions are integer
        if(!IntTy.equals(mul.e1.type) || !IntTy.equals(mul.e2.type)) {
            error.reportError(filename, mul.lineNo, "non-Int arguments: "+ mul.e1.type +" * " + mul.e2.type);
			mul.type = "Object";
		}else{
			mul.type = IntTy;
		}
	}

	@Override
	public void visit(AST.divide divide) {
		// visit the left side expression of divide operator
		divide.e1.accept(this);
		// visit the right side expression of divide operator
		divide.e2.accept(this);
		String IntTy = "Int";
		// check if expressions are integer
        if(!IntTy.equals(divide.e1.type) || !IntTy.equals(divide.e2.type)) {
            error.reportError(filename, divide.lineNo, "non-Int arguments: "+ divide.e1.type +" / " + divide.e2.type);
       		divide.type = "Object";
		}else{
			divide.type = IntTy;
		}
        
	}

	@Override
	public void visit(AST.lt lt) {
		// visit the left side expression of less than operator
		lt.e1.accept(this);
		// visit the right side expression of less than operator
		lt.e2.accept(this);
		String IntTy = "Int";
		// check if expressions are integer
        if(!lt.e1.type.equals(IntTy) || !lt.e2.type.equals(IntTy)) {
			error.reportError(filename, lt.lineNo, "non-Int arguments: "+ lt.e1.type +" < " + lt.e2.type);
			lt.type = "Object";
        }else{
			lt.type = "Bool";
		}
        
	}

	@Override
	public void visit(AST.leq leq) {
		// visit the left side expression of less than equal to operator
		leq.e1.accept(this);
		// visit the right side expression of less than equal to operator
		leq.e2.accept(this);
		String IntTy = "Int";
		// check if expressions are integer
        if(!leq.e1.type.equals(IntTy) || !leq.e2.type.equals(IntTy)) {
            error.reportError(filename, leq.lineNo, "non-Int arguments: "+leq.e1.type +" <= " + leq.e2.type);
			leq.type = "Object";
        }else{
			leq.type = "Bool";
		}
	}

	@Override
	public void visit(AST.eq eq) {

		// visit the left side expression of equal operator
		eq.e1.accept(this);
		// visit the right side expression of equal operator
		eq.e2.accept(this);
		
        
		if((eq.e1.type.equals("String") || eq.e1.type.equals("Int") || eq.e1.type.equals("Bool")) && (eq.e2.type.equals("String") || eq.e2.type.equals("Int") || eq.e2.type.equals("Bool"))) {
			// if both sides of the assignment are of primitive data type
			if(!eq.e1.type.equals(eq.e2.type)){
				error.reportError(filename, eq.lineNo, "Illegal comparison with a basic type.");
			}
		} else if((eq.e1.type.equals("String") || eq.e1.type.equals("Int") || eq.e1.type.equals("Bool")) || (eq.e2.type.equals("String") || eq.e2.type.equals("Int") || eq.e2.type.equals("Bool"))) {
			// if atleast one side of the assignment is of primitive type
			if(!eq.e1.type.equals(eq.e2.type)){
				error.reportError(filename, eq.lineNo, "Illegal comparison with a basic type.");
			}
		}
		eq.type = "Bool";
        
	}

	@Override
	public void visit(AST.comp comp) {

		// visit the expression 
		comp.e1.accept(this);
		String BoolTy = "Bool";
		// if the expression to be complemented is not bool
        if(!BoolTy.equals(comp.e1.type)) {
			error.reportError(filename, comp.lineNo, "Argument of 'not' has type "+comp.e1.type+" instead of Bool.");
			comp.type = "Object";
        }else{
			comp.type = BoolTy;
		}
        
	}

	@Override
	public void visit(AST.neg neg) {
		// visit the expression
		neg.e1.accept(this);
		String IntTy = "Int";
		// if the expression to be negated is not int
        if(!IntTy.equals(neg.e1.type)) {
			error.reportError(filename, neg.lineNo, "Argument of '~' has type "+neg.e1.type+" instead of Int.");
			neg.type = neg.e1.type;
		}else{
			neg.type = IntTy;
		}
        
	}

	@Override
	public void visit(AST.object obj) {
		 
            // Searching for the attributes of obj in the scope table globally
			AST.attr typeName = scopeTbl.lookUpGlobal(obj.name);
			if(typeName == null) {
				// If typeName not exist in the scope table
				error.reportError(filename, obj.lineNo, "Undeclared identifier " + obj.name + "."); 
				obj.type = "Object";
			} else {
				obj.type = typeName.typeid;
			}
        
	}

}
