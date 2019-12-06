#include <iostream>
#include <cstring>
#include <vector>
#include <climits>
using namespace std;

#define e 400
#define d 20

//Reverse the output string.
void print_reverse(string str) 
{ 
   for (int i=str.length()-1; i>=0; i--) 
      cout << str[i];  
} 

// Match function returns the penalty specified in the Problem
int match(char tar,char que){
    int match_arr [] = {-91,114,31,123,114,-100,125,31,31,125,-100,114,123,31,114,-91};
    int a,b;
    if(tar == 'A'){
        a = 0;
    }else if (tar == 'C')
    {
        a = 1;
    }else if (tar == 'G')
    {
        a = 2;
    }else
    {
        a=3;
    }
    
    if(que == 'A'){
        b = 0;
    }else if (que == 'C')
    {
        b = 1;
    }else if (que == 'G')
    {
        b = 2;
    }else
    {
        b=3;
    }
    return match_arr[a*4+b];
}

int main(){
    string target,query;
    
    // Taking Inputs for Query and Target.
    getline(cin, target); 
    getline(cin, query); 

    // Creating Scoring Matrix.
    // Initializing 2-D Array
    int target_length = target.length();
    int query_length = query.length();

    vector<vector<long long>> A(target_length+1,vector<long long>(query_length+1,0));
    vector<vector<long long>> B(target_length+1,vector<long long>(query_length+1,0));
    vector<vector<long long>> C(target_length+1,vector<long long>(query_length+1,0));
    vector<vector<long long>> tracebackA(target_length+1,vector<long long>(query_length+1,0));
    vector<vector<long long>> tracebackB(target_length+1,vector<long long>(query_length+1,0));
    vector<vector<long long>> tracebackC(target_length+1,vector<long long>(query_length+1,0));


    // Updating Initial values in the Table
    A[0][0] = 0;
    B[0][0] = 0;
    C[0][0] = 0;
    tracebackA[0][0]=0;
    tracebackB[0][0]=0;
    tracebackC[0][0]=0;


    for(int i=1; i<=query_length; i++){
        A[0][i] = INT_MAX  ;
        B[0][i] = INT_MAX  ;
        C[0][i] = e+(i-1)*d;
        tracebackA[0][i] = -1;
        tracebackB[0][i] = -1;
        tracebackC[0][i] = -1;
    }
    for(int i=1; i<=target_length; i++){
        A[i][0] = INT_MAX;
        B[i][0] = e+(i-1)*d;
        C[i][0] = INT_MAX;
        tracebackA[i][0] = -1;
        tracebackB[i][0] = -1;
        tracebackC[i][0] = -1;
    }

    // Filling the rest of the Scoring matrix
    for(int i=1 ; i <=target_length;i++){
        for(int j=1; j<=query_length;j++){
            
            // These 3 A[i][j],B[i][j],C[i][j] fills the scoring matrix
            A[i][j] = min(min(A[i-1][j-1],B[i-1][j-1]),C[i-1][j-1]) + match(target[i-1],query[j-1]) ;
            B[i][j] = min(min(A[i-1][j]+e,B[i-1][j]+d),C[i-1][j]+e) ;
            C[i][j] = min(min(A[i][j-1]+e,B[i][j-1]+e),C[i][j-1]+d) ;
            
              
            //traceback matrix for A
            if((A[i-1][j-1] < B[i-1][j-1])&&(A[i-1][j-1] < C[i-1][j-1])){
                tracebackA[i][j] = 0;
            }
            else if (B[i-1][j-1]< C[i-1][j-1]){
                tracebackA[i][j] = 1;
            }
            else{
                tracebackA[i][j] = 2;
            }

            //traceback matrix for B
            if((A[i-1][j]+e < B[i-1][j]+d)&&(A[i-1][j]+e < C[i-1][j]+e)){
                tracebackB[i][j] = 0;
            }
            else if (B[i-1][j]+d < C[i-1][j]+e){
                tracebackB[i][j] = 1;
            }
            else{
                tracebackB[i][j] = 2;
            }

            //traceback matrix for C
            if((A[i][j-1]+e < B[i][j-1]+e)&&(A[i][j-1]+e < C[i][j-1]+d)){
                tracebackC[i][j] = 0;
            }
            else if (B[i][j-1]+e < C[i][j-1]+d){
                tracebackC[i][j] = 1;
            }
            else{
                tracebackC[i][j] = 2;
            }            
        }
    }
    
    //Tracebacking the Scoring Matrix.
    int tar_len = target_length,
        que_len = query_length;
    string print_target;
    string print_query;
    string temp;
    int which_array;
    if((A[tar_len][que_len] < B[tar_len][que_len])&&(A[tar_len][que_len] < C[tar_len][que_len])){
        which_array = 0;
    }
    else if (B[tar_len][que_len] < C[tar_len][que_len]){
        which_array = 1;
    }
    else{
        which_array = 2;
    }

    while( tar_len!=0 && que_len!=0){

        if(which_array == 0){
            if(tracebackA[tar_len][que_len] == 0){
                temp = target[--tar_len];
                print_target.append(temp);
                temp = query[--que_len];
                print_query.append(temp);
                which_array = 0;                
            }else if(tracebackA[tar_len][que_len] == 1){
                temp = target[--tar_len];
                print_target.append(temp);
                temp = query[--que_len];
                print_query.append(temp);
                which_array = 1;
            }else
            {
                temp = target[--tar_len];
                print_target.append(temp);
                temp = query[--que_len];
                print_query.append(temp);
                which_array = 2;
            }
            
        }else if(which_array == 1){
            if(tracebackB[tar_len][que_len] == 0){
                temp = target[--tar_len];
                print_target.append(temp);
                temp = "_";
                print_query.append(temp);                
                which_array = 0;                
            }else if(tracebackB[tar_len][que_len] == 1){
                temp = target[--tar_len];
                print_target.append(temp);
                temp = "_";
                print_query.append(temp);                
                which_array = 1;
            }else
            {
                temp = target[--tar_len];
                print_target.append(temp);
                temp = "_";
                print_query.append(temp);                
                which_array = 2;
            }

        }else{
            if(tracebackC[tar_len][que_len] == 0){
                temp = "_";
                print_target.append(temp);
                temp = query[--que_len];
                print_query.append(temp);
                which_array = 0;                
            }else if(tracebackC[tar_len][que_len] == 1){
                temp = "_";
                print_target.append(temp);
                temp = query[--que_len];
                print_query.append(temp);               
                which_array = 1;
            }else
            {
                temp = "_";
                print_target.append(temp);
                temp = query[--que_len];
                print_query.append(temp);
                which_array = 2;
            }
        }
    }

    //Eating the leftover from query if remains.

    while(que_len>0){
        temp = "_";
        print_target.append(temp);
        temp = query[--que_len];
        print_query.append(temp);
    }

    //Eating the leftover from target if remains.
    while(tar_len>0){
        temp = target[--tar_len];
            print_target.append(temp);
            temp = "_";
            print_query.append(temp);
    }
    
    
    //Printing results
    cout << min(min(A[target_length][query_length],B[target_length][query_length]),C[target_length][query_length]);
    cout<<"\n";
    print_reverse(print_target);
    cout<<"\n";
    print_reverse(print_query);
    return 0;
}