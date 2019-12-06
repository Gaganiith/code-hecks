#include "llvm/Pass.h"
#include "llvm/IR/Function.h"
#include "llvm/Support/raw_ostream.h"
#include "llvm/Analysis/LoopPass.h"
#include "llvm/Analysis/LoopIterator.h"
#include "llvm/Analysis/LoopInfo.h"
#include "llvm/Transforms/Utils/LoopUtils.h"
#include "llvm/Transforms/Utils/BasicBlockUtils.h"
#include "llvm/Transforms/Scalar.h"
#include "llvm/Analysis/ScalarEvolution.h"
#include "llvm/Analysis/ScalarEvolutionExpressions.h"
#include "llvm/IR/Dominators.h"
#include "llvm/IR/TypeBuilder.h"
#include "llvm/IR/IRBuilder.h"
#include "llvm/Transforms/Utils/SimplifyIndVar.h"
#include "llvm/IR/LLVMContext.h"
#include "llvm/IR/Module.h"
#include "llvm/IR/DerivedTypes.h"
#include "llvm/IR/Verifier.h"
#include <set>
#include <regex>
using namespace llvm;
using namespace std;

namespace {

	struct LoopReversal : public FunctionPass{
			static char ID;

			int counts = 0;
			LoopReversal() : FunctionPass(ID) {}

