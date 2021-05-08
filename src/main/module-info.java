module matt.hurricanefx.eye {

    requires kotlin.stdlib.jdk8;
    requires kotlin.stdlib.jdk7;
    requires kotlin.reflect;



    requires transitive matt.kjlib;
    requires transitive javafx.base;

    requires java.desktop;

    exports matt.hurricanefx.eye;
}