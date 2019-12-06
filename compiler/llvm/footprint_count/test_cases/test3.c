// test case when our variable is declared in for loop as well. Our code is able to detect them.
int foo ( ) 
{
	int i ;
	i= 0 ;

	for ( int i = 0 ; i < 100; i++) {
		int temp = 0 ;
		i=i+111;
	}

return i;
}