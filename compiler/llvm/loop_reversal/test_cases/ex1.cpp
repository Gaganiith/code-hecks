#include<stdio.h>
int main(){
	int n = 10;
	int a[10] = {0};

	for(int i=0;i<n;i++){
		a[i] = i + n;
		printf("%d ",a[i]);
	}

}
