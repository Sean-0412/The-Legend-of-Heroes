/**
 * NpcRenderer.java
 * 說明：負責繪製村莊 NPC、名稱與互動提示，互動規則仍由 GamePanel 管理。
 */

package src;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

final class NpcRenderer {
    private final GamePanel game;

    NpcRenderer(GamePanel game) {
        this.game = game;
    }

    void drawShop(Graphics2D g2d) {
        if (game.mapIndex != game.SHOP_NPC_MAP_INDEX) {
            return;
        }
        int x = game.SHOP_NPC_TILE_X * game.TILE_SIZE;
        int y = game.SHOP_NPC_TILE_Y * game.TILE_SIZE;
        if (!drawSprite(g2d, game.shopNpcSprite, game.getShopNpcCenterX(), game.getShopNpcCenterY(),
                1.6, 1.8, "商人", x + 2, y - 6)) {
            drawFallback(g2d, x, y, new Color(118, 74, 42), "商人", x + 5);
        }
        if (game.isNearShopNpc()) {
            drawPrompt(g2d, x, y, 90, "對話");
        }
    }

    void drawInn(Graphics2D g2d) {
        if (game.mapIndex != game.INN_NPC_MAP_INDEX) {
            return;
        }
        int x = game.INN_NPC_TILE_X * game.TILE_SIZE;
        int y = game.INN_NPC_TILE_Y * game.TILE_SIZE;
        if (!drawSprite(g2d, game.innNpcSprite, game.getInnNpcCenterX(), game.getInnNpcCenterY(),
                1.05, 1.2, "旅館", x + 2, y - 6)) {
            drawFallback(g2d, x, y, new Color(56, 112, 154), "旅館", x + 5);
        }
        if (game.isNearInnNpc()) {
            drawPrompt(g2d, x, y, 90, "對話");
        }
    }

    void drawTrainer(Graphics2D g2d) {
        if (game.mapIndex != game.TRAINER_NPC_MAP_INDEX) {
            return;
        }
        int x = game.TRAINER_NPC_TILE_X * game.TILE_SIZE;
        int y = game.TRAINER_NPC_TILE_Y * game.TILE_SIZE;
        if (!drawSprite(g2d, game.trainerNpcSprite, game.getTrainerNpcCenterX(), game.getTrainerNpcCenterY(),
                1.6, 1.8, "訓練師", x - 4, y - 6)) {
            drawFallback(g2d, x, y, new Color(120, 62, 144), "訓練師", x - 2);
        }
        if (game.isNearTrainerNpc()) {
            drawPrompt(g2d, x, y, 96, "對話");
        }
    }

    void drawChief(Graphics2D g2d) {
        if (game.mapIndex != game.CHIEF_NPC_MAP_INDEX) {
            return;
        }
        int x = game.CHIEF_NPC_TILE_X * game.TILE_SIZE;
        int y = game.CHIEF_NPC_TILE_Y * game.TILE_SIZE;
        drawFallback(g2d, x, y, new Color(92, 92, 92), "村長", x + 5);
        g2d.setColor(new Color(245, 245, 245));
        g2d.fillArc(x + 9, y + 9, 22, 18, 180, 180);
        if (game.isNearChiefNpc()) {
            drawPrompt(g2d, x, y, 96, "右鍵交談");
        }
    }

    private boolean drawSprite(Graphics2D g2d, BufferedImage sprite, double centerX, double centerY,
            double widthScale, double heightScale, String label, int labelX, int labelY) {
        if (sprite == null) {
            return false;
        }
        int imageWidth = sprite.getWidth();
        int imageHeight = sprite.getHeight();
        double scale = Math.min(game.TILE_SIZE * widthScale / imageWidth,
                game.TILE_SIZE * heightScale / imageHeight);
        int drawWidth = (int) Math.round(imageWidth * scale);
        int drawHeight = (int) Math.round(imageHeight * scale);
        int drawX = (int) Math.round(centerX - drawWidth / 2.0);
        int drawY = (int) Math.round(centerY - drawHeight / 2.0);
        g2d.drawImage(sprite, drawX, drawY, drawWidth, drawHeight, null);
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Microsoft JhengHei", Font.BOLD, 12));
        g2d.drawString(label, labelX, labelY);
        return true;
    }

    private void drawFallback(Graphics2D g2d, int x, int y, Color bodyColor, String label, int labelX) {
        g2d.setColor(bodyColor);
        g2d.fillRoundRect(x + 8, y + 12, 24, 24, 8, 8);
        g2d.setColor(new Color(232, 208, 170));
        g2d.fillOval(x + 11, y + 4, 18, 18);
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Microsoft JhengHei", Font.BOLD, 11));
        g2d.drawString(label, labelX, y - 4);
    }

    private void drawPrompt(Graphics2D g2d, int x, int y, int width, String text) {
        g2d.setColor(new Color(0, 0, 0, 160));
        g2d.fillRoundRect(x - 24, y - 24, width, 18, 8, 8);
        g2d.setColor(new Color(255, 230, 120));
        g2d.drawString(text, x - 18, y - 10);
    }
}