			void getAnalysisUsage(AnalysisUsage &AU) const override {

					AU.addRequired<DominatorTreeWrapperPass>();
					AU.addPreserved<DominatorTreeWrapperPass>();
					AU.addRequired<LoopInfoWrapperPass>();
					AU.addPreserved<LoopInfoWrapperPass>();
					AU.addRequired<ScalarEvolutionWrapperPass>();
					AU.addPreserved<ScalarEvolutionWrapperPass>();
					AU.setPreservesAll();
			}
	virtual bool runOnFunction(Function&F){

		LoopInfo &LI = getAnalysis<LoopInfoWrapperPass>().getLoopInfo();

		//Iteration on loops
		for(LoopInfo::iterator i = LI.begin(), e = LI.end(); i != e; ++i){
			Loop *L = *i;
		
		// Fetching various Basic blocks in a loop.
		BasicBlock *parentLoopLatch = L->getLoopLatch();
		BasicBlock *parentLoopPred = L->getLoopPredecessor();
		BasicBlock *parentLoopHead = L->getHeader();
		static LLVMContext Context;

		//Running a Loop on all Instructions in basicblock named parentLoopHead.
		for (auto CI=parentLoopHead->begin();CI!=parentLoopHead->end();CI++) {
			if (CmpInst *I = dyn_cast<CmpInst>(CI)){
			//If the found instruction is Compare Instruction then we loop over all Instructions again in the same block.	
			for(auto IP = parentLoopHead->getInstList().begin(); IP!=parentLoopHead->getInstList().end() ;IP++){
				//We check whether its a PHINode or not.
					if(isa<PHINode>(IP)){
						if((I->getOperand(0)->getName()).compare(IP->getName())==0){
						//Till Here I Got the induction variable by comparing PHI node with the operand in Comparison Instruction.

							//Get the lower bound and upper bounds in the loop.
							Value *oldLowerBound = dyn_cast<Value>(IP->getOperand(0)); //IV
							Value *oldUpperBound = dyn_cast<Value>(I->getOperand(1)); //CMP
							Value *IValue1 = ConstantInt::get(Type::getInt32Ty(F.getContext()), 1);

							//Check Comparison Istruction is SLT (Less Than).
							if(I->getPredicate() == CmpInst::ICMP_SLT){

								//Set New Upper & Lower bounds.
								Value *newUpperBound = oldLowerBound; //CMP
								Instruction *NValue1 = BinaryOperator::Create((Instruction::BinaryOps)(Instruction::Sub),oldUpperBound,IValue1,"Myname");
								parentLoopPred->getInstList().insert(parentLoopPred->getFirstInsertionPt(), NValue1);
								Value *newLowerBound = dyn_cast<Value>(NValue1); //IV
								I->setOperand(1,newUpperBound); //CMP
								IP->setOperand(0,newLowerBound); //IV

								//Set the Predicate to SGE (Greater than Equal)
								I->setPredicate(CmpInst::ICMP_SGE); // Comparison Operator Handled

								// Handling loop latch
								for (auto LL=parentLoopLatch->begin();LL!=parentLoopLatch->end();LL++) {
									if(LL->getOpcode()==Instruction::Add){
										if((LL->getOperand(0)->getName()).compare(IP->getName())==0){
											NValue1 = BinaryOperator::Create((Instruction::BinaryOps)(Instruction::Sub),LL->getOperand(0),LL->getOperand(1),"dec");
											ReplaceInstWithInst(dyn_cast<Instruction>(LL),NValue1);
											break;
										}
									}else if(LL->getOpcode()==Instruction::Sub){
										if((LL->getOperand(0)->getName()).compare(IP->getName())==0){
											NValue1 = BinaryOperator::Create((Instruction::BinaryOps)(Instruction::Add),LL->getOperand(0),LL->getOperand(1),"inc");
											ReplaceInstWithInst(dyn_cast<Instruction>(LL),NValue1);
											break;
										}
									}else if(LL->getOpcode()==Instruction::Mul){
										if((LL->getOperand(0)->getName()).compare(IP->getName())==0){
											NValue1 = BinaryOperator::Create((Instruction::BinaryOps)(Instruction::SDiv),LL->getOperand(0),LL->getOperand(1),"div");
											ReplaceInstWithInst(dyn_cast<Instruction>(LL),NValue1);
											break;
										}
									}else if(LL->getOpcode()==Instruction::SDiv){
										if((LL->getOperand(0)->getName()).compare(IP->getName())==0){
											NValue1 = BinaryOperator::Create((Instruction::BinaryOps)(Instruction::Mul),LL->getOperand(0),LL->getOperand(1),"mul");
											ReplaceInstWithInst(dyn_cast<Instruction>(LL),NValue1);
											break;
										}
									}
										
								}

								//Handling Loop Body
								Instruction *temp,*temp1,*final_expr;
								
								for(auto* bb : L->getBlocks()){
									if(bb == parentLoopHead || bb == parentLoopPred || bb == parentLoopLatch)
										continue;
									auto last_access = bb->begin();
									for (auto LI = bb->begin();LI!=bb->end();LI++) {
										temp = BinaryOperator::Create((Instruction::BinaryOps)(Instruction::Sub),oldUpperBound,IValue1,"Myname",dyn_cast<Instruction>(LI));
										temp1 = BinaryOperator::Create((Instruction::BinaryOps)(Instruction::Add),oldLowerBound,temp,"Myname");
										temp1->insertAfter(temp);
										final_expr = BinaryOperator::Create((Instruction::BinaryOps)(Instruction::Sub),temp1,dyn_cast<Instruction>(IP),"Myname");
										final_expr->insertAfter(temp1);
										last_access = LI;
										break;
									}
									for(;last_access!=bb->end();last_access++){
										last_access->dump();
										for(unsigned int i=0;i<last_access->getNumOperands();i++){
											if(last_access->getOperand(i)->getName()==IP->getName()){
												last_access->setOperand(i,final_expr);
											}
										}
									}

								}
							
							}//Check Comparison Istruction is SGT (Greater Than).
							else if(I->getPredicate() == CmpInst::ICMP_SGT){
								Value *newUpperBound = oldLowerBound; //CMP
								Instruction *NValue1 = BinaryOperator::Create((Instruction::BinaryOps)(Instruction::Add),oldUpperBound,IValue1,"Myname");
								parentLoopPred->getInstList().insert(parentLoopPred->getFirstInsertionPt(), NValue1);
								Value *newLowerBound = dyn_cast<Value>(NValue1); //IV
								I->setOperand(1,newUpperBound); //CMP
								IP->setOperand(0,newLowerBound); //IV
								
								//Set the Predicate to SGE (Less than Equal)
								I->setPredicate(CmpInst::ICMP_SLE); // Comparison Operator Handled

								// Handling loop latch
								for (auto LL=parentLoopLatch->begin();LL!=parentLoopLatch->end();LL++) {
									if(LL->getOpcode()==Instruction::Add){
										if((LL->getOperand(0)->getName()).compare(IP->getName())==0){
											NValue1 = BinaryOperator::Create((Instruction::BinaryOps)(Instruction::Sub),LL->getOperand(0),LL->getOperand(1),"dec");
											ReplaceInstWithInst(dyn_cast<Instruction>(LL),NValue1);
											break;
										}
									}else if(LL->getOpcode()==Instruction::Sub){
										if((LL->getOperand(0)->getName()).compare(IP->getName())==0){
											NValue1 = BinaryOperator::Create((Instruction::BinaryOps)(Instruction::Add),LL->getOperand(0),LL->getOperand(1),"inc");
											ReplaceInstWithInst(dyn_cast<Instruction>(LL),NValue1);
											break;
										}
									}else if(LL->getOpcode()==Instruction::Mul){
										if((LL->getOperand(0)->getName()).compare(IP->getName())==0){
											NValue1 = BinaryOperator::Create((Instruction::BinaryOps)(Instruction::SDiv),LL->getOperand(0),LL->getOperand(1),"div");
											ReplaceInstWithInst(dyn_cast<Instruction>(LL),NValue1);
											break;
										}
									}else if(LL->getOpcode()==Instruction::SDiv){
										if((LL->getOperand(0)->getName()).compare(IP->getName())==0){
											NValue1 = BinaryOperator::Create((Instruction::BinaryOps)(Instruction::Mul),LL->getOperand(0),LL->getOperand(1),"mul");
											ReplaceInstWithInst(dyn_cast<Instruction>(LL),NValue1);
											break;
										}
									}
										
								}

								//Handling Loop Body
								Instruction *temp,*temp1,*final_expr;
								
								for(auto* bb : L->getBlocks()){
									if(bb == parentLoopHead || bb == parentLoopPred || bb == parentLoopLatch)
										continue;
									auto last_access = bb->begin();
									for (auto LI = bb->begin();LI!=bb->end();LI++) {
										temp = BinaryOperator::Create((Instruction::BinaryOps)(Instruction::Add),oldLowerBound,IValue1,"Myname",dyn_cast<Instruction>(LI));
										temp1 = BinaryOperator::Create((Instruction::BinaryOps)(Instruction::Add),oldUpperBound,temp,"Myname");
										temp1->insertAfter(temp);
										final_expr = BinaryOperator::Create((Instruction::BinaryOps)(Instruction::Sub),temp1,dyn_cast<Instruction>(IP),"Myname");
										final_expr->insertAfter(temp1);
										last_access = LI;
										break;
									}
									for(;last_access!=bb->end();last_access++){
										for(unsigned int i=0;i<last_access->getNumOperands();i++){
											if(last_access->getOperand(i)->getName()==IP->getName()){
												last_access->setOperand(i,final_expr);
											}
										}
									}

								}							

							}//Check Comparison Istruction is SGT (Less Than Equal).
							else if (I->getPredicate() == CmpInst::ICMP_SLE){
								Instruction *NValue1 = BinaryOperator::Create((Instruction::BinaryOps)(Instruction::Sub),oldLowerBound,IValue1,"Myname");
								parentLoopPred->getInstList().insert(parentLoopPred->getFirstInsertionPt(), NValue1);
								Value *newUpperBound = dyn_cast<Value>(NValue1); //IV
								Value *newLowerBound = oldUpperBound; //IV

								I->setOperand(1,newUpperBound); //CMP
								IP->setOperand(0,newLowerBound); //IV

								//Set the Predicate to SGE (Greater than)
								I->setPredicate(CmpInst::ICMP_SGT); // Comparison Operator Handled

								// Handling loop latch
								for (auto LL=parentLoopLatch->begin();LL!=parentLoopLatch->end();LL++) {
									if(LL->getOpcode()==Instruction::Add){
										if((LL->getOperand(0)->getName()).compare(IP->getName())==0){
											NValue1 = BinaryOperator::Create((Instruction::BinaryOps)(Instruction::Sub),LL->getOperand(0),LL->getOperand(1),"dec");
											ReplaceInstWithInst(dyn_cast<Instruction>(LL),NValue1);
											break;
										}
									}else if(LL->getOpcode()==Instruction::Sub){
										if((LL->getOperand(0)->getName()).compare(IP->getName())==0){
											NValue1 = BinaryOperator::Create((Instruction::BinaryOps)(Instruction::Add),LL->getOperand(0),LL->getOperand(1),"inc");
											ReplaceInstWithInst(dyn_cast<Instruction>(LL),NValue1);
											break;
										}
									}else if(LL->getOpcode()==Instruction::Mul){
										if((LL->getOperand(0)->getName()).compare(IP->getName())==0){
											NValue1 = BinaryOperator::Create((Instruction::BinaryOps)(Instruction::SDiv),LL->getOperand(0),LL->getOperand(1),"div");
											ReplaceInstWithInst(dyn_cast<Instruction>(LL),NValue1);
											break;
										}
									}else if(LL->getOpcode()==Instruction::SDiv){
										if((LL->getOperand(0)->getName()).compare(IP->getName())==0){
											NValue1 = BinaryOperator::Create((Instruction::BinaryOps)(Instruction::Mul),LL->getOperand(0),LL->getOperand(1),"mul");
											ReplaceInstWithInst(dyn_cast<Instruction>(LL),NValue1);
											break;
										}
									}
										
								}

								// Handling Loop Body
								Instruction *final_expr;
								
								for(auto* bb : L->getBlocks()){
									if(bb == parentLoopHead || bb == parentLoopPred || bb == parentLoopLatch)
										continue;
									auto last_access = bb->begin();
									for (auto LI = bb->begin();LI!=bb->end();LI++) {
										final_expr = BinaryOperator::Create((Instruction::BinaryOps)(Instruction::Sub),oldUpperBound,dyn_cast<Instruction>(IP),"Myname",dyn_cast<Instruction>(LI));
										last_access = LI;
										break;
									}
									for(;last_access!=bb->end();last_access++){
										for(unsigned int i=0;i<last_access->getNumOperands();i++){
											if(last_access->getOperand(i)->getName()==IP->getName()){
												last_access->setOperand(i,final_expr);
											}
										}
									}

								}

							}//Check Comparison Istruction is SGT (Greater Than Equal).
							else if (I->getPredicate() == CmpInst::ICMP_SGE){
								Instruction *NValue1 = BinaryOperator::Create((Instruction::BinaryOps)(Instruction::Add),oldLowerBound,IValue1,"Myname");
								parentLoopPred->getInstList().insert(parentLoopPred->getFirstInsertionPt(), NValue1);
								Value *newUpperBound = dyn_cast<Value>(NValue1); //IV
								Value *newLowerBound = oldUpperBound; //IV

								I->setOperand(1,newUpperBound); //CMP
								IP->setOperand(0,newLowerBound); //IV

								//Set the Predicate to SGE (Less than)
								I->setPredicate(CmpInst::ICMP_SLT); // Comparison Operator Handled

								// Handling loop latch
								for (auto LL=parentLoopLatch->begin();LL!=parentLoopLatch->end();LL++) {
									if(LL->getOpcode()==Instruction::Add){
										if((LL->getOperand(0)->getName()).compare(IP->getName())==0){
											NValue1 = BinaryOperator::Create((Instruction::BinaryOps)(Instruction::Sub),LL->getOperand(0),LL->getOperand(1),"dec");
											ReplaceInstWithInst(dyn_cast<Instruction>(LL),NValue1);
											break;
										}
									}else if(LL->getOpcode()==Instruction::Sub){
										if((LL->getOperand(0)->getName()).compare(IP->getName())==0){
											NValue1 = BinaryOperator::Create((Instruction::BinaryOps)(Instruction::Add),LL->getOperand(0),LL->getOperand(1),"inc");
											ReplaceInstWithInst(dyn_cast<Instruction>(LL),NValue1);
											break;
										}
									}else if(LL->getOpcode()==Instruction::Mul){
										if((LL->getOperand(0)->getName()).compare(IP->getName())==0){
											NValue1 = BinaryOperator::Create((Instruction::BinaryOps)(Instruction::SDiv),LL->getOperand(0),LL->getOperand(1),"div");
											ReplaceInstWithInst(dyn_cast<Instruction>(LL),NValue1);
											break;
										}
									}else if(LL->getOpcode()==Instruction::SDiv){
										if((LL->getOperand(0)->getName()).compare(IP->getName())==0){
											NValue1 = BinaryOperator::Create((Instruction::BinaryOps)(Instruction::Mul),LL->getOperand(0),LL->getOperand(1),"mul");
											ReplaceInstWithInst(dyn_cast<Instruction>(LL),NValue1);
											break;
										}
									}
										
								}

								//Handling Loop Body.
								Instruction *final_expr;
								
								for(auto* bb : L->getBlocks()){
									if(bb == parentLoopHead || bb == parentLoopPred || bb == parentLoopLatch)
										continue;
									auto last_access = bb->begin();
									for (auto LI = bb->begin();LI!=bb->end();LI++) {
										final_expr = BinaryOperator::Create((Instruction::BinaryOps)(Instruction::Sub),oldLowerBound,dyn_cast<Instruction>(IP),"Myname",dyn_cast<Instruction>(LI));
										last_access = LI;
										break;
									}
									for(;last_access!=bb->end();last_access++){
										for(unsigned int i=0;i<last_access->getNumOperands();i++){
											if(last_access->getOperand(i)->getName()==IP->getName()){
												last_access->setOperand(i,final_expr);
											}
										}
									}

								}

							}
						} 
					}
				}
			}
		}									

	}
	return true;
	}

};

}

char LoopReversal::ID = 0;
static RegisterPass<LoopReversal> X("LoopReversal", "Class Assignment Transformation Pass");