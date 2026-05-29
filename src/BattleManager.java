/**
 * BattleManager.java
 * 說明: 管理戰鬥邏輯（生成敵人隊列、執行動作、處理戰鬥事件與順序）。
 */

package src;

import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import java.util.ArrayList;
import java.util.List;

class BattleManager {
    private final GamePanel game;

    BattleManager(GamePanel game) {
        this.game = game;
    }

    void updateBattleFrame() {
        if (game.animating) {
            game.animTicks++;

            boolean playerMovingInBattle = game.currentActor instanceof Player
                    && !isHealingSkillAnimation()
                    && ("playerAttack".equals(game.animAction) || "companionAttack".equals(game.animAction));
            game.updatePlayerWalkAnimationForBattle(playerMovingInBattle);

            if (game.damagedEnemyTicks > 0) {
                game.damagedEnemyTicks--;
                if (game.damagedEnemyTicks == 0) {
                    game.damagedEnemyIndex = -1;
                }
            }
            if (game.playerDamageTicks > 0) {
                game.playerDamageTicks--;
                if (game.playerDamageTicks == 0) {
                    game.playerTakingDamage = false;
                }
            }
            if (game.playerHealGlowTicks > 0) {
                game.playerHealGlowTicks--;
            }
            for (int i = 0; i < game.companionTakingDamage.length; i++) {
                if (game.companionTakingDamage[i] > 0) {
                    game.companionTakingDamage[i]--;
                }
                if (game.companionHealGlowTicks[i] > 0) {
                    game.companionHealGlowTicks[i]--;
                }
            }

            if (game.animTicks >= game.ANIM_DURATION) {
                if ("playerAttack".equals(game.animAction)) {
                    int dmg = 0;
                    Object attacker = game.currentActor;

                    int patk = 0;
                    if (attacker instanceof Player) {
                        patk = ((Player) attacker).patk;
                    } else if (attacker instanceof Companion) {
                        patk = ((Companion) attacker).patk;
                    } else {
                        patk = game.player.patk;
                    }

                    if (game.selectedSkill != null) {
                        int targetIndex = (game.targetingEnemyIndex >= 0 && game.targetingEnemyIndex < game.currentEnemies.size())
                                ? game.targetingEnemyIndex : 0;
                        if (!game.currentEnemies.isEmpty() && targetIndex < game.currentEnemies.size()) {
                            game.selectedSkill.execute(game, game.player, game.currentEnemies.get(targetIndex));

                            if (attacker instanceof Player) {
                                ((Player) attacker).cp -= game.selectedSkill.cpCost;
                            } else if (attacker instanceof Companion) {
                                ((Companion) attacker).cp -= game.selectedSkill.cpCost;
                            }

                            if (!(game.selectedSkill instanceof SkillMoonSlice)
                                    && targetIndex >= 0
                                    && targetIndex < game.currentEnemies.size()) {
                                game.currentEnemies.get(targetIndex).cp = Math.min(game.currentEnemies.get(targetIndex).cp + 5,
                                        game.currentEnemies.get(targetIndex).maxCp);
                            }

                            removeDefeatedEnemiesFromBattle();
                            if (tryEnterVictorySettlementIfNoEnemies()) {
                                game.selectedSkill = null;
                                return;
                            }
                        }
                        game.selectedSkill = null;
                    } else {
                        int targetIndex = (game.targetingEnemyIndex >= 0 && game.targetingEnemyIndex < game.currentEnemies.size())
                                ? game.targetingEnemyIndex : 0;
                        if (!game.currentEnemies.isEmpty() && targetIndex < game.currentEnemies.size()) {
                            Enemy targetEnemy = game.currentEnemies.get(targetIndex);
                            dmg = rollDamage(patk, targetEnemy.pdef);
                            targetEnemy.hp -= dmg;

                            game.player.cp = Math.min(game.player.cp + 5, game.player.maxCp);
                            targetEnemy.cp = Math.min(targetEnemy.cp + 5, targetEnemy.maxCp);

                            game.damagedEnemyIndex = targetIndex;
                            game.damagedEnemyTicks = game.DAMAGE_DISPLAY_DURATION;

                            if (targetEnemy.hp <= 0) {
                                targetEnemy.defeated = true;
                                removeEnemyAt(targetIndex);
                                if (targetIndex >= game.currentEnemies.size() && targetIndex > 0) {
                                    game.targetingEnemyIndex = targetIndex - 1;
                                } else if (targetIndex < game.currentEnemies.size()) {
                                    game.targetingEnemyIndex = targetIndex;
                                } else {
                                    game.targetingEnemyIndex = -1;
                                }

                                if (game.currentEnemies.isEmpty()) {
                                    game.markBossDefeatedIfNeeded();
                                    for (Enemy origEnemy : game.originalBattleEnemies) {
                                        origEnemy.defeated = true;
                                    }
                                    game.originalBattleEnemies.clear();

                                    game.isZeroExpSettlement = false;
                                    int totalExp = 60;
                                    game.levelsGained = game.player.gainExp(totalExp / 2);
                                    for (int i = 0; i < game.companions.size(); i++) {
                                        Companion c = game.companions.get(i);
                                        game.companionLevelsGained[i] = c.gainExp(totalExp / 2);
                                        game.companionExpForNextLevel[i] = Math.max(0, c.expToNext - c.exp);
                                    }
                                    game.lastBattleExp = totalExp;
                                    game.expForNextLevel = Math.max(0, game.player.expToNext - game.player.exp);
                                    applyDropsFromPreview();
                                    game.settlementDrops = game.previewDrops.isEmpty() ? "" : "掉落物品: " + game.previewDrops;
                                    game.state = 2;
                                    game.playBattleEndMusic();
                                    game.animating = false;
                                    game.animTicks = 0;
                                    game.currentActor = null;
                                    game.waitingForPlayerDecision = false;
                                    game.repaint();
                                    return;
                                }
                            }
                        }
                    }

                    completeActionAndRefreshBattleOrder(game.selectedSkill != null ? 40 : 20);
                } else if ("companionAttack".equals(game.animAction)) {
                    if (game.currentActionIndex < game.battleOrder.size()) {
                        BattleUnit currentUnit = game.battleOrder.get(game.currentActionIndex);
                        if (currentUnit.unit instanceof Companion) {
                            Companion companion = (Companion) currentUnit.unit;
                            int targetIndex = (game.targetingEnemyIndex >= 0 && game.targetingEnemyIndex < game.currentEnemies.size())
                                    ? game.targetingEnemyIndex : 0;
                            if (!game.currentEnemies.isEmpty() && targetIndex < game.currentEnemies.size()) {
                                Enemy targetEnemy = game.currentEnemies.get(targetIndex);
                                int dmg = rollDamage(companion.patk, targetEnemy.pdef);
                                targetEnemy.hp -= dmg;

                                companion.cp = Math.min(companion.cp + 5, companion.maxCp);
                                targetEnemy.cp = Math.min(targetEnemy.cp + 5, targetEnemy.maxCp);

                                game.damagedEnemyIndex = targetIndex;
                                game.damagedEnemyTicks = game.DAMAGE_DISPLAY_DURATION;

                                if (targetEnemy.hp <= 0) {
                                    targetEnemy.defeated = true;
                                    removeEnemyAt(targetIndex);
                                    if (targetIndex >= game.currentEnemies.size() && targetIndex > 0) {
                                        game.targetingEnemyIndex = targetIndex - 1;
                                    } else if (targetIndex < game.currentEnemies.size()) {
                                        game.targetingEnemyIndex = targetIndex;
                                    } else {
                                        game.targetingEnemyIndex = -1;
                                    }

                                    if (game.currentEnemies.isEmpty()) {
                                        game.markBossDefeatedIfNeeded();
                                        for (Enemy origEnemy : game.originalBattleEnemies) {
                                            origEnemy.defeated = true;
                                        }
                                        game.originalBattleEnemies.clear();

                                        game.isZeroExpSettlement = false;
                                        int exp = 60;
                                        game.levelsGained = game.player.gainExp(exp / 2);
                                        for (int i = 0; i < game.companions.size(); i++) {
                                            Companion c = game.companions.get(i);
                                            game.companionLevelsGained[i] = c.gainExp(exp / 2);
                                            game.companionExpForNextLevel[i] = Math.max(0, c.expToNext - c.exp);
                                        }
                                        game.lastBattleExp = exp;
                                        game.expForNextLevel = Math.max(0, game.player.expToNext - game.player.exp);
                                        applyDropsFromPreview();
                                        game.settlementDrops = game.previewDrops.isEmpty() ? "" : "掉落物品: " + game.previewDrops;
                                        game.state = 2;
                                        game.playBattleEndMusic();
                                        game.animating = false;
                                        game.animTicks = 0;
                                        game.repaint();
                                        return;
                                    }
                                }
                            }
                        }
                    }
                    completeActionAndRefreshBattleOrder(20);
                } else if ("enemyAttack".equals(game.animAction)) {
                    Object target = getEnemyAttackTargetUnit();
                    if (target == null) {
                        chooseEnemyAttackTarget();
                        target = getEnemyAttackTargetUnit();
                    }

                    int dmg = 0;
                    int enemyPatk = 36;
                    if (!game.currentEnemies.isEmpty()) {
                        enemyPatk = game.currentEnemies.get(0).patk;
                    }

                    if (target instanceof Player) {
                        Player p = (Player) target;
                        dmg = rollDamage(enemyPatk, p.pdef);
                        p.hp = Math.max(0, p.hp - dmg);
                        game.playerTakingDamage = true;
                        game.playerDamageTicks = game.DAMAGE_DISPLAY_DURATION;
                        p.cp = Math.min(p.cp + 5, p.maxCp);
                    } else if (target instanceof Companion) {
                        Companion c = (Companion) target;
                        dmg = rollDamage(enemyPatk, c.pdef);
                        c.hp = Math.max(0, c.hp - dmg);
                        c.cp = Math.min(c.cp + 5, c.maxCp);

                        for (int i = 0; i < game.companions.size(); i++) {
                            if (c == game.companions.get(i)) {
                                game.companionTakingDamage[i] = game.DAMAGE_DISPLAY_DURATION;
                                break;
                            }
                        }

                        if (c.hp <= 0) {
                            JOptionPane.showMessageDialog(game, c.name + "被擊敗！");
                        }
                    }

                    if (areAllAlliesDefeated()) {
                        handlePartyDefeatReturnToMainMenu();
                        game.repaint();
                        return;
                    }

                    completeActionAndRefreshBattleOrder(20);
                }
            }
            game.repaint();
        } else if (!game.animating && game.currentEnemies.size() > 0) {
            removeInvisibleBattleUnits();
            sortBattleOrderByAt();
            game.currentActionIndex = 0;

            if (game.currentActionIndex < game.battleOrder.size()) {
                BattleUnit currentUnit = game.battleOrder.get(0);

                if (currentUnit.unit instanceof Player || currentUnit.unit instanceof Companion) {
                    if (currentUnit.unit instanceof Player && ((Player) currentUnit.unit).hp <= 0) {
                        removeInvisibleBattleUnits();
                        sortBattleOrderByAt();
                        game.currentActionIndex = 0;
                        return;
                    }
                    if (currentUnit.unit instanceof Companion && ((Companion) currentUnit.unit).hp <= 0) {
                        removeInvisibleBattleUnits();
                        sortBattleOrderByAt();
                        game.currentActionIndex = 0;
                        return;
                    }

                    if (!game.waitingForPlayerDecision) {
                        game.currentActor = currentUnit.unit;
                        game.waitingForPlayerDecision = true;
                    }
                } else if (currentUnit.unit instanceof Enemy) {
                    boolean enemyStillFighting = false;
                    for (Enemy enemy : game.currentEnemies) {
                        if (enemy == currentUnit.unit) {
                            enemyStillFighting = true;
                            break;
                        }
                    }

                    if (!enemyStillFighting) {
                        removeInvisibleBattleUnits();
                        sortBattleOrderByAt();
                        game.currentActionIndex = 0;
                        return;
                    }

                    Enemy enemy = (Enemy) currentUnit.unit;
                    double hpRatio = enemy.hp / (double) enemy.maxHp;
                    double fleeChance = 0;
                    if (hpRatio < 0.5) {
                        fleeChance = (0.5 - hpRatio) / 0.5 * 0.55;
                    }
                    if (Math.random() < fleeChance) {
                        enemy.defeated = true;
                        enemy.setBattleLock(5000);
                        removeEnemy(enemy);

                        if (game.currentEnemies.isEmpty()) {
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
                            game.currentActor = null;
                            game.waitingForPlayerDecision = false;
                            game.repaint();
                        } else {
                            removeInvisibleBattleUnits();
                            sortBattleOrderByAt();
                            game.currentActionIndex = 0;
                        }
                    } else {
                        chooseEnemyAttackTarget();
                        game.animating = true;
                        game.animAction = "enemyAttack";
                        game.animTicks = 0;
                        game.currentActor = currentUnit.unit;

                        game.currentAttackingEnemyIndex = -1;
                        for (int i = 0; i < game.currentEnemies.size(); i++) {
                            if (game.currentEnemies.get(i) == currentUnit.unit) {
                                game.currentAttackingEnemyIndex = getEnemyBattleSlot(i);
                                break;
                            }
                        }
                    }
                }
            }

            if (game.damagedEnemyTicks > 0) {
                game.damagedEnemyTicks--;
                if (game.damagedEnemyTicks == 0) {
                    game.damagedEnemyIndex = -1;
                }
            }
            if (game.playerDamageTicks > 0) {
                game.playerDamageTicks--;
                if (game.playerDamageTicks == 0) {
                    game.playerTakingDamage = false;
                }
            }
            if (game.playerHealGlowTicks > 0) {
                game.playerHealGlowTicks--;
            }
            for (int i = 0; i < game.companionTakingDamage.length; i++) {
                if (game.companionTakingDamage[i] > 0) {
                    game.companionTakingDamage[i]--;
                }
                if (game.companionHealGlowTicks[i] > 0) {
                    game.companionHealGlowTicks[i]--;
                }
            }

            game.repaint();
        }
    }

