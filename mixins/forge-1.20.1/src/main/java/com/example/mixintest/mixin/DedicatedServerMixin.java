package com.example.mixintest.mixin;

import net.minecraft.server.dedicated.DedicatedServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DedicatedServer.class)
public abstract class DedicatedServerMixin {
    private static final Logger LOGGER = LoggerFactory.getLogger("Mixin Test");

    @Inject(method = "initServer", at = @At("RETURN"))
    private void onServerStarted(CallbackInfoReturnable<Boolean> cir) {
        LOGGER.info("[mixin_test] DedicatedServer.initServer returned {} — mixin injected successfully!", cir.getReturnValue());
    }
}
