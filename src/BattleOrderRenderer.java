/**
 * BattleOrderRenderer.java
 * 說明：負責繪製戰鬥行動順序、目前行動者及下一次行動位置預覽。
 */

package src;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.util.List;

final class BattleOrderRenderer {
    private static final int BAR_X = 20;
    private static final int BAR_Y = 100;
    private static final int UNIT_WIDTH = 40;
    private static final int UNIT_HEIGHT = 30;
    private static final int GAP = 5;
    private static final int MAX_DISPLAY = 8;

    private final GamePanel game;
    private final BattleManager battleManager;

    BattleOrderRenderer(GamePanel game, BattleManager battleManager) {
        this.game = game;
        this.battleManager = battleManager;
    }

    void draw(Graphics2D g2d) {
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Microsoft JhengHei", Font.BOLD, 12));
        g2d.drawString("行動順序:", BAR_X, BAR_Y - 5);

        if (game.battleOrder.isEmpty()) {
            return;
        }

        List<Integer> visibleOrderIndices = battleManager.buildVisibleBattleOrderIndices();
        int displayCount = Math.min(visibleOrderIndices.size(), MAX_DISPLAY);
        for (int i = 0; i < displayCount; i++) {
            BattleUnit unit = game.battleOrder.get(visibleOrderIndices.get(i));
            drawUnit(g2d, unit, BAR_X + i * (UNIT_WIDTH + GAP), BAR_Y, i == 0);
        }

        drawProjectedUnit(g2d);
    }

    private void drawUnit(Graphics2D g2d, BattleUnit unit, int x, int y, boolean active) {
        Color fillColor = getUnitColor(unit, active ? 255 : 150);
        if (active) {
            fillColor = new Color(
                    Math.min(255, fillColor.getRed() + 80),
                    Math.min(255, fillColor.getGreen() + 80),
                    Math.min(255, fillColor.getBlue() + 80),
                    255);
        }

        g2d.setColor(fillColor);
        g2d.fillRect(x, y, UNIT_WIDTH, UNIT_HEIGHT);
        g2d.setColor(active ? new Color(255, 200, 0) : new Color(100, 100, 100));
        g2d.setStroke(new BasicStroke(active ? 3 : 1));
        g2d.drawRect(x, y, UNIT_WIDTH, UNIT_HEIGHT);
        drawCenteredLabel(g2d, getUnitLabel(unit), x, y);
    }

    private void drawProjectedUnit(Graphics2D g2d) {
        if (!battleManager.isBattleOrderPreviewActive()) {
            return;
        }

        int previewIndex = battleManager.getProjectedBattleOrderIndex(
                game.currentActor, battleManager.getPendingActionCostForPreview());
        if (previewIndex < 0 || previewIndex >= MAX_DISPLAY) {
            return;
        }

        BattleUnit actor = battleManager.findBattleUnit(game.currentActor);
        if (actor == null || !battleManager.isBattleUnitVisible(actor)) {
            return;
        }

        int x = BAR_X + previewIndex * (UNIT_WIDTH + GAP);
        int y = BAR_Y - UNIT_HEIGHT - 8;
        g2d.setColor(getUnitColor(actor, 110));
        g2d.fillRect(x, y, UNIT_WIDTH, UNIT_HEIGHT);
        g2d.setColor(new Color(255, 215, 120));
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRect(x, y, UNIT_WIDTH, UNIT_HEIGHT);
        drawCenteredLabel(g2d, getUnitLabel(actor), x, y);
    }

    private Color getUnitColor(BattleUnit unit, int alpha) {
        if (unit.unit instanceof Player) {
            return new Color(0, 0, 255, alpha);
        }
        if (unit.unit instanceof Companion) {
            return new Color(0, 255, 0, alpha);
        }
        if (unit.unit instanceof Enemy) {
            return new Color(255, 0, 0, alpha);
        }
        return new Color(100, 100, 100, Math.min(alpha, 100));
    }

    private String getUnitLabel(BattleUnit unit) {
        if (unit.unit instanceof Player) {
            return "勇者";
        }
        if (unit.unit instanceof Companion) {
            return ((Companion) unit.unit).name;
        }
        if (unit.unit instanceof Enemy) {
            return "敵人";
        }
        return "?";
    }

    private void drawCenteredLabel(Graphics2D g2d, String label, int x, int y) {
        g2d.setStroke(new BasicStroke(1));
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Microsoft JhengHei", Font.BOLD, 12));
        FontMetrics metrics = g2d.getFontMetrics();
        int textX = x + (UNIT_WIDTH - metrics.stringWidth(label)) / 2;
        int textY = y + UNIT_HEIGHT / 2 + metrics.getAscent() / 2;
        g2d.drawString(label, textX, textY);
    }
}