    void spawnRandomEnemies() {
        game.currentEnemies.clear();
        game.enemyBattleSlots.clear();
        game.originalBattleEnemies.clear();

        if (game.triggeredEnemy != null && !game.triggeredEnemy.isDefeated() && !game.triggeredEnemy.isBattleLocked()) {
            Enemy newEnemy = new Enemy((int) game.triggeredEnemy.x / 40, (int) game.triggeredEnemy.y / 40);
            game.triggeredEnemy.setBattleLock(Long.MAX_VALUE);
            game.originalBattleEnemies.add(game.triggeredEnemy);
            game.currentEnemies.add(newEnemy);
            game.enemyBattleSlots.add(game.enemyBattleSlots.size());
        }

        int additionalEnemyCount = (int) (Math.random() * 3);
        for (int i = 0; i < additionalEnemyCount; i++) {
            Enemy virtualEnemy = new Enemy((int) (Math.random() * 15), (int) (Math.random() * 10));
            game.currentEnemies.add(virtualEnemy);
            game.enemyBattleSlots.add(game.enemyBattleSlots.size());
        }

        initBattleOrder();
    }

    int getEnemyBattleSlot(int enemyIndex) {
        if (enemyIndex >= 0 && enemyIndex < game.enemyBattleSlots.size()) {
            return game.enemyBattleSlots.get(enemyIndex);
        }
        return enemyIndex;
    }

