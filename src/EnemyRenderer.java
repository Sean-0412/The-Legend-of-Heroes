/**
 * EnemyRenderer.java
 * 說明：依敵人種類繪製不同造型，供地圖與戰鬥畫面共同使用。
 */

package src;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.Path2D;

final class EnemyRenderer {
    private EnemyRenderer() {
    }

    static void draw(Graphics2D g2d, Enemy enemy, int centerX, int centerY) {
        draw(g2d, enemy, centerX, centerY, System.currentTimeMillis());
    }

    static void draw(Graphics2D g2d, Enemy enemy, int centerX, int centerY, long timeMs) {
        RenderingHints oldHints = g2d.getRenderingHints();
        Stroke oldStroke = g2d.getStroke();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int size = Math.max(20, enemy.displaySize);
        double phase = timeMs / 130.0 + enemy.animationPhaseOffset;
        switch (enemy.kind) {
            case SLIME:
                drawSlime(g2d, enemy.bodyColor, centerX, centerY, size, phase);
                break;
            case WOLF:
                drawWolf(g2d, enemy.bodyColor, centerX, centerY, size, phase);
                break;
            case GOBLIN:
                drawGoblin(g2d, enemy.bodyColor, centerX, centerY, size, phase);
                break;
            case SKELETON:
                drawSkeleton(g2d, enemy.bodyColor, centerX, centerY, size, phase);
                break;
            case SHADOW_MAGE:
                drawShadowMage(g2d, enemy.bodyColor, centerX, centerY, size, phase);
                break;
            case DEMON_KING:
                drawDemonKing(g2d, enemy.bodyColor, centerX, centerY, size, phase);
                break;
            default:
                drawGenericMonster(g2d, enemy.bodyColor, centerX, centerY, size, phase);
                break;
        }

        g2d.setStroke(oldStroke);
        g2d.setRenderingHints(oldHints);
    }

    private static void drawSlime(Graphics2D g, Color body, int x, int y, int s, double phase) {
        int bounce = (int) Math.round(Math.abs(Math.sin(phase)) * 3);
        int squash = (int) Math.round(Math.sin(phase) * 2);
        y -= bounce;
        s += squash;
        int r = s / 2;
        Path2D blob = new Path2D.Double();
        blob.moveTo(x - r, y + r / 2.0);
        blob.curveTo(x - r, y - r, x - r / 2.0, y - r, x, y - r);
        blob.curveTo(x + r / 2.0, y - r, x + r, y - r / 3.0, x + r, y + r / 2.0);
        blob.quadTo(x + r / 2.0, y + r, x, y + r * 0.65);
        blob.quadTo(x - r / 2.0, y + r, x - r, y + r / 2.0);
        g.setColor(body.darker());
        g.fill(blob);
        g.setColor(body);
        g.fillOval(x - r + 2, y - r + 1, s - 4, s - 5);
        drawEyes(g, x, y - s / 10, Math.max(2, s / 7), Color.BLACK);
        g.setColor(new Color(255, 255, 255, 150));
        g.fillOval(x - s / 4, y - s / 3, Math.max(3, s / 6), Math.max(2, s / 9));
    }

