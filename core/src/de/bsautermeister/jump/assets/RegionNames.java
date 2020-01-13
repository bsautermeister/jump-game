package de.bsautermeister.jump.assets;

public class RegionNames {
    public static String SMALL_PLAYER_WALK_TPL = "small_player_*_walk";
    public static String SMALL_PLAYER_STAND_TPL = "small_player_*_stand";
    public static String SMALL_PLAYER_JUMP_TPL = "small_player_*_jump";
    public static String SMALL_PLAYER_CROUCH_TPL = "small_player_*_crouch";
    public static String SMALL_PLAYER_DEAD_TPL = "small_player_*_dead";
    public static String SMALL_PLAYER_TURN_TPL = "small_player_*_turn";
    public static String SMALL_PLAYER_VICTORY = "small_player_3_victory";
    public static String SMALL_PLAYER_BEER_VICTORY = "small_player_3_beer_victory";
    public static String SMALL_PLAYER_ROCKNROLL_VICTORY = "small_player_3_rocknroll_victory";
    public static String SMALL_PLAYER_DROWN_TPL = "small_player_*_drown";
    public static String BIG_PLAYER_WALK = "big_player_walk";
    public static String BIG_PLAYER_STAND = "big_player_stand";
    public static String BIG_PLAYER_JUMP = "big_player_jump";
    public static String BIG_PLAYER_CROUCH = "big_player_crouch";
    public static String BIG_PLAYER_TURN = "big_player_turn";
    public static String BIG_PLAYER_DROWN = "big_player_drown";
    public static String BIG_PLAYER_DEAD = "big_player_dead";
    public static String BIG_PLAYER_VICTORY = "big_player_victory";
    public static String BIG_PLAYER_BEER_VICTORY = "big_player_beer_victory";
    public static String BIG_PLAYER_WALK_ON_FIRE = "big_player_walk_on_fire";
    public static String BIG_PLAYER_STAND_ON_FIRE = "big_player_stand_on_fire";
    public static String BIG_PLAYER_JUMP_ON_FIRE = "big_player_jump_on_fire";
    public static String BIG_PLAYER_CROUCH_ON_FIRE = "big_player_crouch_on_fire";
    public static String BIG_PLAYER_TURN_ON_FIRE = "big_player_turn_on_fire";
    public static String BIG_PLAYER_DROWN_ON_FIRE = "big_player_drown_on_fire";
    public static String GOOMBA = "goomba";
    public static String GOOMBA_STOMP = "goomba_stomp";
    public static String KOOPA = "koopa";
    public static String KOOPA_MOVING = "koopa_moving";
    public static String FLOWER = "flower";
    public static String FISH = "fish";
    public static String SPIKY = "spiky";
    public static String MUSHROOM = "mushroom";
    public static String COIN = "coin";
    public static String BRICK_FRAGMENT = "brick_fragment";
    public static String WATER = "water";
    public static String PLATFORM2 = "platform2";
    public static String PLATFORM3 = "platform3";
    public static String PLATFORM4 = "platform4";
    public static String BREAK_PLATFORM2 = "break_platform2";
    public static String BREAK_PLATFORM3 = "break_platform3";
    public static String BREAK_PLATFORM4 = "break_platform4";
    public static String BEER = "beer";
    public static String PREZEL = "prezel";
    public static String PREZEL_BULLET = "prezel_bullet";
    public static String BACKGROUND_OVERLAY = "bg-overlay";

    public static String LOADING_LOGO = "title";
    public static String LOADING_ANIMATION = "loading";
    public static String LOADING_FRAME = "frame";
    public static String LOADING_BAR_HIDDEN = "loading-hidden";
    public static String LOADING_BACKGROUND = "screen-bg";
    public static String LOADING_FRAME_BACKGROUND = "frame-bg";

    private RegionNames() {}

    public static String fromTemplate(String template, int value) {
        return template.replace("*", String.valueOf(value));
    }
}
