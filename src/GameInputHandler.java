package src;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

class GameInputHandler {
    private final GamePanel game;
    private final MenuUI menuUI;
    private final BattleManager battleManager;

    GameInputHandler(GamePanel game, MenuUI menuUI, BattleManager battleManager) {
        this.game = game;
        this.menuUI = menuUI;
        this.battleManager = battleManager;
    }

    void keyPressed(KeyEvent e) {
        if (game.showLoadMenu) {
            int key = e.getKeyCode();
            switch (key) {
                case KeyEvent.VK_UP:
                    game.selectedLoadSlot = (game.selectedLoadSlot - 1 + game.MAX_SAVE_SLOTS) % game.MAX_SAVE_SLOTS;
                    game.repaint();
                    break;
                case KeyEvent.VK_DOWN:
                    game.selectedLoadSlot = (game.selectedLoadSlot + 1) % game.MAX_SAVE_SLOTS;
                    game.repaint();
                    break;
                case KeyEvent.VK_ENTER:
                    menuUI.loadGame(game.selectedLoadSlot);
                    game.showLoadMenu = false;
                    break;
                case KeyEvent.VK_ESCAPE:
                    game.showLoadMenu = false;
                    game.repaint();
                    break;
                default:
                    break;
            }
            return;
        }

        if (game.state == -1) {
            int key = e.getKeyCode();
            switch (key) {
                case KeyEvent.VK_LEFT:
                    game.selectedMainMenuOption = (game.selectedMainMenuOption - 1 + 3) % 3;
                    break;
                case KeyEvent.VK_RIGHT:
                    game.selectedMainMenuOption = (game.selectedMainMenuOption + 1) % 3;
                    break;
                case KeyEvent.VK_UP:
                    game.selectedMainMenuOption = (game.selectedMainMenuOption - 1 + 3) % 3;
                    break;
                case KeyEvent.VK_DOWN:
                    game.selectedMainMenuOption = (game.selectedMainMenuOption + 1) % 3;
                    break;
                case KeyEvent.VK_ENTER:
                    if (game.selectedMainMenuOption == 0) {
                        menuUI.startNewGame();
                    } else if (game.selectedMainMenuOption == 1) {
                        game.showLoadMenu = true;
                        game.selectedLoadSlot = 0;
                    } else if (game.selectedMainMenuOption == 2) {
                        System.exit(0);
                    }
                    break;
                default:
                    break;
            }
            game.repaint();
            return;
        }

        if (game.state == 2) {
            game.state = 0;
            game.currentEnemies.clear();
            game.originalBattleEnemies.clear();
            game.triggeredEnemy = null;
            game.isZeroExpSettlement = false;
            game.previewDrops = "";

            game.damagedEnemyIndex = -1;
            game.damagedEnemyTicks = 0;
            game.playerTakingDamage = false;
            game.playerDamageTicks = 0;
            for (int i = 0; i < game.companionTakingDamage.length; i++) {
                game.companionTakingDamage[i] = 0;
            }
            game.updateMapMusic();
            return;
        }

        if (game.state == 0 && game.showMapMenu) {
            int key = e.getKeyCode();
            switch (key) {
                case KeyEvent.VK_ESCAPE:
                    game.showMapMenu = false;
                    game.hoveredMapTab = -1;
                    return;
                case KeyEvent.VK_LEFT:
                    game.selectedMapTab = (game.selectedMapTab - 1 + 3) % 3;
                    return;
                case KeyEvent.VK_RIGHT:
                    game.selectedMapTab = (game.selectedMapTab + 1) % 3;
                    return;
                case KeyEvent.VK_UP:
                    if (game.selectedMapTab == 2) {
                        game.selectedSaveSlot = (game.selectedSaveSlot - 1 + game.MAX_SAVE_SLOTS) % game.MAX_SAVE_SLOTS;
                    }
                    return;
                case KeyEvent.VK_DOWN:
                    if (game.selectedMapTab == 2) {
                        game.selectedSaveSlot = (game.selectedSaveSlot + 1) % game.MAX_SAVE_SLOTS;
                    }
                    return;
                case KeyEvent.VK_ENTER:
                    if (game.selectedMapTab == 2) {
                        menuUI.saveGame(game.selectedSaveSlot);
                    }
                    return;
                default:
                    return;
            }
        }

        if (game.state == 0) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_LEFT:
                    game.player.vx = -game.player.speed;
                    game.player.vy = 0;
                    game.playerFacingRight = false;
                    game.mouseDown = false;
                    game.keyDown = true;
                    break;
                case KeyEvent.VK_RIGHT:
                    game.player.vx = game.player.speed;
                    game.player.vy = 0;
                    game.playerFacingRight = true;
                    game.mouseDown = false;
                    game.keyDown = true;
                    break;
                case KeyEvent.VK_UP:
                    game.player.vx = 0;
                    game.player.vy = -game.player.speed;
                    game.mouseDown = false;
                    game.keyDown = true;
                    break;
                case KeyEvent.VK_DOWN:
                    game.player.vx = 0;
                    game.player.vy = game.player.speed;
                    game.mouseDown = false;
                    game.keyDown = true;
                    break;
                default:
                    break;
            }
        } else if (game.state == 1 && game.currentEnemies.size() > 0) {
            if (game.showingFleeMessage) {
                game.showingFleeMessage = false;
                if (game.fleeMessage.contains("成功")) {
                    game.isZeroExpSettlement = true;
                    game.lastBattleExp = 0;
                    game.expForNextLevel = 0;
                    game.levelsGained = 0;
                    for (int i = 0; i < game.companionLevelsGained.length; i++) {
                        game.companionLevelsGained[i] = 0;
                        game.companionExpForNextLevel[i] = 0;
                    }
                    game.previewDrops = "";
                    game.state = 2;
                    game.playBattleEndMusic();
                    game.currentEnemies.clear();
                    game.triggeredEnemy = null;
                    game.currentActor = null;
                    game.waitingForPlayerDecision = false;
                } else {
                    battleManager.completeActionAndRefreshBattleOrder(30);
                }
                game.repaint();
                return;
            }

            if (!game.animating && game.waitingForPlayerDecision) {
                int key = e.getKeyCode();

                if (key == KeyEvent.VK_A) {
                    game.selectingTargetMode = "attack";
                } else if (key == KeyEvent.VK_S) {
                    battleManager.showSkillMenuForTargeting();
                } else if (key == KeyEvent.VK_H) {
                    battleManager.showPotionMenuForTargeting();
                } else if (key == KeyEvent.VK_R) {
                    if (game.isBossBattleActive()) {
                        return;
                    }
                    if (game.currentActor instanceof Player || game.currentActor instanceof Companion) {
                        if (Math.random() < 0.6) {
                            game.fleeMessage = "逃跑成功！敵人短暫失去戰意。";
                            game.showingFleeMessage = true;
                            for (Enemy origEnemy : game.originalBattleEnemies) {
                                origEnemy.setBattleLock(7000);
                            }
                        } else {
                            game.fleeMessage = "逃跑失敗！";
                            game.showingFleeMessage = true;
                        }
                    }
                }
            }
        }
    }

    void keyReleased(KeyEvent e) {
        if (game.state == 0) {
            game.player.vx = 0;
            game.player.vy = 0;
            game.keyDown = false;
        }
    }

    void mousePressed(MouseEvent e) {
        Point p = e.getPoint();

        if (game.showLoadMenu) {
            for (int i = 0; i < game.MAX_SAVE_SLOTS; i++) {
                int slotX = game.getWidth() / 2 - 150 + 20;
                int slotY = game.getHeight() / 2 - 125 + 50 + i * 50;
                Rectangle slotRect = new Rectangle(slotX, slotY, 260, 40);

                if (slotRect.contains(p)) {
                    game.selectedLoadSlot = i;
                    menuUI.loadGame(game.selectedLoadSlot);
                    game.showLoadMenu = false;
                    return;
                }
            }
            return;
        }

        if (game.state == -1) {
            if (game.newGameRect != null && game.newGameRect.contains(p)) {
                game.selectedMainMenuOption = 0;
                menuUI.startNewGame();
                return;
            }
            if (game.loadGameRect != null && game.loadGameRect.contains(p)) {
                game.selectedMainMenuOption = 1;
                game.showLoadMenu = true;
                game.selectedLoadSlot = 0;
                game.repaint();
                return;
            }
            if (game.exitRect != null && game.exitRect.contains(p)) {
                game.selectedMainMenuOption = 2;
                System.exit(0);
                return;
            }
        }

        if (game.state == 2) {
            game.state = 0;
            game.currentEnemies.clear();
            game.originalBattleEnemies.clear();
            game.triggeredEnemy = null;
            game.previewDrops = "";
            game.updateMapMusic();
            return;
        }
        if (game.state == 0) {
            if (game.menuRect != null && game.menuRect.contains(p)) {
                game.showMapMenu = !game.showMapMenu;
                if (game.showMapMenu) {
                    game.selectedMapTab = 0;
                    game.selectedStatusActor = -1;
                } else {
                    game.hoveredMapTab = -1;
                }
                return;
            }

            if (game.showMapMenu) {
                if (game.statusTabRect != null && game.statusTabRect.contains(p)) {
                    game.selectedMapTab = 0;
                    return;
                }
                if (game.bagTabRect != null && game.bagTabRect.contains(p)) {
                    game.selectedMapTab = 1;
                    game.bagScrollOffset = 0;
                    return;
                }
                if (game.saveTabRect != null && game.saveTabRect.contains(p)) {
                    game.selectedMapTab = 2;
                    return;
                }

                if (game.selectedMapTab == 0) {
                    if (game.statusPlayerSelectorRect != null && game.statusPlayerSelectorRect.contains(p)) {
                        game.selectedStatusActor = -1;
                        return;
                    }
                    for (int i = 0; i < game.statusCompanionSelectorRects.length; i++) {
                        Rectangle selectorRect = game.statusCompanionSelectorRects[i];
                        if (selectorRect != null && selectorRect.contains(p)) {
                            game.selectedStatusActor = i;
                            return;
                        }
                    }
                }

                if (game.selectedMapTab == 2 && game.mapMenuPanelRect != null && game.mapMenuPanelRect.contains(p)) {
                    int panelX = game.mapMenuPanelRect.x;
                    int panelY = game.mapMenuPanelRect.y;
                    int panelW = game.mapMenuPanelRect.width;

                    int tabY = panelY + 10;
                    int tabH = 38;
                    int contentX = panelX + 14;
                    int contentY = tabY + tabH + 10;
                    int contentW = panelW - 28;
                    int slotX = contentX + 12;
                    int slotYBase = contentY + 16;
                    int slotW = contentW - 24;
                    int slotH = 76;

                    for (int i = 0; i < game.MAX_SAVE_SLOTS; i++) {
                        int slotY = slotYBase + i * (slotH + 8);
                        Rectangle slotRect = new Rectangle(slotX, slotY, slotW, slotH);
                        if (slotRect.contains(p)) {
                            game.selectedSaveSlot = i;
                            menuUI.saveGame(game.selectedSaveSlot);
                            return;
                        }
                    }
                }

                return;
            }
        }
        if (game.state == 1 && game.showingFleeMessage) {
            game.showingFleeMessage = false;
            if (game.fleeMessage.contains("成功")) {
                game.state = 2;
                game.isZeroExpSettlement = true;
                game.lastBattleExp = 0;
                game.expForNextLevel = 0;
                game.levelsGained = 0;
                for (int i = 0; i < game.companionLevelsGained.length; i++) {
                    game.companionLevelsGained[i] = 0;
                    game.companionExpForNextLevel[i] = 0;
                }
                game.previewDrops = "";
                game.playBattleEndMusic();
                game.currentEnemies.clear();
                game.triggeredEnemy = null;
                game.currentActor = null;
                game.waitingForPlayerDecision = false;
                game.updateMapMusic();
            } else {
                battleManager.completeActionAndRefreshBattleOrder(30);
            }
            game.repaint();
            return;
        }
        if (game.state == 1 && !game.animating && game.waitingForPlayerDecision) {
            int button = e.getButton();

            if (button == MouseEvent.BUTTON3) {
                game.selectingTargetMode = "";
                game.selectingPotionType = "";
                game.selectedSkill = null;
                game.repaint();
                return;
            }

            if (button != MouseEvent.BUTTON1) {
                return;
            }

            if (!game.selectingTargetMode.isEmpty()) {
                int mouseX = (int) p.getX();
                int mouseY = (int) p.getY();

                if ("attack".equals(game.selectingTargetMode)) {
                    int targetIndex = battleManager.findClosestEnemyIndexAt(mouseX, mouseY, 28);
                    if (targetIndex >= 0) {
                        game.targetingEnemyIndex = targetIndex;
                        battleManager.executeAttackAnimation();
                        return;
                    }
                    return;
                } else if ("skill".equals(game.selectingTargetMode)) {
                    int targetIndex = battleManager.findClosestEnemyIndexAt(mouseX, mouseY, 28);
                    if (targetIndex >= 0) {
                        battleManager.executeSkillAnimation(game.currentEnemies.get(targetIndex));
                        return;
                    }

                    double dx = mouseX - game.battleScreenPlayerX;
                    double dy = mouseY - game.battleScreenPlayerY;
                    double dist = Math.hypot(dx, dy);
                    if (dist < 40) {
                        battleManager.executeSkillAnimation(game.player);
                        return;
                    }

                    for (int i = 0; i < game.companions.size(); i++) {
                        dx = mouseX - game.battleScreenCompanionX[i];
                        dy = mouseY - game.battleScreenCompanionY[i];
                        dist = Math.hypot(dx, dy);
                        if (dist < 40) {
                            battleManager.executeSkillAnimation(game.companions.get(i));
                            return;
                        }
                    }
                    return;
                } else if ("potion".equals(game.selectingTargetMode)) {
                    double dx = mouseX - game.battleScreenPlayerX;
                    double dy = mouseY - game.battleScreenPlayerY;
                    double dist = Math.hypot(dx, dy);
                    if (dist < 40) {
                        battleManager.executePotionAnimation(game.player);
                        return;
                    }

                    for (int i = 0; i < game.companions.size(); i++) {
                        dx = mouseX - game.battleScreenCompanionX[i];
                        dy = mouseY - game.battleScreenCompanionY[i];
                        dist = Math.hypot(dx, dy);
                        if (dist < 40) {
                            battleManager.executePotionAnimation(game.companions.get(i));
                            return;
                        }
                    }
                    return;
                }
                return;
            }

            if (game.attackRect != null && game.attackRect.contains(p)) {
                game.selectingTargetMode = "attack";
                return;
            } else if (game.skillRect != null && game.skillRect.contains(p)) {
                battleManager.showSkillMenuForTargeting();
                return;
            } else if (game.healRect != null && game.healRect.contains(p)) {
                battleManager.showPotionMenuForTargeting();
                return;
            } else if (game.runRect != null && game.runRect.contains(p)) {
                if (game.isBossBattleActive()) {
                    return;
                }
                if (game.currentActor instanceof Player || game.currentActor instanceof Companion) {
                    if (Math.random() < 0.6) {
                        game.fleeMessage = "逃跑成功！敵人短暫失去戰意。";
                        game.showingFleeMessage = true;
                        for (Enemy origEnemy : game.originalBattleEnemies) {
                            origEnemy.setBattleLock(7000);
                        }
                    } else {
                        game.fleeMessage = "逃跑失敗！";
                        game.showingFleeMessage = true;
                    }
                }
                return;
            }
        }

        game.mouseDown = true;
        updateMouseDirection(e);
    }

    void mouseReleased(MouseEvent e) {
        game.mouseDown = false;
        game.player.vx = 0;
        game.player.vy = 0;
    }

    void mouseDragged(MouseEvent e) {
        if (game.mouseDown) {
            updateMouseDirection(e);
        }
    }

    void mouseMoved(MouseEvent e) {
        int mouseX = e.getX();
        int mouseY = e.getY();

        if (game.state == 0 && game.showMapMenu) {
            game.hoveredMapTab = -1;
            if (game.statusTabRect != null && game.statusTabRect.contains(mouseX, mouseY)) {
                game.hoveredMapTab = 0;
            } else if (game.bagTabRect != null && game.bagTabRect.contains(mouseX, mouseY)) {
                game.hoveredMapTab = 1;
            } else if (game.saveTabRect != null && game.saveTabRect.contains(mouseX, mouseY)) {
                game.hoveredMapTab = 2;
            }
        } else {
            game.hoveredMapTab = -1;
        }

        if (game.state == 1) {
            game.hoveredEnemyIndex = -1;

            for (int i = 0; i < game.currentEnemies.size(); i++) {
                int enemyX = game.battleScreenEnemyX[i];
                int enemyY = game.battleScreenEnemyY[i];

                double dx = mouseX - enemyX;
                double dy = mouseY - enemyY;
                double distance = Math.sqrt(dx * dx + dy * dy);

                if (distance <= 15) {
                    game.hoveredEnemyIndex = i;
                    break;
                }
            }
        }
    }

    void mouseExited(MouseEvent e) {
        game.hoveredEnemyIndex = -1;
    }

    void mouseWheelMoved(MouseWheelEvent e) {
        if (game.state == 0 && game.showMapMenu && game.selectedMapTab == 1) {
            int maxScroll = Math.max(0, 2 - game.BAG_VISIBLE_ITEMS);
            game.bagScrollOffset -= e.getWheelRotation();
            game.bagScrollOffset = Math.max(0, Math.min(game.bagScrollOffset, maxScroll));
        }
    }

    private void updateMouseDirection(MouseEvent e) {
        int mx = e.getX() / game.TILE_SIZE;
        int my = e.getY() / game.TILE_SIZE;
        game.mouseGridX = mx;
        game.mouseGridY = my;
        int dirX = Integer.compare(mx, (int) (game.player.x / game.TILE_SIZE));
        int dirY = Integer.compare(my, (int) (game.player.y / game.TILE_SIZE));
        game.player.vx = dirX * game.player.speed;
        game.player.vy = dirY * game.player.speed;
    }
}
