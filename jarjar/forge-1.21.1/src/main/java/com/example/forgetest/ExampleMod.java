package com.example.forgetest;

import com.example.greetlib.Greeter;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod("forge_test")
public class ExampleMod {
    private static final Logger LOGGER = LoggerFactory.getLogger("Forge Test");

    public ExampleMod() {
        // Greeter lives in the embedded greetlib jar. If Jar-in-Jar is wired correctly, this class resolves at
        // runtime even though greetlib was never installed separately.
        LOGGER.info(Greeter.greet("Forge"));
    }
}
