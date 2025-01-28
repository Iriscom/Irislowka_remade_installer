package net.iristeam.storycore.client.renderer.dimension;

import net.minecraft.client.render.DimensionEffects;
import net.minecraft.util.math.Vec3d;

public class SpaseDimensionEffect extends DimensionEffects {
    public SpaseDimensionEffect(float cloudsHeight, boolean alternateSkyColor, SkyType skyType, boolean brightenLighting, boolean darkened) {
        super(cloudsHeight, alternateSkyColor, skyType, brightenLighting, darkened);
    }
    public SpaseDimensionEffect() {
        super(Float.NaN, false, SkyType.NORMAL, false, false);
    }

    @Override
    public Vec3d adjustFogColor(Vec3d color, float sunHeight) {
        return new Vec3d(0,0,0);
    }

    @Override
    public boolean useThickFog(int camX, int camY) {
        return false;
    }
}
