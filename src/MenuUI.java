package src;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.swing.JOptionPane;

class MenuUI {
    private final GamePanel game;

    MenuUI(GamePanel game) {
        this.game = game;
    }

    void drawMainMenu(Graphics2D g2d) {
        int width = game.getWidth();
        int height = game.getHeight();

        g2d.setColor(new Color(10, 10, 30));
        g2d.fillRect(0, 0, width, height);

        g2d.setFont(new Font("Microsoft JhengHei", Font.BOLD, 60));
        g2d.setColor(new Color(100, 200, 255));
        String title = "RPG 冒險";
        FontMetrics fm = g2d.getFontMetrics();
        int titleX = (width - fm.stringWidth(title)) / 2;
        g2d.drawString(title, titleX, height / 3);

        int optionY = height / 2 + 50;
        int optionSpacing = 80;

        int newGameX = width / 2 - 150;
        int newGameY = optionY;
        game.newGameRect = new Rectangle(newGameX, newGameY, 120, 50);

        int loadGameX = width / 2 + 30;
        int loadGameY = optionY;
        game.loadGameRect = new Rectangle(loadGameX, loadGameY, 120, 50);

        int exitX = width / 2 - 60;
        int exitY = optionY + optionSpacing;
        game.exitRect = new Rectangle(exitX, exitY, 120, 50);

        drawMenuButton(g2d, game.newGameRect, "NEW", game.selectedMainMenuOption == 0);
        drawMenuButton(g2d, game.loadGameRect, "LOAD", game.selectedMainMenuOption == 1);
        drawMenuButton(g2d, game.exitRect, "EXIT", game.selectedMainMenuOption == 2);

        g2d.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 14));
        g2d.setColor(new Color(200, 200, 200));
        g2d.drawString("← → 方向鍵選擇   ENTER 確認", width / 2 - 120, height - 50);
    }

    void drawLoadMenu(Graphics2D g2d) {
        int width = game.getWidth();
        int height = game.getHeight();

        g2d.setColor(new Color(0, 0, 0, 150));
        g2d.fillRect(0, 0, width, height);

        int menuW = 300;
        int menuH = 250;
        int menuX = (width - menuW) / 2;
        int menuY = (height - menuH) / 2;

        g2d.setColor(new Color(50, 50, 100, 250));
        g2d.fillRoundRect(menuX, menuY, menuW, menuH, 10, 10);
        g2d.setColor(new Color(100, 150, 255));
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRoundRect(menuX, menuY, menuW, menuH, 10, 10);

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Microsoft JhengHei", Font.BOLD, 16));
        g2d.drawString("選擇加載槽位", menuX + 80, menuY + 30);

        for (int i = 0; i < game.MAX_SAVE_SLOTS; i++) {
            int slotX = menuX + 20;
            int slotY = menuY + 50 + i * 50;
            int slotW = menuW - 40;
            int slotH = 40;

            g2d.setColor(i == game.selectedLoadSlot ? new Color(255, 200, 0) : new Color(100, 100, 100));
            g2d.fillRoundRect(slotX, slotY, slotW, slotH, 5, 5);
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 13));

            File saveFile = getSaveFile(i);
            if (saveFile.exists()) {
                g2d.drawString("槽位 " + (i + 1) + " ✓", slotX + 10, slotY + 27);
            } else {
                g2d.drawString("槽位 " + (i + 1) + " (空)", slotX + 10, slotY + 27);
            }
        }

        g2d.setColor(Color.YELLOW);
        g2d.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 11));
        g2d.drawString("↑↓ 選擇  ENTER 確認  ESC 返回", menuX + 15, menuY + menuH - 15);
    }

    void drawMapMenu(Graphics2D g2d) {
        int panelW = Math.min(game.getWidth() - 40, 680);
        int panelH = Math.min(game.getHeight() - 60, 460);
        int panelX = 20;
        int panelY = 20;
        game.mapMenuPanelRect = new Rectangle(panelX, panelY, panelW, panelH);

        GradientPaint panelGrad = new GradientPaint(panelX, panelY, new Color(20, 26, 48, 235),
                panelX, panelY + panelH, new Color(8, 12, 24, 235));
        g2d.setPaint(panelGrad);
        g2d.fillRoundRect(panelX, panelY, panelW, panelH, 12, 12);
        g2d.setColor(new Color(110, 150, 220));
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRoundRect(panelX, panelY, panelW, panelH, 12, 12);

        int tabY = panelY + 10;
        int tabW = 140;
        int tabH = 38;
        int tabGap = 10;
        game.statusTabRect = new Rectangle(panelX + 14, tabY, tabW, tabH);
        game.bagTabRect = new Rectangle(game.statusTabRect.x + tabW + tabGap, tabY, tabW, tabH);
        game.saveTabRect = new Rectangle(game.bagTabRect.x + tabW + tabGap, tabY, tabW, tabH);

        Rectangle[] tabs = { game.statusTabRect, game.bagTabRect, game.saveTabRect };
        String[] tabTitles = { "狀態", "背包", "保存" };
        for (int i = 0; i < tabs.length; i++) {
            Rectangle r = tabs[i];
            boolean active = (game.selectedMapTab == i);
            boolean hover = (game.hoveredMapTab == i);
            GradientPaint tabGrad;
            if (active) {
                tabGrad = new GradientPaint(r.x, r.y, new Color(100, 140, 230), r.x, r.y + r.height,
                        new Color(50, 85, 185));
            } else if (hover) {
                tabGrad = new GradientPaint(r.x, r.y, new Color(72, 105, 170), r.x, r.y + r.height,
                        new Color(38, 62, 118));
            } else {
                tabGrad = new GradientPaint(r.x, r.y, new Color(44, 62, 102), r.x, r.y + r.height,
                        new Color(25, 40, 75));
            }
            g2d.setPaint(tabGrad);
            g2d.fillRoundRect(r.x, r.y, r.width, r.height, 8, 8);
            g2d.setColor(active ? new Color(220, 230, 255) : new Color(120, 140, 180));
            g2d.setStroke(new BasicStroke(active ? 2.5f : 1.5f));
            g2d.drawRoundRect(r.x, r.y, r.width, r.height, 8, 8);
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Microsoft JhengHei", Font.BOLD, 14));
            FontMetrics tfm = g2d.getFontMetrics();
            int tx = r.x + (r.width - tfm.stringWidth(tabTitles[i])) / 2;
            int ty = r.y + ((r.height - tfm.getHeight()) / 2) + tfm.getAscent();
            g2d.drawString(tabTitles[i], tx, ty);
        }

        int contentX = panelX + 14;
        int contentY = tabY + tabH + 10;
        int contentW = panelW - 28;
        int contentH = panelH - (contentY - panelY) - 12;
        g2d.setPaint(new GradientPaint(contentX, contentY, new Color(26, 35, 62, 220), contentX,
                contentY + contentH, new Color(15, 22, 40, 220)));
        g2d.fillRoundRect(contentX, contentY, contentW, contentH, 10, 10);
        g2d.setColor(new Color(90, 120, 180));
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawRoundRect(contentX, contentY, contentW, contentH, 10, 10);

        if (game.selectedMapTab == 0) {
            int selectorX = contentX + 14;
            int selectorY = contentY + 12;
            int selectorW = 96;
            int selectorH = 28;
            int selectorGap = 8;

            game.statusPlayerSelectorRect = new Rectangle(selectorX, selectorY, selectorW, selectorH);
            boolean playerSelected = (game.selectedStatusActor == -1);
            g2d.setPaint(new GradientPaint(selectorX, selectorY,
                    playerSelected ? new Color(106, 148, 236) : new Color(58, 77, 124),
                    selectorX, selectorY + selectorH,
                    playerSelected ? new Color(58, 95, 194) : new Color(34, 51, 86)));
            g2d.fillRoundRect(selectorX, selectorY, selectorW, selectorH, 7, 7);
            g2d.setColor(new Color(210, 225, 255));
            g2d.drawRoundRect(selectorX, selectorY, selectorW, selectorH, 7, 7);
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Microsoft JhengHei", Font.BOLD, 13));
            g2d.drawString("勇者", selectorX + 32, selectorY + 19);

            for (int i = 0; i < game.statusCompanionSelectorRects.length; i++) {
                game.statusCompanionSelectorRects[i] = null;
            }

            int selectorCursorX = selectorX + selectorW + selectorGap;
            for (int i = 0; i < game.companions.size() && i < game.statusCompanionSelectorRects.length; i++) {
                Companion c = game.companions.get(i);
                Rectangle selectorRect = new Rectangle(selectorCursorX, selectorY, selectorW, selectorH);
                game.statusCompanionSelectorRects[i] = selectorRect;
                boolean selected = (game.selectedStatusActor == i);
                g2d.setPaint(new GradientPaint(selectorRect.x, selectorRect.y,
                        selected ? new Color(82, 166, 148) : new Color(45, 92, 85),
                        selectorRect.x, selectorRect.y + selectorRect.height,
                        selected ? new Color(43, 130, 113) : new Color(28, 66, 61)));
                g2d.fillRoundRect(selectorRect.x, selectorRect.y, selectorRect.width, selectorRect.height, 7, 7);
                g2d.setColor(new Color(196, 236, 222));
                g2d.drawRoundRect(selectorRect.x, selectorRect.y, selectorRect.width, selectorRect.height, 7, 7);
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Microsoft JhengHei", Font.BOLD, 13));
                g2d.drawString(c.name, selectorRect.x + 38, selectorRect.y + 19);
                selectorCursorX += selectorW + selectorGap;
            }

            if (game.selectedStatusActor >= game.companions.size()) {
                game.selectedStatusActor = -1;
            }

            boolean showingPlayer = game.selectedStatusActor == -1;
            String actorName;
            int actorLevel;
            int actorExp;
            int actorExpToNext;
            int actorHp;
            int actorMaxHp;
            int actorMana;
            int actorMaxMana;
            int actorCp;
            int actorMaxCp;
            int actorPatk;
            int actorPdef;
            int actorMatk;
            int actorMdef;
            int actorSpeed;
            java.util.List<Skill> actorSkills;

            if (showingPlayer) {
                actorName = game.player.name;
                actorLevel = game.player.level;
                actorExp = game.player.exp;
                actorExpToNext = game.player.expToNext;
                actorHp = game.player.hp;
                actorMaxHp = game.player.maxHp;
                actorMana = game.player.mana;
                actorMaxMana = game.player.maxMana;
                actorCp = game.player.cp;
                actorMaxCp = game.player.maxCp;
                actorPatk = game.player.patk;
                actorPdef = game.player.pdef;
                actorMatk = game.player.matk;
                actorMdef = game.player.mdef;
                actorSpeed = game.player.battleSpeed;
                actorSkills = game.player.skills;
            } else {
                Companion actor = game.companions.get(game.selectedStatusActor);
                actorName = actor.name;
                actorLevel = actor.level;
                actorExp = actor.exp;
                actorExpToNext = actor.expToNext;
                actorHp = actor.hp;
                actorMaxHp = actor.maxHp;
                actorMana = actor.mana;
                actorMaxMana = actor.maxMana;
                actorCp = actor.cp;
                actorMaxCp = actor.maxCp;
                actorPatk = actor.patk;
                actorPdef = actor.pdef;
                actorMatk = actor.matk;
                actorMdef = actor.mdef;
                actorSpeed = actor.battleSpeed;
                actorSkills = actor.skills;
            }

            Color portraitTop;
            Color portraitBottom;
            Color portraitAccent;
            if (showingPlayer) {
                portraitTop = new Color(48, 66, 108);
                portraitBottom = new Color(24, 35, 64);
                portraitAccent = new Color(120, 175, 255);
            } else {
                int theme = Math.abs(game.selectedStatusActor) % 3;
                if (theme == 0) {
                    portraitTop = new Color(52, 95, 108);
                    portraitBottom = new Color(27, 58, 68);
                    portraitAccent = new Color(120, 235, 205);
                } else if (theme == 1) {
                    portraitTop = new Color(98, 74, 122);
                    portraitBottom = new Color(58, 42, 80);
                    portraitAccent = new Color(220, 185, 255);
                } else {
                    portraitTop = new Color(108, 78, 60);
                    portraitBottom = new Color(66, 48, 36);
                    portraitAccent = new Color(255, 210, 145);
                }
            }

            int portraitX = contentX + 14;
            int portraitY = contentY + 48;
            int portraitW = 220;
            int portraitH = contentH - 62;
            g2d.setPaint(new GradientPaint(portraitX, portraitY, portraitTop, portraitX,
                    portraitY + portraitH, portraitBottom));
            g2d.fillRoundRect(portraitX, portraitY, portraitW, portraitH, 10, 10);
            g2d.setColor(new Color(140, 170, 220));
            g2d.setStroke(new BasicStroke(1.5f));
            g2d.drawRoundRect(portraitX, portraitY, portraitW, portraitH, 10, 10);
            boolean showHeroPortrait = showingPlayer && game.heroPortrait != null;
            boolean showMoonPortrait = !showingPlayer && "月".equals(actorName) && game.moonPortrait != null;
            if (showHeroPortrait || showMoonPortrait) {
                BufferedImage portraitImage = showHeroPortrait ? game.heroPortrait : game.moonPortrait;
                double portraitScale = showHeroPortrait ? game.HERO_PORTRAIT_SCALE : game.MOON_PORTRAIT_SCALE;
                int nameAreaH = 28;
                int pad = 10;
                int maxW = portraitW - pad * 2;
                int maxH = portraitH - pad * 2 - nameAreaH;
                int imgW = portraitImage.getWidth();
                int imgH = portraitImage.getHeight();
                double scale = Math.min(maxW / (double) imgW, maxH / (double) imgH) * portraitScale;
                int drawW = (int) Math.round(imgW * scale);
                int drawH = (int) Math.round(imgH * scale);
                int imgX = portraitX + (portraitW - drawW) / 2;
                int imgY = portraitY + pad + (maxH - drawH) / 2;
                g2d.drawImage(portraitImage, imgX, imgY, drawW, drawH, null);

                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Microsoft JhengHei", Font.BOLD, 16));
                FontMetrics nameFm = g2d.getFontMetrics();
                int nameX = portraitX + (portraitW - nameFm.stringWidth(actorName)) / 2;
                int nameY = portraitY + portraitH - 8;
                g2d.drawString(actorName, nameX, nameY);
            } else {
                g2d.setColor(new Color(210, 220, 245));
                g2d.setFont(new Font("Microsoft JhengHei", Font.BOLD, 15));
                g2d.drawString("角色立繪區", portraitX + 62, portraitY + 34);
                g2d.setColor(new Color(175, 190, 220));
                g2d.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 12));
                g2d.drawString("(可放角色圖/大頭照)", portraitX + 50, portraitY + 58);
                g2d.setColor(portraitAccent.darker());
                g2d.fillOval(portraitX + 58, portraitY + 84, 104, 104);
                g2d.setColor(portraitAccent.brighter());
                g2d.drawOval(portraitX + 58, portraitY + 84, 104, 104);

                int iconCX = portraitX + 110;
                int iconCY = portraitY + 136;
                g2d.setStroke(new BasicStroke(3f));
                g2d.setColor(new Color(240, 245, 255));
                if (showingPlayer) {
                    g2d.drawLine(iconCX - 14, iconCY + 12, iconCX + 14, iconCY - 12);
                    g2d.drawLine(iconCX - 10, iconCY - 16, iconCX + 10, iconCY + 16);
                } else {
                    int iconType = Math.abs(game.selectedStatusActor) % 3;
                    if (iconType == 0) {
                        g2d.drawArc(iconCX - 14, iconCY - 14, 28, 28, 60, 240);
                        g2d.setColor(portraitAccent.brighter());
                        g2d.drawArc(iconCX - 8, iconCY - 14, 24, 28, 60, 240);
                    } else if (iconType == 1) {
                        Polygon p1 = new Polygon();
                        p1.addPoint(iconCX, iconCY - 16);
                        p1.addPoint(iconCX + 14, iconCY);
                        p1.addPoint(iconCX, iconCY + 16);
                        p1.addPoint(iconCX - 14, iconCY);
                        g2d.drawPolygon(p1);
                    } else {
                        Polygon p2 = new Polygon();
                        p2.addPoint(iconCX, iconCY - 16);
                        p2.addPoint(iconCX + 14, iconCY + 12);
                        p2.addPoint(iconCX - 14, iconCY + 12);
                        g2d.drawPolygon(p2);
                        g2d.drawLine(iconCX, iconCY - 16, iconCX, iconCY + 12);
                    }
                }

                g2d.setFont(new Font("Microsoft JhengHei", Font.BOLD, 16));
                g2d.drawString(actorName, portraitX + 84, portraitY + 224);
            }

            int cardX = portraitX + portraitW + 16;
            int cardW = contentX + contentW - cardX - 14;
            int cardH = 106;

            g2d.setPaint(new GradientPaint(cardX, portraitY, new Color(66, 84, 135), cardX,
                    portraitY + cardH, new Color(39, 55, 97)));
            g2d.fillRoundRect(cardX, portraitY, cardW, cardH, 10, 10);
            g2d.setColor(new Color(164, 194, 245));
            g2d.drawRoundRect(cardX, portraitY, cardW, cardH, 10, 10);
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Microsoft JhengHei", Font.BOLD, 14));
            g2d.drawString((showingPlayer ? "玩家數值" : "隊友數值") + " - " + actorName, cardX + 12, portraitY + 24);
            g2d.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 13));
            g2d.drawString("Lv." + actorLevel + "  EXP " + actorExp + " / " + actorExpToNext, cardX + 12, portraitY + 46);
            g2d.drawString("HP " + actorHp + "/" + actorMaxHp + "   MP " + actorMana + "/" + actorMaxMana,
                    cardX + 12, portraitY + 66);
            g2d.drawString("CP " + actorCp + "/" + actorMaxCp + "   SPD " + actorSpeed,
                    cardX + 12, portraitY + 86);
            g2d.drawString("物攻 " + actorPatk + "  物防 " + actorPdef + "  魔攻 " + actorMatk + "  魔防 " + actorMdef,
                    cardX + 12, portraitY + 102);

            g2d.setPaint(new GradientPaint(cardX, portraitY + cardH + 12, new Color(58, 78, 126), cardX,
                    portraitY + cardH + 12 + cardH, new Color(34, 50, 90)));
            g2d.fillRoundRect(cardX, portraitY + cardH + 12, cardW, cardH, 10, 10);
            g2d.setColor(new Color(150, 184, 236));
            g2d.drawRoundRect(cardX, portraitY + cardH + 12, cardW, cardH, 10, 10);
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Microsoft JhengHei", Font.BOLD, 14));
            g2d.drawString("戰技", cardX + 12, portraitY + cardH + 30);
            g2d.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 12));
            if (actorSkills == null || actorSkills.isEmpty()) {
                g2d.drawString("尚無戰技", cardX + 12, portraitY + cardH + 54);
            } else {
                int skillY = portraitY + cardH + 50;
                int maxSkillLines = 3;
                for (int i = 0; i < Math.min(actorSkills.size(), maxSkillLines); i++) {
                    Skill sk = actorSkills.get(i);
                    g2d.drawString("- " + sk.name + " (CP " + sk.cpCost + ")", cardX + 12, skillY);
                    skillY += 18;
                }
            }

            int tipY = portraitY + cardH * 2 + 26;
            g2d.setColor(new Color(205, 220, 250));
            g2d.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 12));
            g2d.drawString("上方可切換角色檢視：勇者/隊友", cardX + 12, tipY);
        } else if (game.selectedMapTab == 1) {
            java.util.List<String> bagItems = new java.util.ArrayList<>();
            bagItems.add("小藥 x" + game.player.smallPotions);
            bagItems.add("大藥 x" + game.player.largePotions);
            bagItems.add("金幣 x" + game.player.gold);

            int visibleStart = game.bagScrollOffset;
            int visibleEnd = Math.min(game.bagScrollOffset + game.BAG_VISIBLE_ITEMS, bagItems.size());
            int itemY = contentY + 34;
            for (int i = visibleStart; i < visibleEnd; i++) {
                g2d.setColor(i == game.bagScrollOffset ? Color.CYAN : Color.WHITE);
                g2d.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 15));
                g2d.drawString("- " + bagItems.get(i), contentX + 14, itemY);
                itemY += 30;
            }

            g2d.setColor(new Color(160, 180, 210));
            g2d.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 11));
            g2d.drawString("滑鼠滾輪可滾動項目", contentX + 14, contentY + contentH - 12);
        } else {
            int slotX = contentX + 12;
            int slotYBase = contentY + 16;
            int slotW = contentW - 24;
            int slotH = 76;

            for (int i = 0; i < game.MAX_SAVE_SLOTS; i++) {
                int slotY = slotYBase + i * (slotH + 8);
                g2d.setColor(i == game.selectedSaveSlot ? new Color(255, 200, 0, 180) : new Color(70, 80, 110, 180));
                g2d.fillRoundRect(slotX, slotY, slotW, slotH, 8, 8);
                g2d.setColor(Color.WHITE);
                g2d.setStroke(new BasicStroke(1.5f));
                g2d.drawRoundRect(slotX, slotY, slotW, slotH, 8, 8);

                g2d.setFont(new Font("Microsoft JhengHei", Font.BOLD, 14));
                g2d.drawString("槽位 " + (i + 1), slotX + 12, slotY + 24);

                File saveFile = getSaveFile(i);
                g2d.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 12));
                if (saveFile.exists()) {
                    try {
                        FileInputStream fis = new FileInputStream(saveFile);
                        ObjectInputStream ois = new ObjectInputStream(fis);
                        GameState gameStateData = (GameState) ois.readObject();
                        ois.close();
                        fis.close();
                        long timeAgo = System.currentTimeMillis() - gameStateData.saveTime;
                        g2d.drawString("保存於: " + formatTimeAgo(timeAgo), slotX + 12, slotY + 46);
                        g2d.drawString("地圖 " + (gameStateData.currentMapIndex + 1) + "  Lv." + gameStateData.playerLevel,
                                slotX + 12, slotY + 64);
                    } catch (Exception ex) {
                        g2d.drawString("保存資料無法讀取", slotX + 12, slotY + 46);
                    }
                } else {
                    g2d.setColor(new Color(180, 180, 180));
                    g2d.drawString("(空槽位)", slotX + 12, slotY + 46);
                }
            }

            g2d.setColor(new Color(220, 220, 150));
            g2d.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 12));
            g2d.drawString("點擊槽位立即保存，ESC 關閉選單", contentX + 12, contentY + contentH - 12);
        }
    }

    void drawMenuButton(Graphics2D g2d, Rectangle rect, String text, boolean selected) {
        if (selected) {
            g2d.setColor(new Color(255, 200, 0));
            g2d.fillRoundRect(rect.x, rect.y, rect.width, rect.height, 8, 8);
            g2d.setColor(Color.BLACK);
            g2d.setStroke(new BasicStroke(3));
        } else {
            g2d.setColor(new Color(100, 100, 100));
            g2d.fillRoundRect(rect.x, rect.y, rect.width, rect.height, 8, 8);
            g2d.setColor(Color.WHITE);
            g2d.setStroke(new BasicStroke(1));
        }
        g2d.drawRoundRect(rect.x, rect.y, rect.width, rect.height, 8, 8);

        g2d.setFont(new Font("Microsoft JhengHei", Font.BOLD, 18));
        FontMetrics fm = g2d.getFontMetrics();
        int textX = rect.x + (rect.width - fm.stringWidth(text)) / 2;
        int textY = rect.y + ((rect.height - fm.getHeight()) / 2) + fm.getAscent();
        g2d.drawString(text, textX, textY);
    }

    void startNewGame() {
        game.state = 0;

        game.player = new Player(2 * game.TILE_SIZE, 2 * game.TILE_SIZE);
        game.companions.clear();
        game.companions.add(new Companion(3 * game.TILE_SIZE, 2 * game.TILE_SIZE, "月"));

        game.mapIndex = 0;
        game.map = game.allMaps[game.mapIndex];
        game.allEnemies.set(game.mapIndex, game.mapManager.spawnEnemiesForMap(game.mapIndex));
        game.enemies = game.allEnemies.get(game.mapIndex);

        game.currentEnemies.clear();
        game.enemyBattleSlots.clear();
        game.originalBattleEnemies.clear();
        game.battleOrder.clear();
        game.triggeredEnemy = null;
        game.currentActor = null;
        game.waitingForPlayerDecision = false;
        game.currentActionIndex = 0;
        game.currentAttackingEnemyIndex = -1;
        game.enemyAttackTargetIsPlayer = true;
        game.enemyAttackTargetCompanionIndex = -1;
        game.targetingEnemyIndex = -1;
        game.selectedSkill = null;
        game.selectingTargetMode = "";
        game.selectingPotionType = "";
        game.previewDrops = "";
        game.settlementDrops = "";
        game.showLoadMenu = false;
        game.showMapMenu = false;
        game.selectedMapTab = 0;
        game.selectedStatusActor = -1;

        game.updateMapMusic();

        game.repaint();
    }

    void saveGame(int slot) {
        try {
            GameState state = new GameState();

            state.playerX = game.player.x;
            state.playerY = game.player.y;
            state.playerLevel = game.player.level;
            state.playerExp = game.player.exp;
            state.playerHP = game.player.hp;
            state.playerMana = game.player.mana;
            state.playerCP = game.player.cp;
            state.playerMaxHP = game.player.maxHp;
            state.playerMaxMana = game.player.maxMana;
            state.playerMaxCP = game.player.maxCp;
            state.playerPatk = game.player.patk;
            state.playerPdef = game.player.pdef;
            state.playerMatk = game.player.matk;
            state.playerMdef = game.player.mdef;

            state.smallPotions = game.player.smallPotions;
            state.largePotions = game.player.largePotions;
            state.gold = game.player.gold;

            if (!game.companions.isEmpty()) {
                Companion c = game.companions.get(0);
                state.companionX = c.x;
                state.companionY = c.y;
                state.companionHP = c.hp;
                state.companionMana = c.mana;
                state.companionCP = c.cp;
                state.companionMaxHP = c.maxHp;
                state.companionMaxMana = c.maxMana;
                state.companionMaxCP = c.maxCp;
                state.companionPatk = c.patk;
                state.companionPdef = c.pdef;
                state.companionMatk = c.matk;
                state.companionMdef = c.mdef;
                state.companionLevel = c.level;
                state.companionExp = c.exp;
            }

            state.currentMapIndex = game.mapIndex;

            int enemyId = 0;
            for (java.util.List<Enemy> enemyList : game.allEnemies) {
                for (Enemy e : enemyList) {
                    if (e.defeated) {
                        state.defeatedEnemies.put("enemy_" + enemyId, true);
                    }
                    enemyId++;
                }
            }

            File saveFile = getSaveFile(slot);
            String saveFileName = saveFile.getPath();
            FileOutputStream fos = new FileOutputStream(saveFile);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(state);
            oos.close();
            fos.close();

            System.out.println("遊戲已保存到槽位 " + (slot + 1) + " (" + saveFileName + ")");
            JOptionPane.showMessageDialog(game, "遊戲已保存到槽位 " + (slot + 1), "保存成功", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            System.err.println("保存遊戲時出錯: " + e.getMessage());
            JOptionPane.showMessageDialog(game, "保存遊戲失敗: " + e.getMessage(), "保存失敗", JOptionPane.ERROR_MESSAGE);
        }
    }

    void loadGame(int slot) {
        try {
            File saveFile = getSaveFile(slot);
            String saveFileName = saveFile.getPath();

            if (!saveFile.exists()) {
                JOptionPane.showMessageDialog(game, "槽位 " + (slot + 1) + " 沒有保存檔案", "無法加載", JOptionPane.WARNING_MESSAGE);
                return;
            }

            FileInputStream fis = new FileInputStream(saveFile);
            ObjectInputStream ois = new ObjectInputStream(fis);
            GameState gameStateData = (GameState) ois.readObject();
            ois.close();
            fis.close();

            game.player.x = gameStateData.playerX;
            game.player.y = gameStateData.playerY;
            game.player.level = gameStateData.playerLevel;
            game.player.exp = gameStateData.playerExp;
            game.player.hp = gameStateData.playerHP;
            game.player.mana = gameStateData.playerMana;
            game.player.cp = gameStateData.playerCP;
            game.player.maxHp = gameStateData.playerMaxHP;
            game.player.maxMana = gameStateData.playerMaxMana;
            game.player.maxCp = gameStateData.playerMaxCP > 0 ? gameStateData.playerMaxCP : 200;
            game.player.cp = Math.min(game.player.cp, game.player.maxCp);
            game.player.patk = gameStateData.playerPatk;
            game.player.pdef = gameStateData.playerPdef;
            game.player.matk = gameStateData.playerMatk;
            game.player.mdef = gameStateData.playerMdef;

            game.player.smallPotions = gameStateData.smallPotions;
            game.player.largePotions = gameStateData.largePotions;
            game.player.gold = gameStateData.gold;

            if (!game.companions.isEmpty()) {
                Companion c = game.companions.get(0);
                c.x = gameStateData.companionX;
                c.y = gameStateData.companionY;
                c.hp = gameStateData.companionHP;
                c.mana = gameStateData.companionMana;
                c.cp = gameStateData.companionCP;
                c.maxHp = gameStateData.companionMaxHP;
                c.maxMana = gameStateData.companionMaxMana;
                c.maxCp = gameStateData.companionMaxCP > 0 ? gameStateData.companionMaxCP : 200;
                c.cp = Math.min(c.cp, c.maxCp);
                c.patk = gameStateData.companionPatk;
                c.pdef = gameStateData.companionPdef;
                c.matk = gameStateData.companionMatk;
                c.mdef = gameStateData.companionMdef;
                c.level = gameStateData.companionLevel;
                c.exp = gameStateData.companionExp;
            }

            game.mapManager.switchMap(gameStateData.currentMapIndex, gameStateData.playerX, gameStateData.playerY);

            int enemyId = 0;
            for (java.util.List<Enemy> enemyList : game.allEnemies) {
                for (Enemy e : enemyList) {
                    if (gameStateData.defeatedEnemies.containsKey("enemy_" + enemyId)) {
                        e.defeated = true;
                    }
                    enemyId++;
                }
            }

            game.currentEnemies.clear();
            game.state = 0;
            game.repaint();

            System.out.println("遊戲已從槽位 " + (slot + 1) + " 加載（" + gameStateData.getSaveTimeString() + "）");
            JOptionPane.showMessageDialog(game, "遊戲已從槽位 " + (slot + 1) + " 加載\n保存時間: " + gameStateData.getSaveTimeString(), "加載成功", JOptionPane.INFORMATION_MESSAGE);
        } catch (ClassNotFoundException e) {
            System.err.println("無法識別保存檔案格式: " + e.getMessage());
            JOptionPane.showMessageDialog(game, "保存檔案格式錯誤", "加載失敗", JOptionPane.ERROR_MESSAGE);
        } catch (IOException e) {
            System.err.println("加載遊戲時出錯: " + e.getMessage());
            JOptionPane.showMessageDialog(game, "加載遊戲失敗: " + e.getMessage(), "加載失敗", JOptionPane.ERROR_MESSAGE);
        }
    }

    String formatTimeAgo(long milliseconds) {
        long seconds = milliseconds / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (days > 0) {
            return days + "天前";
        } else if (hours > 0) {
            return hours + "小時前";
        } else if (minutes > 0) {
            return minutes + "分鐘前";
        } else {
            return "剛才";
        }
    }

    File getSaveFile(int slot) {
        File saveDir = new File(game.SAVE_DIR_NAME);
        if (!saveDir.exists()) {
            saveDir.mkdirs();
        }
        return new File(saveDir, game.SAVE_FILE_PREFIX + slot + game.SAVE_FILE_EXT);
    }
}
