

Note : It is assumed that there is no SELF_TYPE.

Our solution to the semantic analyzer of COOL consists of following major steps:

1.  Construct the Inheritance graph : In the first pass of AST traversal of classes , we check for inheritance errors and cycle detection.
i) It first adds IO and Object classes to inheritance graph then add other valid classes after checking errors. 

ii) It than adds all edges in the adjacency list of the graph.
iii) Lastly it check for multiple cycles using DFS and prints suitable error if cycle is detected.

2. Installing Classes : Basic classes name and  methods are added in the constructor of ASTTraversal class.  After Cycle is checked, we add classes into the classes_list hashmap of the classData object using addClasses method which inserts classes in breadth first order such that all the attributes and methods of the inherited classes are added before adding the child class methods and attributes.
Basic Classes installed are :
Object : methods-  i. abort :Object
                   ii. copy:Object
                   iii. type_name:String
IO : methods- i. out_string(x:String) :Object
              ii. out_int(X:Int): Object
              iii. in_string():String
             iv. in_int() :Int 
String : methods - i. length() :Int
		   ii. concat(s:String) :String
                   iii. substr(i:Int,l:Int) :String
Int : No explicit methods 
Bool : No explicit methods

Next we use two hash maps classmethods and classattributes for each class to be inserted after checking attributes and methods errors.It checks errors for attributes if it is already defined or it is an attribute of parent class.
Similarly for methods we check if method is already defined and if it is already declared in inherited class. We also check if number of formals are different or their parameter types are different and its return type matches the body expression type.
Next we also check if main class exists and it consists of main method or not. 

3. In the second pass, we fill the Scopetable in program visit method.We add self attribute and ancestors attribute to each class. For each class we traverse its features. For each feature i.e, Method and Attribute we visit them while iterating the features. 
i) For each attribute we check if attribute is undefined or not and if expression conforms with its value.
ii) For each method we enter its scope and iterate over its formal which is a feature. if the formal exists we check if formal parameter is defined multiple times. Next we visit the method.body expression and check if the block expression type is conforming to the return type. At last we exit the scope of the method.
iii)  Various Expressions are visited while traversing the features of the class :

Dispatch and static dispatch : 
First we visit the caller expression and checks its errors than we iterate over all the actuals expression of the dispatch call. Next we check if the dispatch expression type of caller exists in our classData object than we check if the number of actuals and formals are same and they conform to each other. At last we check if the method is defined or not.
for the static dispatch, we also check type conformance with caller expression.

Loop : 
We visit the predicate expression of the loop and check if it is of type Bool.Next we visit the body of the loop with its type default to Object.

Condition :
We visit the predicate of the condition and check if it conforms to Bool type. Than we visit the if.body and else.body expressions. Condition expression type is assigned the parent type of both expressions using the LCA (lowest common ancestor) method.

Assignment :
First we visit the assignment expression.We check if self is assigned in the assignment expression and we get the attribute from scopetable using loopupglobal method.If the attribute is undefined than report the error and check if the assignment type conforms with attribute type.

Block :
We iterate all the expressions in the block and visit them sequentially. Next we assign the type of block expression as the last expression of the block list l1.

Let :
We check if the let.name is self or not.Than we visit the let.value expression and check if it conforms with the attribute.
Next we enter  a new scope of let block.Next we insert the attributes in the let block than we visit the let body expression and assign the let expression with let.body expression. At last we exit the let block scope.

Typcase and branch :
For the case statement, we check branching errors. first we visit the typcase.predicate expression and we traverse over it branches.For each branch we enter its scope and check if branch type is defined.In the scope table we insert the branch attribute and exit the branch scope. While iterating on the size of branches in typcase, we check if branches are duplicate or not.

isVoid :
We just visit the expression and set its type to the Object.

New_ :
Here we check if the new is used with undefined class or not and assign its expression.

no_expr / int_const / string_const / bool_const :
We simply assign the type to its constant type.

plus / sub / mul / divide :
For all these expression we visit left and right hand side of the expressions and check is they are of "Int" type.

