class A inherits B {
    a : Int <- 1;
    b : String <- "Gagan";
    foo(x : Int) : Int {
        {
            x <- x + a;
        }
    };
};

class B inherits IO {
    a1 : A;
    a2 : A <- new A;
    fun(x : Int, y : A) : A {
        {
            y <- a1;
            y <- new A;
        }
    };
};

class Main {
    i : Int;
	io : IO <- new IO;
    main() : Int {
        0
    };
};