    void removeEnemyAt(int enemyIndex) {
        if (enemyIndex < 0 || enemyIndex >= game.currentEnemies.size()) {
            return;
        }
        Enemy removedEnemy = game.currentEnemies.remove(enemyIndex);
        if (enemyIndex < game.enemyBattleSlots.size()) {
            game.enemyBattleSlots.remove(enemyIndex);
        }
        if (removedEnemy != null) {
            for (int i = game.battleOrder.size() - 1; i >= 0; i--) {
                if (game.battleOrder.get(i).unit == removedEnemy) {
                    game.battleOrder.remove(i);
                }
            }
        }
    }

    void removeEnemy(Enemy enemy) {
        int idx = game.currentEnemies.indexOf(enemy);
        if (idx >= 0) {
            removeEnemyAt(idx);
        }
    }

    String generateDropsPreview() {
        double rand = Math.random();
        if (rand < 0.3) {
            return "小藥水 x1";
        } else if (rand < 0.6) {
            return "大藥水 x1";
        } else if (rand < 0.85) {
            int goldAmount = 30 + (int) (Math.random() * 40);
            return "金幣 x" + goldAmount;
        }
        return "";
    }

    void applyDropsFromPreview() {
        if (game.previewDrops.isEmpty()) {
            return;
        }

        if (game.previewDrops.contains("小藥水")) {
            game.player.smallPotions += 1;
        } else if (game.previewDrops.contains("大藥水")) {
            game.player.largePotions += 1;
        } else if (game.previewDrops.contains("金幣")) {
            int startIdx = game.previewDrops.indexOf("x") + 1;
            if (startIdx > 0) {
                try {
                    int goldAmount = Integer.parseInt(game.previewDrops.substring(startIdx).trim());
                    game.player.gold += goldAmount;
                } catch (NumberFormatException e) {
                    game.player.gold += 50;
                }
            }
        }
    }