lt / leq / eq :
Similar to the arithmatic expressions we check if l-value and r-value expressions are of type "Int".

comp / neg :
We visit the expression and check if the comp expression is of type Bool and neg expression is of type Int.

Object :
For the object we lookup in the scopetable if it exist or not and assign its type.

Structure of Code:

1. AST.java : We have added accept method from Visitable interface to visitable classes to accept a visitor to traverse the AST.

2. ScopeTable.java : Added a method insertAll to add all attributes from a input hashmap in a scope.

3. ClassData.java : This class uses a hashmap classes_list to store all the basic classes and new classes introduced by the input program if no errors exist.It also has a hashmap called height to store the level of class in inheritance graph.
It consists of three major methods :
i) addClassDetails method : It inserts classes into the classes_list with error checking on classes attributes and methods.
ii) isConforming method : to check if the two input types are conforming. 
iii) LCA method : it return the common ancestor of two input names.

4. InheritanceGraph.java : This class adds classes to inheritance graph and check cycles in the inheritance graph. It checks if class is already defined or is a basic class also if class inherits from String,bool or int.It consists of following methods :
i) addClasses method : It adds classes (using addClassDetails method) to classes_list hashmap in bfs order.
ii) addClassesforInheritance method : It adds classes to the inheritance graph.It also inserts class index value for each class name to classNametoIndex Map.
iii) checkParentandAddAdjacent method : This methods adds class index for its parent index and checks if parent is defined.
iv) checkCycles method : It detects a cycle using dfs_cycle method and prints all classes involved in the cycle using printAdjClasses method.

5. Semantic.java : In its constructor we start the traversal of the program by creating a new object of ASTTraversal class.

6. ASTTraversal.java : This class defines all the visit methods of the interface VisitorPattern corresponding to the classes of AST.java.

7. VisitorPattern.java : This is a interface which declares the visit methods to be overridden.

8. Visitable.java : This is a simple interface for accept method to be implemented in AST.java.

9. ClassStructure.java : This class defines the basic data structures of a class like its name, parent_name, methodlist and atrributelist.


Test Cases : 

###################### Test cases Where there is Error. ###################### 

test1 ==> Cycle in Inheritance Graph.

test2 ==> Class cannot inherit from predefined classes like String, Int and Bool.

test3 ==> Re declaration of a same class.

test4 ==> Class A is trying to inherit a class which is undefined.

test5 ==> Declaration of same variable twice.

test6 ==> Declaration of same function twice in a class.

test7 ==> Variable is already declared in the inherited class.

test8 ==> Return type of a function redefined in class A inheriting from class B is different.

test9 ==> The types of the formals are not same in the function redefined in class A inheriting from class B.

test10 ==> Formals cannot be re-defined.

test11 ==> The Definition of the attribute is Int whereas the Initialisation is done with the character "c".

test12 ==> The number of formals in the function redefinition in a class inheriting from parent class are different.

test13 ==> Different number of parameters passed to a function call as defined in the function.

test14 ==> The While loop should have a return type as Boolean for condition value.

test15 ==> The return type of function test is Boolean which does not match with the integer value returned as 2.

test16 ==> No declaration corresponding to variable p and there is an initialisation with 1 is being done over it.

test17 ==> A Branch of case has undefined type B.

test18 ==> There is no class D therefore the error of class D with new keyword is undefined exists.

test19 ==> There is no main class in this program.

test20 ==> There is no main method in this main class.

test21 ==> Type mismatch between operator a and b in multiplication, for this test case. But our code works on all addition, subtraction and division as well.

test22 ==> Type mismatch between operator a and b in less than operator for this test case. But our code works on all logical operators including < , > , <= , >=, = .

test23 ==> The variable a is undefined and the argument in addition is int and non-Int object , which again a type mismatch error as well.

###################### Test cases Where there is no Error. ###################### 
test-NE-1 ==> Test case runs true on this where method defined in the inherited class are same with same number of formals with same return type.

test-NE-2 ==> Test Case considering if-else conditions.

test-NE-3 ==> Test Case considering let expressions and arithmetic operations with same datatypes, hence no error.

