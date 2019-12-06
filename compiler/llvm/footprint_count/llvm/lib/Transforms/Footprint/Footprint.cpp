#include "llvm/Pass.h"
#include "llvm/Support/raw_ostream.h"
#include "llvm/Transforms/Scalar.h"
#include "llvm/IR/Function.h"
#include "llvm/IR/Module.h"
#include "llvm/Support/CommandLine.h"
#include "llvm/IR/DebugLoc.h"
#include "llvm/Support/Debug.h"
#include "llvm/IR/DebugInfoMetadata.h"
#include "llvm/IR/IntrinsicInst.h"
#include "llvm/IR/DebugInfo.h"
#include "llvm/Analysis/Passes.h"
#include "llvm/Analysis/ValueTracking.h"
#include "llvm/IR/CFG.h"
#include "llvm/Support/raw_ostream.h"
#include "llvm/Analysis/AliasAnalysis.h"
#include "llvm/IR/InstIterator.h"
#include "llvm/IR/Metadata.h"
#include "clang/Basic/Version.h"
#include "llvm/Analysis/AliasSetTracker.h"
#include <set>
#include <regex>
using namespace llvm;
using namespace std;
static cl::opt<std::string> InputVariable("var-name",
                                     cl::desc("Specify Variable for Analysis"),
                                     cl::value_desc("var"), cl::Required);
namespace {
  // Hello - The first implementation, without getAnalysisUsage.
  struct Footprint : public ModulePass {
    static char ID; // Pass identification, replacement for typeid
    Footprint() : ModulePass(ID) {}


	void getAnalysisUsage(AnalysisUsage &AU) const override{
		AU.addRequired<AAResultsWrapperPass>();
		AU.setPreservesAll();
	}

