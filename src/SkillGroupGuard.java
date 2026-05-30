package src;

public class SkillGroupGuard extends Skill {
    SkillGroupGuard() {
        super("群體防護", "提升全隊士氣並回復少量HP", 28);
    }

    @Override
    void execute(GamePanel gamePanel, Player player, Enemy enemy) {
        gamePanel.healAllAlliesByPercent(0.10 + 0.05 * (level - 1));
    }
}
