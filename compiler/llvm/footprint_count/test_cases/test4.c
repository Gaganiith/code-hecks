// test case when our variable is declared in Nested for loops as well. Our code is able to detect them.i
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
