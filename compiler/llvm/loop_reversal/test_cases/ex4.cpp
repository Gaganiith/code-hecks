#include<stdio.h>
int main(){
	int n = 10;
	int a[10] = {0};
	int m = 5;
	for(int i=0;i<n;i++){
		a[i] = i + n;
		for(int j=0;j<n;j++){
			for(int k = n-1;k>=0;k--){
				printf("%d ",a[i]);
			}
		}
		for(int j=0;j<n;j++){
			for(int k = n-1;k>=0;k--){
				printf("%d ",a[i]);
			}
		}
	}
	return 0;
}
