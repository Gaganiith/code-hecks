class Main {
	main():IO {
		new IO.out_string("Hello world!\n")
	};
};

class A inherits B {
    test( x : Int ) : String {
        "x"
    };
};

class B {
    a : Int;
    test( x : Int ) : Int {
        0
    };
};
