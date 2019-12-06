// test case when our variable is declared in multiple lexical blocks. Our code is able to detect them.
int foo ( ) 
{
	int i ;
	i= 0 ;

	{
		int i = 400;
	}

	{
		int i =200;
	}


return i;
}