    boolean isPlayerAlive() {
        return game.player != null && game.player.hp > 0;
    }

    boolean areAllAlliesDefeated() {
        if (isPlayerAlive()) {
            return false;
        }
        for (Companion c : game.companions) {
            if (c.hp > 0) {
                return false;
            }
        }
        return true;
    }

    int getCurrentSkillCasterPatk(Player fallbackPlayer) {
        if (game.currentActor instanceof Companion) {
            return ((Companion) game.currentActor).patk;
        }
        if (game.currentActor instanceof Player) {
            return ((Player) game.currentActor).patk;
        }
        if (fallbackPlayer != null) {
            return fallbackPlayer.patk;
        }
        return game.player != null ? game.player.patk : 1;
    }

    void dealFlatDamageToAllEnemies(int damage) {
        int finalDamage = Math.max(1, damage);
        for (Enemy enemy : game.currentEnemies) {
            enemy.hp = Math.max(0, enemy.hp - finalDamage);
            enemy.cp = Math.min(enemy.cp + 5, enemy.maxCp);
        }
    }

    void removeDefeatedEnemiesFromBattle() {
        for (int i = game.currentEnemies.size() - 1; i >= 0; i--) {
            Enemy enemy = game.currentEnemies.get(i);
            if (enemy.hp <= 0) {
                enemy.defeated = true;
                removeEnemyAt(i);
            }
        }

        if (game.targetingEnemyIndex >= game.currentEnemies.size()) {
            game.targetingEnemyIndex = game.currentEnemies.isEmpty() ? -1 : game.currentEnemies.size() - 1;
        }
    }

