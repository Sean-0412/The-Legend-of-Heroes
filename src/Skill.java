package src;

public abstract class Skill {
    String name;
    String description;
    int cpCost;  // 技能 CP 消耗

    Skill(String name, String description, int cpCost) {
        this.name = name;
        this.description = description;
        this.cpCost = cpCost;
    }

    abstract void execute(GamePanel gamePanel, Player player, Enemy enemy);
}
