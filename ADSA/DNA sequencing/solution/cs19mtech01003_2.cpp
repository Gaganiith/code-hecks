#include <iostream>
#include <cstring>
#include <vector>

using namespace std;

#define gap_penalty 30

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

    vector<vector<long long>> ScoringMatrix(target_length+1,vector<long long>(query_length+1,0));
    vector<vector<long long>> traceback(target_length+1,vector<long long>(query_length+1,0));

    // Updating Initial values in the Table
    ScoringMatrix[0][0] = 0;
    traceback[0][0]=0;

    int beside,bottom,diagonal;

    for(int i=1; i<=query_length; i++){
        ScoringMatrix[0][i] = ScoringMatrix[0][i-1] + gap_penalty;
        traceback[0][i] = -1;
    }
    for(int i=1; i<=target_length; i++){
        ScoringMatrix[i][0] = ScoringMatrix[i-1][0] + gap_penalty;
        traceback[i][0] = -1;
    }
    
    // Filling the rest of the Scoring matrix
    for(int i=1 ; i <=target_length;i++){
        for(int j=1; j<=query_length;j++){
            beside = ScoringMatrix[i][j-1]+gap_penalty;
            bottom = ScoringMatrix[i-1][j]+gap_penalty;
            diagonal = ScoringMatrix[i-1][j-1] + match(target[i-1],query[j-1]);

            if((beside < bottom)&&(beside < diagonal)){
                ScoringMatrix[i][j] = beside;
                traceback[i][j] = 0;
            }
            else if (bottom< diagonal){
                ScoringMatrix[i][j] = bottom;
                traceback[i][j] = 1;
            }
            else{
                ScoringMatrix[i][j] = diagonal;
                traceback[i][j] = 2;
            }

            ScoringMatrix[i][j] = min(min(beside,bottom),diagonal);
        }
    }
    

    //Tracebacking the Scoring Matrix.
    int tar_len = target_length,
        que_len = query_length;
    string print_target;
    string print_query;
    string temp;
    while( tar_len!=0 && que_len!=0){
        if(traceback[tar_len][que_len] == 1){
            temp = target[--tar_len];
            print_target.append(temp);
            temp = "_";
            print_query.append(temp);

        }else if (traceback[tar_len][que_len] == 0)
        {
            temp = "_";
            print_target.append(temp);
            temp = query[--que_len];
            print_query.append(temp);
        }else
        {
            temp = target[--tar_len];
            print_target.append(temp);
            temp = query[--que_len];
            print_query.append(temp);
        }
    }
    while(que_len>0){
         temp = "_";
            print_target.append(temp);
            temp = query[--que_len];
            print_query.append(temp);
    }
    while(tar_len>0){
        temp = target[--tar_len];
            print_target.append(temp);
            temp = "_";
            print_query.append(temp);
    }

    //Printing results
    cout<<ScoringMatrix[target_length][query_length]<<"\n";   
    print_reverse(print_target);
    cout<<"\n";
    print_reverse(print_query);

    return 0;
}