    boolean tryEnterVictorySettlementIfNoEnemies() {
        if (!game.currentEnemies.isEmpty()) {
            return false;
        }

        game.markBossDefeatedIfNeeded();
        for (Enemy origEnemy : game.originalBattleEnemies) {
            origEnemy.defeated = true;
        }
        game.originalBattleEnemies.clear();

        game.isZeroExpSettlement = false;
        int totalExp = 60;
        game.levelsGained = game.player.gainExp(totalExp / 2);
        for (int i = 0; i < game.companions.size(); i++) {
            Companion c = game.companions.get(i);
            game.companionLevelsGained[i] = c.gainExp(totalExp / 2);
            game.companionExpForNextLevel[i] = Math.max(0, c.expToNext - c.exp);
        }
        game.lastBattleExp = totalExp;
        game.expForNextLevel = Math.max(0, game.player.expToNext - game.player.exp);
        applyDropsFromPreview();
        game.settlementDrops = game.previewDrops.isEmpty() ? "" : "掉落物品: " + game.previewDrops;
        game.state = 2;
        game.playBattleEndMusic();
        game.animating = false;
        game.animTicks = 0;
        game.currentActor = null;
        game.waitingForPlayerDecision = false;
        game.repaint();
        return true;
    }

    void chooseEnemyAttackTarget() {
        game.enemyAttackTargetIsPlayer = true;
        game.enemyAttackTargetCompanionIndex = -1;

        List<Integer> aliveCompanionIndexes = new ArrayList<>();
        for (int i = 0; i < game.companions.size(); i++) {
            if (game.companions.get(i).hp > 0) {
                aliveCompanionIndexes.add(i);
            }
        }

        boolean playerAlive = isPlayerAlive();
        if (playerAlive && !aliveCompanionIndexes.isEmpty()) {
            if (Math.random() < 0.7) {
                game.enemyAttackTargetIsPlayer = true;
            } else {
                game.enemyAttackTargetIsPlayer = false;
                game.enemyAttackTargetCompanionIndex = aliveCompanionIndexes
                        .get((int) (Math.random() * aliveCompanionIndexes.size()));
            }
        } else if (playerAlive) {
            game.enemyAttackTargetIsPlayer = true;
        } else if (!aliveCompanionIndexes.isEmpty()) {
            game.enemyAttackTargetIsPlayer = false;
            game.enemyAttackTargetCompanionIndex = aliveCompanionIndexes
                    .get((int) (Math.random() * aliveCompanionIndexes.size()));
        }
    }

    Object getEnemyAttackTargetUnit() {
        if (game.enemyAttackTargetIsPlayer) {
            return isPlayerAlive() ? game.player : null;
        }
        if (game.enemyAttackTargetCompanionIndex >= 0 && game.enemyAttackTargetCompanionIndex < game.companions.size()) {
            Companion c = game.companions.get(game.enemyAttackTargetCompanionIndex);
            return c.hp > 0 ? c : null;
        }
        return null;
    }

    void healAllAlliesByPercent(double percent) {
        int playerHeal = (int) Math.ceil(game.player.maxHp * percent);
        game.player.heal(playerHeal);
        game.playerHealGlowTicks = game.HEAL_GLOW_DURATION;

        for (int i = 0; i < game.companions.size(); i++) {
            Companion c = game.companions.get(i);
            int companionHeal = (int) Math.ceil(c.maxHp * percent);
            c.heal(companionHeal);
            if (i < game.companionHealGlowTicks.length) {
                game.companionHealGlowTicks[i] = game.HEAL_GLOW_DURATION;
            }
        }
    }

