package de.bsautermeister.jump.screens.game.level;

public class LevelMetadata {
    private static final LevelInfo[] LEVELS = new LevelInfo[]{
            LevelInfo.tutorial(),

            new LevelInfo(150, 6000, 7500, 9000, 0),
            new LevelInfo(240, 10000, 12000, 14000, 1),
            new LevelInfo(240, 10000, 12000, 13500, 3),
            new LevelInfo(180, 9000, 11000, 13000, 6),
            new LevelInfo(210, 10000, 12000, 14000, 9)
    };

    private LevelMetadata() {}

    public static LevelInfo getLevelInfo(int level) {
        return LEVELS[level];
    }
}