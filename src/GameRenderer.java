package src;

import java.awt.Graphics2D;
import java.awt.RenderingHints;

class GameRenderer {
    private final GamePanel game;
    private final MenuUI menuUI;
    private final MapRenderer mapRenderer;
    private final BattleRenderer battleRenderer;

    GameRenderer(GamePanel game, MenuUI menuUI, MapRenderer mapRenderer, BattleRenderer battleRenderer) {
        this.game = game;
        this.menuUI = menuUI;
        this.mapRenderer = mapRenderer;
        this.battleRenderer = battleRenderer;
    }

    void render(Graphics2D g2d) {
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (game.showLoadMenu) {
            menuUI.drawLoadMenu(g2d);
            return;
        }

        if (game.state == -1) {
            menuUI.drawMainMenu(g2d);
            return;
        }

        if (game.state == 2) {
            battleRenderer.drawSettlement(g2d);
            return;
        }

        if (game.state == 1) {
            battleRenderer.drawBattle(g2d);
            return;
        }

        mapRenderer.drawMap(g2d);

        if (game.showMapMenu) {
            menuUI.drawMapMenu(g2d);
        }
    }
}
