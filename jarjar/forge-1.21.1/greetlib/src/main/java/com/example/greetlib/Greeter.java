package com.example.greetlib;

/// A trivial library function. The point of the example is not what this does, but that the mod can call it
/// at runtime because the library jar is embedded (jar-in-jar) inside the mod and made loadable for the
/// target modloader.
public final class Greeter {
    private Greeter() { }

    public static String greet(String who) {
        return "GreetLib says hi to " + who + " (nested lib)";
    }
}
