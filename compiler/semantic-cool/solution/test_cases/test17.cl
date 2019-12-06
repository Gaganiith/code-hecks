class Main {
	main():IO {
		new IO.out_string("Hello world!\n")
	};
};

class A {
    a : Int;
    b : String;
    test() : Object { 
        case b of
                a : B => a <- 0;
            esac;
    };
};