    boolean isHealingSkillAnimation() {
        return game.animating && "playerAttack".equals(game.animAction) && game.selectedSkill instanceof SkillMoonlight;
    }

    void handlePartyDefeatReturnToMainMenu() {
        JOptionPane.showMessageDialog(game, "全隊被打敗了，返回主畫面。", "戰鬥失敗", JOptionPane.INFORMATION_MESSAGE);

        game.animating = false;
        game.animTicks = 0;
        game.animAction = "";
        game.currentActor = null;
        game.waitingForPlayerDecision = false;
        game.currentActionIndex = 0;
        game.currentAttackingEnemyIndex = -1;
        game.enemyAttackTargetIsPlayer = true;
        game.enemyAttackTargetCompanionIndex = -1;
        game.targetingEnemyIndex = -1;
        game.hoveredEnemyIndex = -1;
        game.damagedEnemyIndex = -1;
        game.damagedEnemyTicks = 0;
        game.playerTakingDamage = false;
        game.playerDamageTicks = 0;
        game.showingFleeMessage = false;
        game.fleeMessage = "";
        game.selectingTargetMode = "";
        game.selectingPotionType = "";
        game.selectedSkill = null;

        game.currentEnemies.clear();
        game.enemyBattleSlots.clear();
        game.originalBattleEnemies.clear();
        game.battleOrder.clear();
        game.triggeredEnemy = null;
        game.previewDrops = "";
        game.settlementDrops = "";

        game.stopCurrentMusic();
        game.state = -1;
    }

    int rollDamage(int atk, int def) {
        int base = Math.max(1, atk - def);
        double factor = 0.95 + Math.random() * 0.10;
        return Math.max(1, (int) Math.round(base * factor));
    }

    void initBattleOrder() {
        game.battleOrder.clear();
        game.currentActionIndex = 0;

        BattleUnit playerUnit = new BattleUnit(game.player.battleSpeed, true, game.player);
        game.battleOrder.add(playerUnit);

        for (Companion c : game.companions) {
            BattleUnit companionUnit = new BattleUnit(c.battleSpeed, true, c);
            game.battleOrder.add(companionUnit);
        }

        for (Enemy enemy : game.currentEnemies) {
            BattleUnit enemyUnit = new BattleUnit(enemy.speed, false, enemy);
            game.battleOrder.add(enemyUnit);
        }

        sortBattleOrderByAt();
    }

    void sortBattleOrderByAt() {
        game.battleOrder.sort((a, b) -> {
            int cmp = Double.compare(a.at, b.at);
            if (cmp != 0) {
                return cmp;
            }
            cmp = Integer.compare(b.speed, a.speed);
            if (cmp != 0) {
                return cmp;
            }
            return Integer.compare(System.identityHashCode(a), System.identityHashCode(b));
        });
    }

    void removeInvisibleBattleUnits() {
        for (int i = game.battleOrder.size() - 1; i >= 0; i--) {
            if (!isBattleUnitVisible(game.battleOrder.get(i))) {
                game.battleOrder.remove(i);
            }
        }
    }

    BattleUnit findBattleUnit(Object unit) {
        if (unit == null) {
            return null;
        }
        for (BattleUnit battleUnit : game.battleOrder) {
            if (battleUnit.unit == unit) {
                return battleUnit;
            }
        }
        return null;
    }

    void completeActionAndRefreshBattleOrder(int actionCost) {
        BattleUnit actedUnit = findBattleUnit(game.currentActor);
        if (actedUnit != null) {
            actedUnit.addActionCost(actionCost);
        }

        removeInvisibleBattleUnits();
        sortBattleOrderByAt();
        game.currentActionIndex = 0;
        game.animating = false;
        game.animTicks = 0;
        game.animAction = "";
        game.currentActor = null;
        game.waitingForPlayerDecision = false;
        game.currentAttackingEnemyIndex = -1;
        game.enemyAttackTargetIsPlayer = true;
        game.enemyAttackTargetCompanionIndex = -1;
    }

    int getPendingActionCostForPreview() {
        if ("enemyAttack".equals(game.animAction)) {
            return 20;
        }
        if (game.selectedSkill != null) {
            return 40;
        }
        if (!game.selectingPotionType.isEmpty()) {
            return 10;
        }
        if ("attack".equals(game.selectingTargetMode)) {
            return 20;
        }
        if ("skill".equals(game.selectingTargetMode)) {
            return 40;
        }
        if ("potion".equals(game.selectingTargetMode)) {
            return 10;
        }
        if (game.showingFleeMessage) {
            return 30;
        }
        return 20;
    }

