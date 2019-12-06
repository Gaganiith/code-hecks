#include<stdio.h>
int main(){
	int n = 10;
	int a[10] = {0};

	for(int i=n;i>0;i--){
		a[i] = i + n;
		for(int j=0;j<n;j++){
			a[i] = 3;
		}
		printf("%d ",a[i]);
	}
	return 0;
}
