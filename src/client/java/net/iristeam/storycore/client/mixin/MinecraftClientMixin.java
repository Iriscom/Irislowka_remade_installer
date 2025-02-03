package net.iristeam.storycore.client.mixin;

import net.iristeam.storycore.client.interfaces.MinecraftClientInterface;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.RunArgs;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin implements MinecraftClientInterface {
    private static boolean tel = false;//defaults false
    private static long startTime;

    @Override
    public long getStartTime() {
        return startTime;
    }

    @Override
    public boolean isTeleporting() {
        return tel;
    }

    @Override
    public void setTeleporting(boolean Telep) {
        tel = Telep;
        startTime = System.currentTimeMillis();
    }
}
