class A {
    b : Int <- 4;
    fun(x : Int) : Int {
        {
            x <- x + b;
        }
    };
};

class B {
    a : A;
    b : A <- new A;
    foo(x : Int, y : A) : A {
        {
            y <- a;
            y <- new A;
        }
    };
};

class Main {
    main() : Int {
        0
    };
};
