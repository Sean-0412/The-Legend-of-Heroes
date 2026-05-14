public class SkillSaintBlood extends Skill {
    SkillSaintBlood() {
        super("聖依血", "將敵人血量扣到1", 20);
    }

    @Override
    void execute(GamePanel gamePanel, Player player, Enemy enemy) {
        if (enemy != null) {
            enemy.hp = 1;
        }
    }
}
