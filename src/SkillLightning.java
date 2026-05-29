package src;

public class SkillLightning extends Skill {
    SkillLightning() {
        super("雷擊術", "以魔法雷擊單一敵人", 22);
    }

    @Override
    void execute(GamePanel gamePanel, Player player, Enemy enemy) {
        if (enemy == null) {
            return;
        }

        int casterMatk = gamePanel.getCurrentSkillCasterMatk(player);
        int damage = Math.max(1, (int) Math.round(casterMatk * (1.2 + 0.2 * (level - 1))) - enemy.mdef);
        enemy.hp = Math.max(0, enemy.hp - damage);
        enemy.cp = Math.min(enemy.cp + 5, enemy.maxCp);
    }
}