    int getProjectedBattleOrderIndex(Object unit, int actionCost) {
        if (unit == null || game.battleOrder.isEmpty()) {
            return -1;
        }

        List<BattleUnit> projectedOrder = new ArrayList<>();
        BattleUnit projectedUnit = null;
        for (BattleUnit battleUnit : game.battleOrder) {
            BattleUnit copy = new BattleUnit(battleUnit.speed, battleUnit.isPlayer, battleUnit.unit);
            copy.at = battleUnit.at;
            projectedOrder.add(copy);
            if (battleUnit.unit == unit) {
                projectedUnit = copy;
            }
        }

        if (projectedUnit == null) {
            return -1;
        }

        projectedUnit.addActionCost(actionCost);
        projectedOrder.sort((a, b) -> {
            int cmp = Double.compare(a.at, b.at);
            if (cmp != 0) {
                return cmp;
            }
            cmp = Integer.compare(b.speed, a.speed);
            if (cmp != 0) {
                return cmp;
            }
            return Integer.compare(System.identityHashCode(a), System.identityHashCode(b));
        });

        for (int i = 0; i < projectedOrder.size(); i++) {
            if (projectedOrder.get(i).unit == unit) {
                return i;
            }
        }

        return -1;
    }

    List<Integer> buildVisibleBattleOrderIndices() {
        List<Integer> result = new ArrayList<>();
        for (int i = 0; i < game.battleOrder.size(); i++) {
            if (isBattleUnitVisible(game.battleOrder.get(i))) {
                result.add(i);
            }
        }
        return result;
    }

    boolean isBattleOrderPreviewActive() {
        if (game.currentActor == null) {
            return false;
        }
        if (game.animating) {
            return true;
        }
        if (!game.selectingTargetMode.isEmpty()) {
            if ("attack".equals(game.selectingTargetMode)) {
                return game.hoveredEnemyIndex >= 0 && game.hoveredEnemyIndex < game.currentEnemies.size();
            }
            if ("skill".equals(game.selectingTargetMode)) {
                return game.hoveredEnemyIndex >= 0 && game.hoveredEnemyIndex < game.currentEnemies.size();
            }
            if ("potion".equals(game.selectingTargetMode)) {
                return game.hoveredEnemyIndex >= 0 && game.hoveredEnemyIndex < game.currentEnemies.size();
            }
        }
        return false;
    }

    boolean isBattleUnitVisible(BattleUnit unit) {
        if (unit == null || unit.unit == null) {
            return false;
        }

        if (unit.unit instanceof Player) {
            return game.player != null && game.player.hp > 0;
        }

        if (unit.unit instanceof Companion) {
            return ((Companion) unit.unit).hp > 0;
        }

        if (unit.unit instanceof Enemy) {
            Enemy enemy = (Enemy) unit.unit;
            return enemy.hp > 0 && game.currentEnemies.contains(enemy);
        }

        return false;
    }

    void executeAttackAnimation() {
        game.animating = true;
        if (game.currentActor instanceof Companion) {
            game.animAction = "companionAttack";
        } else {
            game.animAction = "playerAttack";
        }
        game.animTicks = 0;
        game.selectedSkill = null;
        game.waitingForPlayerDecision = false;
        game.selectingTargetMode = "";
        game.repaint();
    }

    void executeSkillAnimation(Object target) {
        if (game.selectedSkill == null) {
            return;
        }

        if (target instanceof Enemy) {
            game.targetingEnemyIndex = game.currentEnemies.indexOf((Enemy) target);
        }

        game.animating = true;
        game.animAction = "playerAttack";
        game.animTicks = 0;
        game.waitingForPlayerDecision = false;
        game.selectingTargetMode = "";
        game.repaint();
    }

    int findClosestEnemyIndexAt(int mouseX, int mouseY, int hitRadius) {
        int nearestIndex = -1;
        double nearestDist = Double.MAX_VALUE;

        for (int i = 0; i < game.currentEnemies.size(); i++) {
            double dx = mouseX - game.battleScreenEnemyX[i];
            double dy = mouseY - game.battleScreenEnemyY[i];
            double dist = Math.hypot(dx, dy);
            if (dist <= hitRadius && dist < nearestDist) {
                nearestDist = dist;
                nearestIndex = i;
            }
        }

        return nearestIndex;
    }

