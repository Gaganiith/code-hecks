// test case when our variable is referenced by a pointer in the same lexical block , the footprint caused by pointers is also being detected.
int i = 22;

int foo ( ) 
{
	int temp =11;
	int i ;
	i= 0 ;
	int *ip;
	int **iip;

	ip = &i;
	iip = &ip;

	*ip = temp ;
	**iip = temp;

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