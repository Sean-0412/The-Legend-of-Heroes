package src;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.image.BufferedImage;

class BattleRenderer {
    private static final double BATTLE_PLAYER_SCALE = 1.0;

    private final GamePanel game;
    private final BattleManager battleManager;

    BattleRenderer(GamePanel game, BattleManager battleManager) {
        this.game = game;
        this.battleManager = battleManager;
    }

    void drawBattle(Graphics2D g2d) {
        int centerX = game.getWidth() / 2;
        int centerY = game.getHeight() / 2;
        int playerX = centerX - 120;
        int playerY = centerY - 100;
        int companionX = centerX - 120;
        int companionY = centerY - 20;
        int enemyX = centerX + 120;

        g2d.setColor(new Color(0, 0, 0, 200));
        g2d.fillRect(0, 0, game.getWidth(), game.getHeight());

        int offsetX = 0;
        int offsetY = 0;
        int moveDuration = 15;
        int waitDuration = 30;
        int returnDuration = 15;
        int targetOffsetX = 0;
        int targetOffsetY = 0;
        boolean healingSkillAnimation = battleManager.isHealingSkillAnimation();
        boolean moonSliceAnimation = game.animating
                && "playerAttack".equals(game.animAction)
                && game.selectedSkill instanceof SkillMoonSlice;
        if (game.animating) {
            if (healingSkillAnimation) {
                targetOffsetX = 0;
                targetOffsetY = 0;
            } else if (moonSliceAnimation) {
                if (game.currentActor instanceof Player) {
                    targetOffsetX = centerX - playerX;
                } else if (game.currentActor instanceof Companion) {
                    targetOffsetX = centerX - companionX;
                }
                targetOffsetY = 0;
            } else if (game.currentActor instanceof Player) {
                targetOffsetX = enemyX - playerX - 20;
            } else if (game.currentActor instanceof Companion) {
                targetOffsetX = enemyX - companionX - 20;
            } else if (game.currentActor instanceof Enemy) {
                int targetX = playerX;
                targetOffsetX = targetX - enemyX + 20;
            }

            if (!healingSkillAnimation && !moonSliceAnimation
                    && game.targetingEnemyIndex >= 0 && game.targetingEnemyIndex < game.currentEnemies.size()) {
                int targetEnemySlot = battleManager.getEnemyBattleSlot(game.targetingEnemyIndex);
                int targetEnemyY = centerY - 80 + targetEnemySlot * 50;
                if (game.currentActor instanceof Player) {
                    targetOffsetY = targetEnemyY - playerY;
                } else if (game.currentActor instanceof Companion) {
                    targetOffsetY = targetEnemyY - (companionY + (game.companions.indexOf(game.currentActor) * 50));
                }
            }

            if (game.currentActor instanceof Enemy && game.currentAttackingEnemyIndex >= 0) {
                int enemyStartY = centerY - 80;
                int enemyInitialY = enemyStartY + game.currentAttackingEnemyIndex * 50;
                int targetY = playerY;
                if (!game.enemyAttackTargetIsPlayer
                        && game.enemyAttackTargetCompanionIndex >= 0
                        && game.enemyAttackTargetCompanionIndex < game.companions.size()) {
                    targetY = companionY + game.enemyAttackTargetCompanionIndex * 50;
                }
                targetOffsetY = targetY - enemyInitialY;
            }

            if (game.animTicks < moveDuration) {
                double progress = (double) game.animTicks / moveDuration;
                offsetX = (int) (targetOffsetX * progress);
                offsetY = (int) (targetOffsetY * progress);
            } else if (game.animTicks < moveDuration + waitDuration) {
                offsetX = targetOffsetX;
                offsetY = targetOffsetY;
            } else {
                int returnTicks = game.animTicks - moveDuration - waitDuration;
                double progress = 1 - (double) returnTicks / returnDuration;
                offsetX = (int) (targetOffsetX * progress);
                offsetY = (int) (targetOffsetY * progress);
            }
        }

        int battleMoveDir = 0;
        if (game.animating
                && game.currentActor instanceof Player
                && !healingSkillAnimation
                && ("playerAttack".equals(game.animAction) || "companionAttack".equals(game.animAction))) {
            int targetDir = Integer.compare(targetOffsetX, 0);
            if (game.animTicks < moveDuration) {
                battleMoveDir = targetDir;
            } else if (game.animTicks >= moveDuration + waitDuration
                    && game.animTicks < moveDuration + waitDuration + returnDuration) {
                battleMoveDir = -targetDir;
            }
        }

        int actualPlayerX = playerX;
        int actualPlayerY = playerY;
        boolean isPlayerDown = game.player.hp <= 0;
        if (game.currentActor instanceof Player
                && !isPlayerDown
                && !healingSkillAnimation
                && ("playerAttack".equals(game.animAction) || "companionAttack".equals(game.animAction))) {
            actualPlayerX = playerX + offsetX;
            actualPlayerY = playerY + offsetY;
        }

        boolean isPlayerActive = game.currentActor instanceof Player;
        double playerHealGlow = game.playerHealGlowTicks / (double) game.HEAL_GLOW_DURATION;
        if (healingSkillAnimation && game.currentActor instanceof Player) {
            drawHealingAura(g2d, actualPlayerX, actualPlayerY);
        }
        if (isPlayerDown) {
            g2d.setColor(new Color(120, 120, 120));
            g2d.fillOval(actualPlayerX - 12, actualPlayerY - 12, 24, 24);
            g2d.setColor(new Color(220, 220, 220));
            g2d.setStroke(new BasicStroke(1.5f));
            g2d.drawLine(actualPlayerX - 10, actualPlayerY - 10, actualPlayerX + 10, actualPlayerY + 10);
            g2d.drawLine(actualPlayerX + 10, actualPlayerY - 10, actualPlayerX - 10, actualPlayerY + 10);
        } else {
            int basePlayerSpriteSize = 40;
            BufferedImage battleSprite = game.getBattlePlayerSprite(battleMoveDir);
            if (battleSprite == null) {
                battleSprite = game.battlePlayerSprite;
            }
            int spriteSize = (int) Math.round(game.getMapPlayerDrawSize(battleSprite, basePlayerSpriteSize)
                    * BATTLE_PLAYER_SCALE);
            int half = spriteSize / 2;
            if (battleSprite != null) {
                g2d.drawImage(battleSprite, actualPlayerX - half, actualPlayerY - half, spriteSize, spriteSize, null);

                if (playerHealGlow > 0) {
                    Composite oldComposite = g2d.getComposite();
                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
                            (float) Math.min(0.45, playerHealGlow * 0.45)));
                    g2d.setColor(Color.WHITE);
                    g2d.fillOval(actualPlayerX - half, actualPlayerY - half, spriteSize, spriteSize);
                    g2d.setComposite(oldComposite);
                }

                if (isPlayerActive) {
                    g2d.setColor(Color.YELLOW);
                    g2d.setStroke(new BasicStroke(3));
                    g2d.drawRoundRect(actualPlayerX - half - 2, actualPlayerY - half - 2, spriteSize + 4, spriteSize + 4, 8, 8);
                }
            } else if (isPlayerActive) {
                g2d.setColor(blendToWhite(new Color(100, 150, 255), playerHealGlow));
                g2d.fillOval(actualPlayerX - 15, actualPlayerY - 15, 30, 30);
                g2d.setColor(Color.YELLOW);
                g2d.setStroke(new BasicStroke(3));
                g2d.drawOval(actualPlayerX - 15, actualPlayerY - 15, 30, 30);
            } else {
                g2d.setColor(blendToWhite(Color.BLUE, playerHealGlow));
                g2d.fillOval(actualPlayerX - 12, actualPlayerY - 12, 24, 24);
            }
        }
        game.battleScreenPlayerX = actualPlayerX;
        game.battleScreenPlayerY = actualPlayerY;

        if (game.playerTakingDamage && game.playerDamageTicks > 0) {
            int barWidth = 50;
            int barHeight = 8;
            int barX = actualPlayerX - barWidth / 2;
            int barY = actualPlayerY - 30;

            g2d.setColor(Color.DARK_GRAY);
            g2d.fillRect(barX, barY, barWidth, barHeight);

            int hpWidth = (int) (barWidth * (game.player.hp / (double) game.player.maxHp));
            g2d.setColor(new Color(255, 165, 0));
            g2d.fillRect(barX, barY, hpWidth, barHeight);

            g2d.setColor(Color.WHITE);
            g2d.setStroke(new BasicStroke(1.5f));
            g2d.drawRect(barX, barY, barWidth, barHeight);
        }

        for (int i = 0; i < game.companions.size(); i++) {
            Companion c = game.companions.get(i);
            int cx = companionX;
            int cy = companionY + i * 50;
            boolean isCompanionDown = c.hp <= 0;
            double companionHealGlow = game.companionHealGlowTicks[i] / (double) game.HEAL_GLOW_DURATION;

            boolean isCompanionActive = game.currentActor == c;
            int displayCx = cx;
            int displayCy = cy;
            if (isCompanionActive && !isCompanionDown
                    && ("playerAttack".equals(game.animAction) || "companionAttack".equals(game.animAction))) {
                displayCx = cx + offsetX;
                displayCy = cy + offsetY;
            }

            if (isCompanionDown) {
                g2d.setColor(new Color(120, 120, 120));
                g2d.fillOval(displayCx - 12, displayCy - 12, 24, 24);
                g2d.setColor(new Color(220, 220, 220));
                g2d.setStroke(new BasicStroke(1.5f));
                g2d.drawLine(displayCx - 10, displayCy - 10, displayCx + 10, displayCy + 10);
                g2d.drawLine(displayCx + 10, displayCy - 10, displayCx - 10, displayCy + 10);
            } else if (isCompanionActive) {
                if (healingSkillAnimation) {
                    drawHealingAura(g2d, displayCx, displayCy);
                }
                g2d.setColor(blendToWhite(new Color(100, 220, 100), companionHealGlow));
                g2d.fillOval(displayCx - 15, displayCy - 15, 30, 30);
                g2d.setColor(Color.YELLOW);
                g2d.setStroke(new BasicStroke(3));
                g2d.drawOval(displayCx - 15, displayCy - 15, 30, 30);
            } else {
                g2d.setColor(blendToWhite(new Color(100, 200, 100), companionHealGlow));
                g2d.fillOval(displayCx - 12, displayCy - 12, 24, 24);
            }
            game.battleScreenCompanionX[i] = displayCx;
            game.battleScreenCompanionY[i] = displayCy;

            if (game.companionTakingDamage[i] > 0) {
                int barWidth = 50;
                int barHeight = 8;
                int barX = displayCx - barWidth / 2;
                int barY = displayCy - 30;

                g2d.setColor(Color.DARK_GRAY);
                g2d.fillRect(barX, barY, barWidth, barHeight);

                int hpWidth = (int) (barWidth * (c.hp / (double) c.maxHp));
                g2d.setColor(new Color(255, 165, 0));
                g2d.fillRect(barX, barY, hpWidth, barHeight);

                g2d.setColor(Color.WHITE);
                g2d.setStroke(new BasicStroke(1.5f));
                g2d.drawRect(barX, barY, barWidth, barHeight);
            }
        }

        int enemyStartX = centerX + 120;
        int enemyStartY = centerY - 80;
        int enemySpacingY = 50;

        for (int i = 0; i < game.currentEnemies.size(); i++) {
            Enemy enemy = game.currentEnemies.get(i);
            int enemySlot = battleManager.getEnemyBattleSlot(i);
            int actualEnemyX = enemyStartX;
            int actualEnemyY = enemyStartY + enemySlot * enemySpacingY;

            if ("enemyAttack".equals(game.animAction) && game.currentActionIndex < game.battleOrder.size()) {
                BattleUnit currentUnit = game.battleOrder.get(game.currentActionIndex);
                if (currentUnit.unit instanceof Enemy && currentUnit.unit == enemy) {
                    actualEnemyX = enemyStartX + offsetX;
                    actualEnemyY = enemyStartY + enemySlot * enemySpacingY + offsetY;
                }
            }

            g2d.setColor(Color.RED);
            g2d.fillOval(actualEnemyX - 12, actualEnemyY - 12, 24, 24);

            if (moonSliceAnimation) {
                drawMoonSliceSlash(g2d, actualEnemyX, actualEnemyY);
            }

            game.battleScreenEnemyX[i] = actualEnemyX;
            game.battleScreenEnemyY[i] = actualEnemyY;

            if (game.hoveredEnemyIndex == i) {
                int barWidth = 50;
                int barHeight = 8;
                int barX = actualEnemyX - barWidth / 2;
                int barY = actualEnemyY - 25;

                g2d.setColor(Color.DARK_GRAY);
                g2d.fillRect(barX, barY, barWidth, barHeight);

                int hpWidth = (int) (barWidth * (enemy.hp / (double) enemy.maxHp));
                g2d.setColor(new Color(255, 165, 0));
                g2d.fillRect(barX, barY, hpWidth, barHeight);

                g2d.setColor(Color.WHITE);
                g2d.setStroke(new BasicStroke(1.5f));
                g2d.drawRect(barX, barY, barWidth, barHeight);
            }

            if (game.damagedEnemyIndex == i && game.damagedEnemyTicks > 0) {
                int barWidth = 50;
                int barHeight = 8;
                int barX = actualEnemyX - barWidth / 2;
                int barY = actualEnemyY - 25;

                g2d.setColor(Color.DARK_GRAY);
                g2d.fillRect(barX, barY, barWidth, barHeight);

                int hpWidth = (int) (barWidth * (enemy.hp / (double) enemy.maxHp));
                g2d.setColor(new Color(255, 165, 0));
                g2d.fillRect(barX, barY, hpWidth, barHeight);

                g2d.setColor(Color.WHITE);
                g2d.setStroke(new BasicStroke(1.5f));
                g2d.drawRect(barX, barY, barWidth, barHeight);
            }
        }

        drawBattleOrderBar(g2d);

        if (game.hoveredEnemyIndex >= 0 && game.hoveredEnemyIndex < game.currentEnemies.size()) {
            Enemy enemy = game.currentEnemies.get(game.hoveredEnemyIndex);

            int infoPanelX = 20;
            int infoPanelY = 20;
            int infoPanelW = 220;
            int infoPanelH = 55;

            g2d.setColor(new Color(100, 0, 0, 200));
            g2d.fillRect(infoPanelX, infoPanelY, infoPanelW, infoPanelH);

            g2d.setColor(new Color(200, 0, 0));
            g2d.setStroke(new BasicStroke(2));
            g2d.drawRect(infoPanelX, infoPanelY, infoPanelW, infoPanelH);

            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Microsoft JhengHei", Font.BOLD, 13));
            g2d.drawString("敵人" + (game.hoveredEnemyIndex + 1), infoPanelX + 10, infoPanelY + 18);

            g2d.setColor(new Color(200, 200, 100));
            g2d.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 11));
            g2d.drawString("Lv." + 5, infoPanelX + 10, infoPanelY + 35);

            g2d.setColor(new Color(255, 100, 100));
            g2d.setFont(new Font("Microsoft JhengHei", Font.BOLD, 12));
            String hpText = "HP: " + enemy.hp + "/" + enemy.maxHp;
            g2d.drawString(hpText, infoPanelX + 100, infoPanelY + 35);
        }

        int panelY = game.getHeight() - 180;
        int panelH = 160;

        g2d.setColor(new Color(20, 40, 80, 200));
        g2d.fillRect(0, panelY, game.getWidth(), panelH);
        g2d.setColor(new Color(100, 150, 200));
        g2d.setStroke(new BasicStroke(3));
        g2d.drawRect(0, panelY, game.getWidth(), panelH);

        int characterCount = 1 + game.companions.size();
        int charPanelW = (game.getWidth() - 20) / characterCount;
        int charPanelH = 125;
        int charPanelY = panelY + 10;

        drawCharacterPanel(g2d, 10, charPanelY, charPanelW - 10, charPanelH,
                game.player.name != null ? game.player.name : "玩家", game.player.hp, game.player.maxHp,
                game.player.mana, game.player.maxMana, game.player.cp, game.player.maxCp);

        for (int i = 0; i < game.companions.size(); i++) {
            Companion c = game.companions.get(i);
            int x = 10 + (i + 1) * charPanelW;
            drawCharacterPanel(g2d, x, charPanelY, charPanelW - 10, charPanelH, c.name, c.hp, c.maxHp,
                    c.mana, c.maxMana, c.cp, c.maxCp);
        }

        int btnAreaY = panelY + 130;
        int btnW = 85, btnH = 35, gap = 8;
        int startX = (game.getWidth() - (btnW * 4 + gap * 3)) / 2;

        game.attackRect = new Rectangle(startX, btnAreaY, btnW, btnH);
        game.skillRect = new Rectangle(startX + btnW + gap, btnAreaY, btnW, btnH);
        game.healRect = new Rectangle(startX + (btnW + gap) * 2, btnAreaY, btnW, btnH);
        game.runRect = new Rectangle(startX + (btnW + gap) * 3, btnAreaY, btnW, btnH);

        g2d.setColor(new Color(60, 60, 100));
        g2d.fill(game.attackRect);
        g2d.fill(game.skillRect);
        g2d.fill(game.healRect);
        g2d.fill(game.runRect);

        g2d.setColor(new Color(150, 150, 200));
        g2d.setStroke(new BasicStroke(2));
        g2d.draw(game.attackRect);
        g2d.draw(game.skillRect);
        g2d.draw(game.healRect);
        g2d.draw(game.runRect);

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Microsoft JhengHei", Font.BOLD, 13));
        FontMetrics fm = g2d.getFontMetrics();

        String[] btnTexts = { "攻擊", "戰技", "用藥", "逃跑" };
        Rectangle[] btnRects = { game.attackRect, game.skillRect, game.healRect, game.runRect };
        for (int i = 0; i < btnTexts.length; i++) {
            String text = btnTexts[i];
            Rectangle rect = btnRects[i];
            int tX = rect.x + (rect.width - fm.stringWidth(text)) / 2;
            int tY = rect.y + ((rect.height - fm.getHeight()) / 2) + fm.getAscent();
            g2d.drawString(text, tX, tY);
        }

        if (!game.previewDrops.isEmpty()) {
            g2d.setColor(new Color(200, 150, 100));
            g2d.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 12));
            FontMetrics fmDrops = g2d.getFontMetrics();
            int textWidth = fmDrops.stringWidth("掉落: " + game.previewDrops);
            g2d.drawString("掉落: " + game.previewDrops, game.getWidth() - textWidth - 20, 35);
        }

        if (!game.selectingTargetMode.isEmpty()) {
            int msgCenterX = game.getWidth() / 2;
            int msgCenterY = 80;

            g2d.setColor(new Color(255, 200, 0));
            g2d.setStroke(new BasicStroke(3));
            g2d.drawRect(msgCenterX - 180, msgCenterY - 20, 360, 40);

            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Microsoft JhengHei", Font.BOLD, 14));
            FontMetrics fmTarget = g2d.getFontMetrics();

            String targetMsg = "";
            if ("attack".equals(game.selectingTargetMode)) {
                targetMsg = "點擊要攻擊的敵人  (右鍵取消)";
            } else if ("skill".equals(game.selectingTargetMode)) {
                targetMsg = "點擊目標  (右鍵取消)";
            } else if ("potion".equals(game.selectingTargetMode)) {
                targetMsg = "點擊要治療的目標  (右鍵取消)";
            }

            int textX = msgCenterX - fmTarget.stringWidth(targetMsg) / 2;
            g2d.drawString(targetMsg, textX, msgCenterY + 5);
        }

        if (game.showingFleeMessage && !game.fleeMessage.isEmpty()) {
            int msgCenterX = game.getWidth() / 2;
            int msgCenterY = game.getHeight() / 2;

            g2d.setColor(new Color(0, 0, 0, 150));
            g2d.fillRect(0, 0, game.getWidth(), game.getHeight());

            int boxWidth = 300;
            int boxHeight = 120;
            int boxX = msgCenterX - boxWidth / 2;
            int boxY = msgCenterY - boxHeight / 2;

            g2d.setColor(new Color(50, 50, 100, 200));
            g2d.fillRect(boxX, boxY, boxWidth, boxHeight);
            g2d.setColor(new Color(100, 150, 255));
            g2d.setStroke(new BasicStroke(2));
            g2d.drawRect(boxX, boxY, boxWidth, boxHeight);

            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Microsoft JhengHei", Font.BOLD, 16));
            FontMetrics fmMsg = g2d.getFontMetrics();
            int textX = msgCenterX - fmMsg.stringWidth(game.fleeMessage) / 2;
            g2d.drawString(game.fleeMessage, textX, msgCenterY - 20);

            g2d.setColor(Color.YELLOW);
            g2d.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 12));
            g2d.drawString("按任意鍵繼續...", msgCenterX - 60, msgCenterY + 30);
        }
    }

    void drawSettlement(Graphics2D g2d) {
        g2d.setColor(new Color(0, 0, 0, 200));
        g2d.fillRect(0, 0, game.getWidth(), game.getHeight());

        Font chFont = new Font("Microsoft JhengHei", Font.BOLD, 24);
        if (!chFont.canDisplay('漢')) {
            chFont = new Font(Font.DIALOG, Font.BOLD, 24);
        }

        int centerX = game.getWidth() / 2;
        int centerY = game.getHeight() / 2;

        g2d.setFont(chFont);
        g2d.setColor(Color.YELLOW);
        if (game.isZeroExpSettlement) {
            g2d.drawString("敵人逃脫", centerX - 60, centerY - 200);
            g2d.setColor(Color.CYAN);
            g2d.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 18));
            g2d.drawString("敵人趁其不備逃脫了...", centerX - 120, centerY - 150);
        } else {
            g2d.drawString("戰鬥結束", centerX - 60, centerY - 200);

            g2d.setColor(Color.CYAN);
            g2d.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 18));
            g2d.drawString("獲得經驗值: " + game.lastBattleExp, centerX - 80, centerY - 140);

            int charSpacing = 200;
            int charX1 = centerX - charSpacing;
            int charX2 = centerX + charSpacing;
            int charInfoY = centerY - 50;

            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Microsoft JhengHei", Font.BOLD, 20));
            g2d.drawString(game.player.name, charX1 - 30, charInfoY);

            g2d.setColor(new Color(150, 200, 255));
            g2d.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 16));
            String playerLevelStr = "等級: " + game.player.level;
            if (game.levelsGained > 0) {
                playerLevelStr += " (+" + game.levelsGained + ")";
            }
            g2d.drawString(playerLevelStr, charX1 - 40, charInfoY + 40);

            g2d.setColor(new Color(100, 200, 255));
            g2d.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 14));
            g2d.drawString("Next level: " + game.expForNextLevel, charX1 - 60, charInfoY + 70);

            if (game.companions.size() > 0) {
                Companion c = game.companions.get(0);
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Microsoft JhengHei", Font.BOLD, 20));
                g2d.drawString(c.name, charX2 - 30, charInfoY);

                g2d.setColor(new Color(150, 200, 100));
                g2d.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 16));
                String companionLevelStr = "等級: " + c.level;
                if (game.companionLevelsGained[0] > 0) {
                    companionLevelStr += " (+" + game.companionLevelsGained[0] + ")";
                }
                g2d.drawString(companionLevelStr, charX2 - 40, charInfoY + 40);

                g2d.setColor(new Color(100, 180, 100));
                g2d.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 14));
                g2d.drawString("Next level: " + game.companionExpForNextLevel[0], charX2 - 60, charInfoY + 70);
            }
        }

        if (!game.settlementDrops.isEmpty()) {
            g2d.setColor(new Color(200, 100, 200));
            g2d.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 16));
            g2d.drawString(game.settlementDrops, centerX - 100, centerY + 150);
        }

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 14));
        g2d.drawString("按任意鍵或點擊畫面以繼續...", centerX - 120, game.getHeight() - 50);
    }

    private void drawBattleOrderBar(Graphics2D g2d) {
        int barX = 20;
        int barY = 100;
        int unitWidth = 40;
        int unitHeight = 30;
        int gap = 5;
        int maxDisplay = 8;

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Microsoft JhengHei", Font.BOLD, 12));
        g2d.drawString("行動順序:", barX, barY - 5);

        if (game.battleOrder.isEmpty()) {
            return;
        }

        java.util.List<Integer> visibleOrderIndices = battleManager.buildVisibleBattleOrderIndices();

        if (visibleOrderIndices.isEmpty()) {
            return;
        }

        java.util.List<Integer> displayOrderIndices = visibleOrderIndices;

        int displayCount = Math.min(displayOrderIndices.size(), maxDisplay);

        for (int i = 0; i < displayCount; i++) {
            int orderIndex = displayOrderIndices.get(i);
            BattleUnit unit = game.battleOrder.get(orderIndex);
            int x = barX + i * (unitWidth + gap);
            int y = barY;

            Color fillColor;
            if (unit.unit instanceof Player) {
                fillColor = new Color(0, 0, 255, 150);
            } else if (unit.unit instanceof Companion) {
                fillColor = new Color(0, 255, 0, 150);
            } else if (unit.unit instanceof Enemy) {
                fillColor = new Color(255, 0, 0, 150);
            } else {
                fillColor = new Color(100, 100, 100, 100);
            }

            if (i == 0) {
                int r = Math.min(255, fillColor.getRed() + 80);
                int g_val = Math.min(255, fillColor.getGreen() + 80);
                int b = Math.min(255, fillColor.getBlue() + 80);
                fillColor = new Color(r, g_val, b, 255);
            }

            g2d.setColor(fillColor);
            g2d.fillRect(x, y, unitWidth, unitHeight);

            if (i == 0) {
                g2d.setColor(new Color(255, 200, 0));
                g2d.setStroke(new BasicStroke(3));
            } else {
                g2d.setColor(new Color(100, 100, 100));
                g2d.setStroke(new BasicStroke(1));
            }
            g2d.drawRect(x, y, unitWidth, unitHeight);

            g2d.setStroke(new BasicStroke(1));
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Microsoft JhengHei", Font.BOLD, 12));
            String label = "?";
            if (unit.unit instanceof Player) {
                label = "勇者";
            } else if (unit.unit instanceof Companion) {
                label = ((Companion) unit.unit).name;
            } else if (unit.unit instanceof Enemy) {
                label = "敵人";
            }
            FontMetrics fm = g2d.getFontMetrics();
            int textX = x + (unitWidth - fm.stringWidth(label)) / 2;
            int textY = y + unitHeight / 2 + fm.getAscent() / 2;
            g2d.drawString(label, textX, textY);
        }

        if (battleManager.isBattleOrderPreviewActive()) {
            int previewIndex = battleManager.getProjectedBattleOrderIndex(game.currentActor, battleManager.getPendingActionCostForPreview());
            if (previewIndex >= 0 && previewIndex < maxDisplay) {
                BattleUnit currentActorUnit = battleManager.findBattleUnit(game.currentActor);
                if (currentActorUnit != null && battleManager.isBattleUnitVisible(currentActorUnit)) {
                    int previewX = barX + previewIndex * (unitWidth + gap);
                    int previewY = barY - unitHeight - 8;

                    Color previewColor;
                    String previewLabel;
                    if (currentActorUnit.unit instanceof Player) {
                        previewColor = new Color(0, 0, 255, 110);
                        previewLabel = "勇者";
                    } else if (currentActorUnit.unit instanceof Enemy) {
                        previewColor = new Color(255, 0, 0, 110);
                        previewLabel = "敵人";
                    } else {
                        previewColor = new Color(0, 255, 0, 110);
                        previewLabel = ((Companion) currentActorUnit.unit).name;
                    }

                    g2d.setColor(previewColor);
                    g2d.fillRect(previewX, previewY, unitWidth, unitHeight);
                    g2d.setColor(new Color(255, 215, 120));
                    g2d.setStroke(new BasicStroke(2));
                    g2d.drawRect(previewX, previewY, unitWidth, unitHeight);

                    g2d.setStroke(new BasicStroke(1));
                    g2d.setColor(Color.WHITE);
                    g2d.setFont(new Font("Microsoft JhengHei", Font.BOLD, 12));
                    FontMetrics previewFm = g2d.getFontMetrics();
                    int previewTextX = previewX + (unitWidth - previewFm.stringWidth(previewLabel)) / 2;
                    int previewTextY = previewY + unitHeight / 2 + previewFm.getAscent() / 2;
                    g2d.drawString(previewLabel, previewTextX, previewTextY);
                }
            }
        }
    }

    private void drawCharacterPanel(Graphics2D g2d, int x, int y, int w, int h, String name, int hp, int maxHp,
            int mana, int maxMana, int cp, int maxCp) {
        g2d.setColor(new Color(50, 50, 80, 150));
        g2d.fillRect(x, y, w, h);
        g2d.setColor(new Color(100, 200, 100));
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRect(x, y, w, h);

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Microsoft JhengHei", Font.BOLD, 12));
        g2d.drawString(name, x + 10, y + 18);

        int barX = x + 10;
        int barY = y + 25;
        int barW = w - 20;
        int barH = 12;

        g2d.setColor(new Color(80, 80, 80));
        g2d.fillRect(barX, barY, barW, barH);
        int hpWidth = (int) (barW * (hp / (double) maxHp));
        g2d.setColor(new Color(255, 165, 0));
        g2d.fillRect(barX, barY, hpWidth, barH);
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.setStroke(new BasicStroke(1));
        g2d.drawRect(barX, barY, barW, barH);

        g2d.setColor(new Color(200, 200, 200));
        g2d.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 9));
        g2d.drawString("HP", x + 10, barY - 2);
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 10));
        g2d.drawString(hp + "/" + maxHp, barX + 3, barY + 10);

        int barY2 = barY + 16;
        g2d.setColor(new Color(80, 80, 80));
        g2d.fillRect(barX, barY2, barW, barH);
        int manaWidth = (int) (barW * (mana / (double) maxMana));
        g2d.setColor(new Color(100, 150, 255));
        g2d.fillRect(barX, barY2, manaWidth, barH);
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.drawRect(barX, barY2, barW, barH);

        g2d.setColor(new Color(200, 200, 200));
        g2d.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 9));
        g2d.drawString("MP", x + 10, barY2 - 2);
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 10));
        g2d.drawString(mana + "/" + maxMana, barX + 3, barY2 + 10);

        int barY3 = barY2 + 16;
        g2d.setColor(new Color(80, 80, 80));
        g2d.fillRect(barX, barY3, barW, barH);
        int cpWidth = (int) (barW * (cp / (double) maxCp));
        g2d.setColor(new Color(100, 200, 100));
        g2d.fillRect(barX, barY3, cpWidth, barH);
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.drawRect(barX, barY3, barW, barH);

        g2d.setColor(new Color(200, 200, 200));
        g2d.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 9));
        g2d.drawString("CP", x + 10, barY3 - 2);
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 10));
        g2d.drawString(cp + "/" + maxCp, barX + 3, barY3 + 10);
    }

    private Color blendToWhite(Color base, double ratio) {
        double t = Math.max(0.0, Math.min(1.0, ratio));
        int r = (int) Math.round(base.getRed() + (255 - base.getRed()) * t);
        int g = (int) Math.round(base.getGreen() + (255 - base.getGreen()) * t);
        int b = (int) Math.round(base.getBlue() + (255 - base.getBlue()) * t);
        return new Color(r, g, b);
    }

    private void drawHealingAura(Graphics2D g2d, int centerX, int centerY) {
        double progress = Math.max(0.0, Math.min(1.0, game.animTicks / (double) game.ANIM_DURATION));
        int outerRadius = 26 + (int) Math.round(progress * 22);
        int innerRadius = 16 + (int) Math.round(progress * 14);
        int alphaOuter = (int) Math.round(160 * (1.0 - progress));
        int alphaInner = (int) Math.round(220 * (1.0 - progress * 0.7));

        Composite oldComposite = g2d.getComposite();
        Stroke oldStroke = g2d.getStroke();

        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.max(0f, Math.min(1f, alphaOuter / 255f))));
        g2d.setColor(new Color(180, 240, 255));
        g2d.setStroke(new BasicStroke(3f));
        g2d.drawOval(centerX - outerRadius, centerY - outerRadius, outerRadius * 2, outerRadius * 2);

        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.max(0f, Math.min(1f, alphaInner / 255f))));
        g2d.setColor(new Color(230, 255, 255));
        g2d.setStroke(new BasicStroke(2f));
        g2d.drawOval(centerX - innerRadius, centerY - innerRadius, innerRadius * 2, innerRadius * 2);

        g2d.setComposite(oldComposite);
        g2d.setStroke(oldStroke);
    }

    private void drawMoonSliceSlash(Graphics2D g2d, int centerX, int centerY) {
        double progress = Math.max(0.0, Math.min(1.0, game.animTicks / (double) game.ANIM_DURATION));
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

        g2d.setColor(new Color(255, 255, 255));
        g2d.setStroke(new BasicStroke(2f));
        g2d.drawLine(centerX - 20 + sweep, centerY + 2, centerX + 2 + sweep, centerY - 20);

        g2d.setColor(new Color(180, 220, 255));
        g2d.setStroke(new BasicStroke(2f));
        g2d.drawLine(centerX - 14 + sweep, centerY + 16, centerX + 14 + sweep, centerY - 12);

        g2d.setComposite(oldComposite);
        g2d.setStroke(oldStroke);
    }
}
