// test case when our variable is declared in Different functions. Our code is able to detect them.
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
	int i=2323;
	return i;
}