    void executePotionAnimation(Object target) {
        if (game.selectingPotionType.isEmpty()) {
            return;
        }

        Object currentCharacter = game.currentActor;
        int healAmount = 0;

        if ("small".equals(game.selectingPotionType)) {
            healAmount = 40;
        } else if ("large".equals(game.selectingPotionType)) {
            healAmount = 80;
        }

        if (target instanceof Player) {
            ((Player) target).heal(healAmount);
        } else if (target instanceof Companion) {
            ((Companion) target).heal(healAmount);
        }

        if (currentCharacter instanceof Player) {
            Player p = (Player) currentCharacter;
            if ("small".equals(game.selectingPotionType)) {
                p.smallPotions = Math.max(0, p.smallPotions - 1);
            } else if ("large".equals(game.selectingPotionType)) {
                p.largePotions = Math.max(0, p.largePotions - 1);
            }
        }

        completeActionAndRefreshBattleOrder(10);
        game.selectingTargetMode = "";
        game.selectingPotionType = "";
        game.repaint();
    }

    void showSkillMenuForTargeting() {
        Object currentCharacter = game.currentActor;
        if (currentCharacter == null) {
            currentCharacter = game.player;
        }

        List<Skill> skillList;

        if (currentCharacter instanceof Player) {
            skillList = ((Player) currentCharacter).skills;
        } else if (currentCharacter instanceof Companion) {
            skillList = ((Companion) currentCharacter).skills;
        } else {
            JOptionPane.showMessageDialog(game, "無法使用戰技！");
            return;
        }

        if (skillList.isEmpty()) {
            JOptionPane.showMessageDialog(game, "還沒有戰技可使用！");
            return;
        }

        String[] skillNames = new String[skillList.size()];
        for (int i = 0; i < skillList.size(); i++) {
            Skill skill = skillList.get(i);
            skillNames[i] = skill.name + " - " + skill.description;
        }

        JList<String> skillListUI = new JList<>(skillNames);
        skillListUI.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        skillListUI.setSelectedIndex(0);
        skillListUI.setFont(new java.awt.Font("Microsoft JhengHei", java.awt.Font.PLAIN, 14));
        skillListUI.setFixedCellHeight(30);
        skillListUI.setVisibleRowCount(Math.min(8, skillNames.length));

        JScrollPane scrollPane = new JScrollPane(skillListUI);
        scrollPane.setPreferredSize(new java.awt.Dimension(400, 250));
        int result = JOptionPane.showConfirmDialog(game, scrollPane, "選擇戰技", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION && skillListUI.getSelectedIndex() >= 0) {
            int choice = skillListUI.getSelectedIndex();
            Skill skill = skillList.get(choice);

            if (currentCharacter instanceof Player) {
                Player p = (Player) currentCharacter;
                if (p.cp < skill.cpCost) {
                    JOptionPane.showMessageDialog(game, "CP 不足！需要 " + skill.cpCost + " CP，現有 " + p.cp + " CP。");
                    return;
                }
            } else if (currentCharacter instanceof Companion) {
                Companion c = (Companion) currentCharacter;
                if (c.cp < skill.cpCost) {
                    JOptionPane.showMessageDialog(game, "CP 不足！需要 " + skill.cpCost + " CP，現有 " + c.cp + " CP。");
                    return;
                }
            }

            game.selectedSkill = skill;
            game.selectingTargetMode = "skill";
            game.repaint();
        }
    }

    void showPotionMenuForTargeting() {
        Object currentCharacter = game.currentActor;
        if (currentCharacter == null) {
            currentCharacter = game.player;
        }

        int smallPotions = 0;
        int largePotions = 0;

        if (currentCharacter instanceof Player) {
            Player p = (Player) currentCharacter;
            smallPotions = p.smallPotions;
            largePotions = p.largePotions;
        } else if (currentCharacter instanceof Companion) {
            smallPotions = game.player.smallPotions;
            largePotions = game.player.largePotions;
        }

        if (smallPotions + largePotions <= 0) {
            JOptionPane.showMessageDialog(game, "沒有可用的藥水！");
            return;
        }

        Object[] options = { "小藥 (+40)", "大藥 (+80)", "取消" };
        int choice = JOptionPane.showOptionDialog(game,
                "選擇藥水種類", "藥水",
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
                null, options, options[0]);

        if (choice == 0 && smallPotions > 0) {
            game.selectingPotionType = "small";
            game.selectingTargetMode = "potion";
            game.repaint();
        } else if (choice == 1 && largePotions > 0) {
            game.selectingPotionType = "large";
            game.selectingTargetMode = "potion";
            game.repaint();
        }
    }
}
