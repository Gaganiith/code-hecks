class Main {
    main () : Int {
        0
    };
};
class C {
    a : Int;
    b : String;
    c : String;
    fun() : Int {
        let x : Int <- 30 in
        let b : String in
        let a : Int <- 50 in {
            x <- a;
            b <- "hi";
            a <- x - a;
	    a <- x * a;
            c <- "hello";
            0;
        } 
    };
};
