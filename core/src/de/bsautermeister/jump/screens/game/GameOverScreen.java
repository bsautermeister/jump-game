package de.bsautermeister.jump.screens.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import de.bsautermeister.jump.Cfg;
import de.bsautermeister.jump.assets.AssetDescriptors;
import de.bsautermeister.jump.commons.GameApp;
import de.bsautermeister.jump.screens.ScreenBase;
import de.bsautermeister.jump.screens.menu.SelectLevelScreen;
import de.bsautermeister.jump.utils.GdxUtils;

public class GameOverScreen extends ScreenBase {
    private final Viewport viewport;
    private final Stage stage;

    public GameOverScreen(GameApp game) {
        super(game);
        this.viewport = new FitViewport(Cfg.WORLD_WIDTH, Cfg.WORLD_HEIGHT);
        this.stage = new Stage(viewport, game.getBatch());
        initialize();
    }

    private void initialize() {
        BitmapFont font = getAssetManager().get(AssetDescriptors.Fonts.MARIO32);
        Label.LabelStyle labelStyle = new Label.LabelStyle(font, Color.WHITE);

        Table table = new Table();
        table.center();
        table.setFillParent(true);

        Label gameOverLabel = new Label("GAME OVER", labelStyle);
        table.add(gameOverLabel).expandX();

        stage.addActor(table);
    }

    @Override
    public void render(float delta) {
        GdxUtils.clearScreen(Color.BLACK);
        stage.draw();

        if (Gdx.input.isTouched()) {
            setScreen(new SelectLevelScreen(getGame(), 1));
        }
    }

    @Override
    public void dispose() {
        stage.dispose();
    }
}