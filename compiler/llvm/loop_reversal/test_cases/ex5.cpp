#include<stdio.h>

int main(){
	int n = 10;
	int m = 3;
	int a[10] = {0};
	for(int i=n-1;i>=0;i--){
		a[i] = i + n;
		printf("%d ",a[i]);
		for(int j=0;j<n;j++){
			m = 1;
			printf("%d ",a[i]);
		}
	}
	return 0;
}

