/**
 * BattleEffectRenderer.java
 * 說明：提供戰鬥技能的純繪圖效果，目前包含月光治療與月影斬特效。
 */

package src;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;

final class BattleEffectRenderer {
    private BattleEffectRenderer() {
    }

    static void drawHealingAura(Graphics2D g2d, int centerX, int centerY, double progress) {
        progress = Math.max(0.0, Math.min(1.0, progress));
        double intensity = Math.sin(Math.PI * progress);
        int outerRadius = 24 + (int) Math.round(progress * 25);
        int innerRadius = 15 + (int) Math.round(progress * 16);

        Composite oldComposite = g2d.getComposite();
        Stroke oldStroke = g2d.getStroke();
        RenderingHints oldHints = g2d.getRenderingHints();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        float moonAlpha = (float) Math.min(0.9, 0.25 + intensity * 0.65);
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, moonAlpha));
        g2d.setColor(new Color(235, 248, 255));
        g2d.fillOval(centerX - 13, centerY - 78, 26, 26);
        g2d.setColor(new Color(75, 105, 150));
        g2d.fillOval(centerX - 5, centerY - 82, 24, 24);

        float beamAlpha = (float) (0.08 + intensity * 0.20);
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, beamAlpha));
        g2d.setColor(new Color(175, 225, 255));
        g2d.fillRoundRect(centerX - 20, centerY - 58, 40, 88, 28, 28);

        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
                (float) Math.min(0.75, 0.18 + intensity * 0.57)));
        g2d.setColor(new Color(180, 240, 255));
        g2d.setStroke(new BasicStroke(3f));
        g2d.drawOval(centerX - outerRadius, centerY - outerRadius, outerRadius * 2, outerRadius * 2);

        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
                (float) Math.min(0.95, 0.35 + intensity * 0.60)));
        g2d.setColor(new Color(230, 255, 255));
        g2d.setStroke(new BasicStroke(2f));
        g2d.drawOval(centerX - innerRadius, centerY - innerRadius, innerRadius * 2, innerRadius * 2);

        for (int i = 0; i < 8; i++) {
            double moteProgress = (progress * 1.7 + i * 0.137) % 1.0;
            int moteX = centerX + (int) Math.round(Math.sin(i * 2.4 + progress * 5.0) * (12 + i % 3 * 5));
            int moteY = centerY + 22 - (int) Math.round(moteProgress * 82);
            int moteSize = 2 + (i % 3);
            float moteAlpha = (float) (0.25 + 0.65 * Math.sin(Math.PI * moteProgress));
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, moteAlpha));
            g2d.setColor(i % 2 == 0 ? new Color(210, 245, 255) : Color.WHITE);
            g2d.fillOval(moteX - moteSize / 2, moteY - moteSize / 2, moteSize, moteSize);
        }

        g2d.setComposite(oldComposite);
        g2d.setStroke(oldStroke);
        g2d.setRenderingHints(oldHints);
    }

    static void drawMoonSliceSlash(Graphics2D g2d, int centerX, int centerY, double progress) {
        progress = Math.max(0.0, Math.min(1.0, progress));
        if (progress < 0.15 || progress > 0.75) {
            return;
        }

        float alpha = (float) Math.max(0.0, 1.0 - Math.abs(progress - 0.45) / 0.30);
        int sweep = (int) Math.round((progress - 0.15) / 0.60 * 20);
        Composite oldComposite = g2d.getComposite();
        Stroke oldStroke = g2d.getStroke();
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.max(0f, Math.min(1f, alpha))));

        g2d.setColor(new Color(220, 245, 255));
        g2d.setStroke(new BasicStroke(3f));
        g2d.drawLine(centerX - 18 + sweep, centerY + 10, centerX + 10 + sweep, centerY - 18);
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(2f));
        g2d.drawLine(centerX - 20 + sweep, centerY + 2, centerX + 2 + sweep, centerY - 20);
        g2d.setColor(new Color(180, 220, 255));
        g2d.drawLine(centerX - 14 + sweep, centerY + 16, centerX + 14 + sweep, centerY - 12);

        g2d.setComposite(oldComposite);
        g2d.setStroke(oldStroke);
    }
}