    virtual bool runOnModule(Module &M){
			// dbgs()<<*(((M.getNamedMetadata("llvm.ident"))->getOperand(0))->getOperand(0));
			NamedMDNode *aa =  cast<NamedMDNode>(M.getNamedMetadata("llvm.dbg.cu"));
			DICompileUnit *bb = 	cast<DICompileUnit>(aa->getOperand(0));
			
			string prod =  bb->getProducer();
			regex r("[0-9][0-9.]+");
			regex p("https[a-zA-Z.:/-]+");
			regex q("[0-9][0-9a-z]+");
			smatch m;

			dbgs()<<"Clang version : ";
			
			regex_search(prod, m, r); 
			for (auto x : m) 
        dbgs() << x << " "; 

			dbgs()<<"\nLLVM Source Repository : ";

			regex_search(prod, m, p); 
			for (auto x : m) 
        dbgs() << x << " "; 

			dbgs()<<"\nLLVM Commit Hash : ";

			
			regex_search(prod, m, q); 
			for (auto x : m) 
        dbgs() << x << " "; 

							dbgs()<<"\nClang Source Repository : ";

			regex_search(prod, m, p); 
			for (auto x : m) 
        dbgs() << x << " "; 

			dbgs()<<"\nClang Commit Hash : ";

			regex_search(prod, m, q); 
			for (auto x : m) 
        dbgs() << x << " "; 



			dbgs()<<"\nTarget : "<<sys::getDefaultTargetTriple();
			std::map<DIScope*, unsigned> gquiz1;
			std::map<DIScope*, unsigned> :: iterator itr;
			NamedMDNode *NameNode = M.getNamedMetadata("llvm.dbg.cu");
			// if(isa<DICompileUnit>(NameNode->getOperand(0))){
				DICompileUnit *compile = cast<DICompileUnit>(NameNode->getOperand(0));
				DIGlobalVariableExpressionArray globalArray = compile->getGlobalVariables();
			

			
			



// Finding Maximum Line number of each scope and generating a Map out of it.	

			for (Function &F: M) { 

				for (BasicBlock &BB : F) {
					for (Instruction &II : BB) {
						Instruction *I = &II;
						if (DILocation *Loc = I->getDebugLoc()){
							
						if(gquiz1.empty()){
							gquiz1.insert(std::pair <DIScope*, unsigned> (Loc->getScope(), Loc->getLine())); 
						}else{
								for (itr = gquiz1.begin(); itr != gquiz1.end(); ++itr) 
									{ 
										if(itr->first == Loc->getScope()){
											if(itr->second < Loc->getLine()){
												itr->second = Loc->getLine();
											}
										}else
										{
											gquiz1.insert(std::pair <DIScope*, unsigned> (Loc->getScope(), Loc->getLine())); 
										}
										
									}							
							}
 
						}
					}
				}
			}


// Handling the for loop child scope and getting its max line number.
			for (Function &F: M) { 
				for (BasicBlock &BB : F) {
					for (Instruction &II : BB) {
						Instruction *I = &II;
							if (DILocation *Loc = I->getDebugLoc()){

						
								if(isa<DILexicalBlock>(Loc->getScope())){
									if(isa<DILexicalBlock>(Loc->getScope()->getScope())){
										int temp_line_holder;

										for (auto itr = gquiz1.find(Loc->getScope()); itr != gquiz1.end();){
											temp_line_holder = itr->second;
											itr = gquiz1.end();
											}
										for (auto itr = gquiz1.find(cast<DIScope>(Loc->getScope()->getScope())); itr != gquiz1.end();){
											if(itr->second < temp_line_holder){
												itr->second = temp_line_holder;
												}
												itr = gquiz1.end();
											}														
										}

								}			
							}	
					}
				}
			}

// THis prints scope and footprint values for GLobal Variables.
			for(auto globe_iter =  M.getGlobalList().begin() ; globe_iter !=  M.getGlobalList().end(); globe_iter++){
				// dbgs()<<iter->getName();

				GlobalVariable *globe_iter1 = &*globe_iter;

				if(globe_iter1->getName() == InputVariable){
					int maxiLine=0;
					for (itr = gquiz1.begin(); itr != gquiz1.end(); ++itr) 
					{ 
						if(itr->second>maxiLine)
							maxiLine = itr->second;
					}
					

				dbgs()<<"\nVariable Name : "<< globe_iter1->getName();
				dbgs()<<"\nVariable Scope : ";
					
					for(auto itr_global_array = globalArray.begin();itr_global_array!=globalArray.end();++itr_global_array){
						DIGlobalVariableExpression *GLvarExp = *itr_global_array;
						DIGlobalVariable *GLvar = GLvarExp->getVariable();
						if(GLvar->getName()==InputVariable){
							dbgs()<< GLvar->getLine()<<":";
							dbgs()<< maxiLine;
						}
					}

					dbgs()<<"\nFootprint : ";
					for(User *U : globe_iter1->users()){
							// dbgs()<<*U<<"\n";	
							if(Instruction *UI = dyn_cast<Instruction>(U)){
								if (DILocation *Loc = UI->getDebugLoc()){

											dbgs()<<Loc->getLine()<<", ";

								}
							}
						}
						dbgs()<<"\nNumber of Reads : undef";
						dbgs()<<"\nNumber of Writes : undef";
					}
				
			}			


	    for (Function &F: M) { 
				if (F.isDeclaration()) continue;
				AliasAnalysis &AA = getAnalysis<AAResultsWrapperPass>(F).getAAResults();
					AliasSetTracker tracker(AA);
						for (BasicBlock &BB : F) {
							for (Instruction &II : BB) {
								tracker.add(&II);
							}
						}
						//  tracker.print(errs());

				for (BasicBlock &BB : F) {
					for (Instruction &II : BB) {
						Instruction *I = &II;

							if (CallInst *CI = dyn_cast<CallInst>(I)) {
								std::vector<int> vec ;

								DIVariable *Variable_name_given_by_user = cast<DIVariable>(cast<MetadataAsValue>(CI->getOperand(1))->getMetadata());
								if(Variable_name_given_by_user->getName()==InputVariable){

										

										dbgs()<<"\nVariable Name : "<<InputVariable;
										dbgs()<<"\nVariable Scope : "<<Variable_name_given_by_user->getLine()<<":" << (gquiz1.find(Variable_name_given_by_user->getScope())->second);
										dbgs()<<"\nFootprint : ";

										Metadata *Meta = cast<MetadataAsValue>(CI->getOperand(0))->getMetadata();
										if (isa<ValueAsMetadata>(Meta)) {
											Value *V = cast <ValueAsMetadata>(Meta)->getValue();

											AliasSet *var_set;
				              for(AliasSetTracker::iterator setTrack = tracker.begin(); setTrack!=tracker.end();setTrack++){
												AliasSet &s = cast<AliasSet>(*setTrack);
												for(AliasSet::iterator setTrack_inside = s.begin();setTrack_inside!=s.end();setTrack_inside++){
													if(Value *cv = setTrack_inside.getPointer()){
														if(V->getName()==cv->getName()){
																		var_set = &s;
														}
													}
												}
											}
											for(AliasSet::iterator setTrack_insider = var_set->begin();setTrack_insider!=var_set->end();setTrack_insider++){
												Value *iterating_over_set = setTrack_insider->getValue();
														for(User *U : iterating_over_set->users()){
															// dbgs()<<*U<<"\n";	
															if(Instruction *UI = dyn_cast<Instruction>(U)){
																if (DILocation *Loc = UI->getDebugLoc()){

																			vec.push_back(Loc->getLine());
				
																}
															}
														}												
												  
											}

											for(User *U : V->users()){
												// dbgs()<<*U<<"\n";	
												if(Instruction *UI = dyn_cast<Instruction>(U)){
													if (DILocation *Loc = UI->getDebugLoc()){

																vec.push_back(Loc->getLine());
	 
													}
												}
											}
								
	
										}
								
										std::vector<int>::iterator vec_uniq;
										set<int> set_v(vec.begin(),vec.end());
										vec.assign(set_v.begin(),set_v.end());
										for (auto i = vec.begin(); i != vec.end(); ++i)
													if(i==vec.begin()){
														dbgs()<< *i;
													}else{
														dbgs()<< ", "<< *i;
													}

									dbgs()<<"\nNumber of Reads : undef";
									dbgs()<<"\nNumber of Writes : undef";
													 

								}

							}



					}
				}
			}

	    return false;
    }


  
};
}

char Footprint::ID = 0;
static RegisterPass<Footprint> X("Footprint", "Class Assignment Analysis Pass");
