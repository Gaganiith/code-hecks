package cool;
import java.util.*;
import java.util.Map.Entry;

import cool.AST.*;

public class InheritanceGraph{

    // class index for classNametoIndexMap
    int classIndex ;

    // if inheritance graph contains cycle
    boolean hasCycle;

    // inheritance graph of classes
    public ArrayList <ArrayList <Integer>> graph;

    // Hashmap for class name to index
    HashMap<String,Integer> classNameToIndexMap;

    // Hashmap from classname to AST.class_ for inserting classes into class data object
    public Map<String, AST.class_> classNameToClassMap;

    // obect for reportError method
    Semantic error;

    // for the index of class 
    public static int index;

    InheritanceGraph(){

        classIndex = 0; 

        hasCycle = false;

        error = new Semantic();

        graph = new ArrayList <ArrayList <Integer>>();

        classNameToIndexMap = new HashMap<String,Integer>();

        classNameToClassMap = new HashMap<String,AST.class_>();

    }

    // Method to get classname from class index
    public static <String,Integer> String getKeyByValue(Map<String,Integer> map, Integer value) {
        for (Entry<String,Integer> entry : map.entrySet()) {
            if (Objects.equals(value, entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }

    public void addClasses(ClassData clsData){
        // Create Queue to insert classes
        Queue<Integer> Q = new LinkedList<Integer>(); 
        // insert element using poll without violating size of queue
        // insert index 0 for Object class
		Q.offer(0);		
        
        // index to be inserted into class data object
		int k;
		while (!Q.isEmpty()) {
                // get front element of queue
			    k = Q.poll();
                if(k != 1 && k != 0) {
                    // get the classname from index
                    String clsname = getKeyByValue(classNameToIndexMap, k);
                    // add class into the class data
                    clsData.addClassDetails(classNameToClassMap.get(clsname));
                }	
                for(Integer i : graph.get(k)) {
                    // insert next class
                    Q.offer(i);
                }
			}
    }

    /* Adds classes in inheritance graph and classNametoIndex,classNameToClass Hashmaps  */
    public void addClassesforInheritance(AST.program prog) {

        // add Object and IO classes to the hashmap
        classNameToIndexMap.put("Object", 0);
        classNameToIndexMap.put("IO", 1);
        
        // add Object and IO classes to the graph
        graph.add(new ArrayList <Integer> (Arrays.asList(1)));
        graph.add(new ArrayList <Integer>());

        // two classes are added
        classIndex = classIndex + 2;

        // 1st pass
        for(AST.class_ cl : prog.classes) {

            //if the class is one of the basic classes
            if(cl.name.equals("Object") || cl.name.equals("String") || cl.name.equals("Int") || cl.name.equals("Bool") || cl.name.equals("IO")) {
                error.reportError(cl.filename, cl.lineNo, "Class redefinition of class '" + cl.name + "' not possible.");
                System.out.println("Compilation halted due to static semantic errors.");
				System.exit(1);
            }
            
            
            // if class inherits from a not inheritable class i.e, String,Int and Bool
			if(cl.parent.equals("String") || cl.parent.equals("Int") ||cl.parent.equals("Bool") ) {
                error.reportError(cl.filename, cl.lineNo, "Class " + cl.name + " cannot inherit class " + cl.parent + ".");
                System.out.println("Compilation halted due to static semantic errors.");
				System.exit(1);
            } 
            
            if(!classNameToIndexMap.containsKey(cl.name)) {
				// if cl is a valid new class and can be added to the graph
				classNameToIndexMap.put(cl.name, classIndex);
                classNameToClassMap.put(cl.name, cl);
                classIndex = classIndex + 1;

                graph.add(new ArrayList <Integer> ());

			} else if(classNameToIndexMap.containsKey(cl.name) == true) {
                // if the class is already defined 
                error.reportError(cl.filename, cl.lineNo, "Redefinition of basic class " + cl.name + ".");
                System.out.println("Compilation halted due to static semantic errors.");
                System.exit(1);
            }
            
        }
        
        // enter Int and Bool with index -1
        classNameToIndexMap.put("Int", -1);
        classNameToIndexMap.put("Bool", -1);
    }

    /* Add adjacent edges of the graph */ 
    public void checkParentandAddAdjacent(AST.program prog){
        for(AST.class_ cl : prog.classes) {
                // if the  parent class is not defined then report the error
                // else add it to the graphs          
                if(!classNameToIndexMap.containsKey(cl.parent)) {
                    error.reportError(cl.filename, cl.lineNo, "Class "+cl.name+" inherits from an undefined class "+ cl.parent + ".");
                    System.out.println("Compilation halted due to static semantic errors.");
                    System.exit(1);
                }

                // This adds an edge between parent and the child class
                graph.get(classNameToIndexMap.get(cl.parent)).add(classNameToIndexMap.get(cl.name));
            }
    }

    /*Check for cycles in the inheritance graph */
    public boolean checkCycles(String filename){

        boolean errorFlag = false;
        // size of the inheritance graph
        int gSize = graph.size();
        // boolean visited arraylist for dfs 
        ArrayList<Boolean> visited = new ArrayList<>();
        // boolean recursion stack arraylist for dfs
        ArrayList<Boolean> recStack = new ArrayList<>();
        // initialize and add elements in both arraylists with false 
        for(int i = 0; i < gSize; i++) {
            visited.add(false);
            recStack.add(false);
        }
        // check for each class in the graph for cycles
        for(int i = 0; i < gSize; i++){
            dfs_cycle(i, visited, recStack,graph);
			if (hasCycle){
                errorFlag = true;
                // print all classes involved in the cycle
                index = i;
                printAdjClasses(i,graph,filename);
               
            }
            hasCycle = false;
		}
        return errorFlag;
    }

    void printAdjClasses(int i,ArrayList<ArrayList <Integer> > graph,String filename){
        
        for(Integer j:graph.get(i)){
            String classname = "";
            for (Entry<String, Integer> entry : classNameToIndexMap.entrySet()) {
                if (entry.getValue().equals(j)) {
                    classname = entry.getKey();
                }
            }
             // other classes involved in the cycle
             error.reportError(filename, 1, "Class " + classname + ", or an ancestor of " + classname + ", is involved in an inheritance cycle.");
            if(index!=j){
                printAdjClasses(j, graph,filename);
            }
        }
    }

    /* Check for cycles using Depth first search*/
    public void dfs_cycle(int v, ArrayList<Boolean> visited, ArrayList<Boolean> recStack,ArrayList<ArrayList <Integer> > graph){
        
        // if class is not visited 
        if(visited.get(v) == false){
              // mark the class as visited and part of recursion stack
              visited.set(v,true);
              recStack.set(v, true);
              for(Integer i:graph.get(v)){
                if(!visited.get(i)) {
                    dfs_cycle(i, visited, recStack, graph);
                } else if (recStack.get(i)) {
                    hasCycle = true;
                    return;
                }
            }
        }
        recStack.set(v, false);
    }

}