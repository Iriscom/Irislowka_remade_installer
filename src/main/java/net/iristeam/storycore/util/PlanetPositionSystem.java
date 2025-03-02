package net.iristeam.storycore.util;

import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

import static net.iristeam.storycore.StoryCore.MS;
import static net.iristeam.storycore.world.dimensions.ModDimensions.*;
public class PlanetPositionSystem {
    private static final double[] DISTANCES = {39.0, 72.0, 100.0, 152.0, 520.0, 958.0, 1922.0, 3005.0};
    private static final double[] PERIODS   = {0.24, 0.62, 1.0, 1.88, 11.86, 29.46, 84.01, 164.8};
    private static final double[] SIZES = {13.927, 0.04879, 0.12104, 0.12742, 0.06779, 1.3982, 1.1646, 0.50724, 0.49244, 0.02376};

    public static Vec3d getScale(int planetId){
        if (planetId < 0 || planetId >= DISTANCES.length+1) return null;
        return new Vec3d(SIZES[planetId],SIZES[planetId],SIZES[planetId]);
    }

    public static Vec2f getPosition(int planetId) {
//        planetId--;
        if (planetId < 0 || planetId >= DISTANCES.length+1) return null;
        if (planetId == 0) return new Vec2f(0, 0);
        planetId--;
        double angle = (2 * Math.PI / PERIODS[planetId]) * MS.getWorld( STORY_SPACE_LEVEL_KEY).getTime()/10000;
        return new Vec2f((float) (DISTANCES[planetId] * Math.cos(angle)), (float) (DISTANCES[planetId] * Math.sin(angle)));
    }
}
