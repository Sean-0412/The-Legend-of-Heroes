/**
 * MapRenderer.java
 * 說明: 負責將地圖格子、傳送門、敵人與玩家等元素繪製到畫面上。
 */

package src;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

class MapRenderer {
    private final GamePanel game;

    MapRenderer(GamePanel game) {
        this.game = game;
    }

    void drawMap(Graphics2D g2d) {
        Color floorA;
        Color floorB;
        Color wallCol;
        if (game.mapIndex == 0) {
            floorA = new Color(0, 100, 0);
            floorB = new Color(0, 120, 0);
            wallCol = new Color(100, 80, 50);
        } else {
            floorA = new Color(40, 35, 30);
            floorB = new Color(55, 50, 42);
            wallCol = new Color(80, 70, 60);
        }
        int arc = game.TILE_SIZE / 4;
        for (int y = 0; y < game.map.length; y++) {
            for (int x = 0; x < game.map[0].length; x++) {
                if (game.map[y][x] == 1) {
                    g2d.setColor(wallCol);
                } else {
                    g2d.setColor(((x + y) % 2 == 0) ? floorA : floorB);
                }
                g2d.fillRoundRect(x * game.TILE_SIZE, y * game.TILE_SIZE, game.TILE_SIZE, game.TILE_SIZE, arc, arc);
            }
        }

        Color portalColor = (game.mapIndex == 0) ? new Color(255, 200, 50, 180) : new Color(80, 160, 255, 180);
        g2d.setColor(portalColor);
        if (game.mapIndex == 0) {
            g2d.fillRoundRect(19 * game.TILE_SIZE, 7 * game.TILE_SIZE, game.TILE_SIZE, 2 * game.TILE_SIZE, 10, 10);
            g2d.setColor(portalColor.darker());
            g2d.setFont(new Font("Microsoft JhengHei", Font.BOLD, 11));
            g2d.drawString("→地下城", 19 * game.TILE_SIZE - 4, 7 * game.TILE_SIZE - 4);
        } else {
            g2d.fillRoundRect(0, 7 * game.TILE_SIZE, game.TILE_SIZE, 2 * game.TILE_SIZE, 10, 10);
            g2d.setColor(portalColor.darker());
            g2d.setFont(new Font("Microsoft JhengHei", Font.BOLD, 11));
            g2d.drawString("←草原", 2, 7 * game.TILE_SIZE - 4);
        }

        for (Enemy e : game.enemies) {
            if (e.shouldRenderOnMap()) {
                g2d.setColor(Color.RED);
                g2d.fillOval((int) e.x + 8, (int) e.y + 8, 24, 24);
            }
        }

        for (Companion c : game.companions) {
            g2d.setColor(new Color(100, 200, 100));
            g2d.fillOval((int) c.x + 8, (int) c.y + 8, 24, 24);
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Microsoft JhengHei", Font.BOLD, 10));
            g2d.drawString(c.name, (int) c.x - 5, (int) c.y - 5);
        }

        int playerSpriteSize = 40;
        BufferedImage currentPlayerSprite = game.getCurrentPlayerSprite();
        int drawW = game.getMapPlayerDrawWidth(currentPlayerSprite, playerSpriteSize);
        int drawH = game.getMapPlayerDrawHeight(currentPlayerSprite, playerSpriteSize);
        int playerSpriteOffsetX = (game.TILE_SIZE - drawW) / 2;
        int playerSpriteOffsetY = (game.TILE_SIZE - drawH) / 2;

        if (currentPlayerSprite != null) {
            g2d.drawImage(currentPlayerSprite,
                (int) game.player.x + playerSpriteOffsetX,
                (int) game.player.y + playerSpriteOffsetY,
                drawW,
                drawH,
                    null);
        } else {
            g2d.setColor(Color.BLUE);
            g2d.fillOval((int) game.player.x + 8, (int) game.player.y + 8, 24, 24);
        }

        int mw = 90, mh = 32;
        game.menuRect = new Rectangle(10, game.getHeight() - mh - 10, mw, mh);

        g2d.setColor(new Color(30, 30, 45, 230));
        g2d.fillRoundRect(game.menuRect.x, game.menuRect.y, game.menuRect.width, game.menuRect.height, 8, 8);
        g2d.setColor(new Color(180, 200, 255));
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRoundRect(game.menuRect.x, game.menuRect.y, game.menuRect.width, game.menuRect.height, 8, 8);
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Microsoft JhengHei", Font.BOLD, 13));
        g2d.drawString("選單", game.menuRect.x + 28, game.menuRect.y + 21);
    }
}
