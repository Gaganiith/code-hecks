// test case when our variable is declared as a global variable and it has a footprint in one of the functions as well. Our code is able to detect them.
int i = 22;

int foo ( ) 
{
	int i ;
	i= 0 ;

	for ( int i = 0 ; i < 100; i++) {
		int temp = 0 ;
		i=11;

		for(int i = 0 ; i<10;i++){
			i = 10;
		}

	}

return i;
}

int foo2(){
	
	return i;
}