    private static void drawWolf(Graphics2D g, Color body, int x, int y, int s, double phase) {
        y += (int) Math.round(Math.sin(phase * 2) * 1.5);
        int r = s / 2;
        int stride = (int) Math.round(Math.sin(phase) * s / 7.0);
        g.setColor(body.darker());
        g.setStroke(new BasicStroke(Math.max(2f, s / 10f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine(x - s / 5, y + r / 3, x - s / 4 + stride, y + r);
        g.drawLine(x + s / 5, y + r / 3, x + s / 4 - stride, y + r);
        Polygon ears = new Polygon(
                new int[] { x - r, x - r / 2, x, x + r / 2, x + r },
                new int[] { y - r, y - r / 4, y - r / 2, y - r / 4, y - r }, 5);
        g.setColor(body.darker());
        g.fillPolygon(ears);
        Polygon head = new Polygon(
                new int[] { x - r + 2, x, x + r - 2, x + r / 2, x, x - r / 2 },
                new int[] { y - r / 3, y - r, y - r / 3, y + r / 2, y + r, y + r / 2 }, 6);
        g.setColor(body);
        g.fillPolygon(head);
        drawEyes(g, x, y - s / 8, Math.max(2, s / 8), new Color(255, 210, 70));
        g.setColor(new Color(215, 205, 190));
        g.fillOval(x - s / 5, y + s / 8, s * 2 / 5, s / 3);
        g.setColor(Color.BLACK);
        g.fillOval(x - 2, y + s / 7, 4, 3);
    }

    private static void drawGoblin(Graphics2D g, Color body, int x, int y, int s, double phase) {
        int step = (int) Math.round(Math.sin(phase) * s / 7.0);
        y += (int) Math.round(Math.abs(Math.sin(phase)) * -2);
        int r = s / 2;
        g.setColor(body.darker());
        g.setStroke(new BasicStroke(Math.max(2f, s / 11f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine(x - r / 2, y + r / 3, x - r + step, y + r);
        g.drawLine(x + r / 2, y + r / 3, x + r - step, y + r);
        g.drawLine(x - r / 2, y, x - r - step, y + r / 3);
        g.drawLine(x + r / 2, y, x + r + step, y + r / 3);
        Polygon ears = new Polygon(
                new int[] { x - r - 4, x - r / 2, x, x + r / 2, x + r + 4 },
                new int[] { y - 2, y - r / 2, y, y - r / 2, y - 2 }, 5);
        g.setColor(body.darker());
        g.fillPolygon(ears);
        g.setColor(body);
        g.fillOval(x - r, y - r, s, s);
        drawEyes(g, x, y - s / 8, Math.max(2, s / 8), new Color(255, 235, 90));
        g.setColor(body.darker().darker());
        g.drawLine(x - s / 5, y + s / 4, x + s / 5, y + s / 4);
        g.drawLine(x, y, x - 2, y + s / 7);
    }

    private static void drawSkeleton(Graphics2D g, Color bone, int x, int y, int s, double phase) {
        int rattle = (int) Math.round(Math.sin(phase * 2) * 2);
        x += rattle;
        int r = s / 2;
        g.setColor(bone.darker());
        int jawDrop = (int) Math.round((Math.sin(phase) + 1) * 1.5);
        g.fillRoundRect(x - r / 2, y + r / 3 + jawDrop, r, r, 3, 3);
        g.setColor(bone);
        g.fillOval(x - r, y - r, s, s - r / 3);
        g.setColor(new Color(45, 45, 50));
        g.fillOval(x - s / 3, y - s / 5, s / 4, s / 4);
        g.fillOval(x + s / 12, y - s / 5, s / 4, s / 4);
        Polygon nose = new Polygon(
                new int[] { x, x - 2, x + 2 },
                new int[] { y, y + 5, y + 5 }, 3);
        g.fillPolygon(nose);
        g.setColor(new Color(85, 75, 65));
        for (int i = -1; i <= 1; i++) {
            g.drawLine(x + i * 4, y + r / 2 + jawDrop, x + i * 4, y + r - 1 + jawDrop);
        }
    }

    private static void drawShadowMage(Graphics2D g, Color robe, int x, int y, int s, double phase) {
        y += (int) Math.round(Math.sin(phase) * 3);
        int r = s / 2;
        int cloakSway = (int) Math.round(Math.sin(phase * 0.8) * 3);
        Polygon cloak = new Polygon(
                new int[] { x, x - r, x - r + 2 + cloakSway, x + r - 2 + cloakSway, x + r },
                new int[] { y - r, y - r / 5, y + r, y + r, y - r / 5 }, 5);
        g.setColor(robe.darker());
        g.fillPolygon(cloak);
        g.setColor(robe);
        g.fillArc(x - r, y - r, s, s, 0, 180);
        g.setColor(new Color(25, 20, 40));
        g.fillOval(x - r / 2, y - r / 2, r, r);
        drawEyes(g, x, y - 1, Math.max(2, s / 9), new Color(100, 235, 255));
        g.setColor(new Color(180, 115, 235));
        g.drawLine(x - r + 3, y + r - 2, x + r - 3, y + r - 2);
    }

    private static void drawDemonKing(Graphics2D g, Color body, int x, int y, int s, double phase) {
        s += (int) Math.round((Math.sin(phase * 0.65) + 1) * 1.5);
        y += (int) Math.round(Math.sin(phase * 0.65));
        int r = s / 2;
        Polygon horns = new Polygon(
                new int[] { x - r, x - r - 7, x - r / 3, x + r / 3, x + r + 7, x + r },
                new int[] { y - r / 3, y - r - 6, y - r, y - r, y - r - 6, y - r / 3 }, 6);
        g.setColor(new Color(235, 205, 125));
        g.fillPolygon(horns);
        g.setColor(body);
        g.fillOval(x - r, y - r, s, s);
        g.setColor(body.darker());
        g.fillArc(x - r, y, s, r, 180, 180);
        drawEyes(g, x, y - s / 9, Math.max(3, s / 8), new Color(255, 220, 60));
        g.setColor(new Color(240, 200, 120));
        g.setStroke(new BasicStroke(2f));
        g.drawOval(x - r, y - r, s, s);
    }

    private static void drawGenericMonster(Graphics2D g, Color body, int x, int y, int s, double phase) {
        y += (int) Math.round(Math.sin(phase) * 2);
        int r = s / 2;
        Polygon monster = new Polygon(
                new int[] { x, x + r / 2, x + r, x + r / 2, x, x - r / 2, x - r, x - r / 2 },
                new int[] { y - r, y - r / 2, y, y + r / 2, y + r, y + r / 2, y, y - r / 2 }, 8);
        g.setColor(body);
        g.fillPolygon(monster);
        drawEyes(g, x, y, Math.max(2, s / 8), Color.YELLOW);
    }

    private static void drawEyes(Graphics2D g, int x, int y, int eyeSize, Color iris) {
        int gap = eyeSize + 2;
        g.setColor(Color.WHITE);
        g.fillOval(x - gap - eyeSize / 2, y - eyeSize / 2, eyeSize, eyeSize);
        g.fillOval(x + gap - eyeSize / 2, y - eyeSize / 2, eyeSize, eyeSize);
        g.setColor(iris);
        int pupil = Math.max(2, eyeSize / 2);
        g.fillOval(x - gap - pupil / 2, y - pupil / 2, pupil, pupil);
        g.fillOval(x + gap - pupil / 2, y - pupil / 2, pupil, pupil);
    }
}
