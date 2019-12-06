class Main {
    main () : Int {
        0
    };
};
class A {
    a : Int;
    b : Int;
    fun() : String {
        {
            if a = 5 then a = 4 else a = 7 fi ;
            if b = 2 then a = 1 else a = 0 fi ;
            "hi from fun";
        }
    };
};
