/**
 * GameState.java
 * 說明: 定義或管理遊戲狀態相關結構（若包含常數或狀態邏輯在此維護）。
 */

package src;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * GameState 類別：用於保存和加載遊戲進度
 * 包含玩家狀態、隊友狀態、地圖信息、敵人擊敗狀態等
 */
public class GameState implements Serializable {
    private static final long serialVersionUID = 1L;

    // 玩家狀態
    public double playerX;
    public double playerY;
    public int playerLevel;
    public int playerExp;
    public int playerHP;
    public int playerMana;
    public int playerCP;
    public int playerMaxHP;
    public int playerMaxMana;
    public int playerMaxCP;
    public int playerPatk;
    public int playerPdef;
    public int playerMatk;
    public int playerMdef;
    public EquipmentItem playerWeapon;
    public EquipmentItem playerClothes;
    public EquipmentItem playerShoes;
    public EquipmentItem playerAccessory1;
    public EquipmentItem playerAccessory2;
    public List<EquipmentItem> playerEquipmentInventory;
    
    // 玩家背包物品
    public int smallPotions;
    public int largePotions;
    public int cpPotions;
    public int gold;
    
    // 隊友狀態（假設只有一個隊友）
    public double companionX;
    public double companionY;
    public int companionHP;
    public int companionMana;
    public int companionCP;
    public int companionMaxHP;
    public int companionMaxMana;
    public int companionMaxCP;
    public int companionPatk;
    public int companionPdef;
    public int companionMatk;
    public int companionMdef;
    public int companionLevel;
    public int companionExp;
    public EquipmentItem companionWeapon;
    public EquipmentItem companionClothes;
    public EquipmentItem companionShoes;
    public EquipmentItem companionAccessory1;
    public EquipmentItem companionAccessory2;
    
    // 地圖和敵人狀態
    public int currentMapIndex;
    public Map<String, Boolean> defeatedEnemies;
    public boolean bossCutscenePlayed;
    public boolean bossDefeated;
    
    // 保存時間戳記
    public long saveTime;
    
    /**
     * 建構子：初始化保存時間戳記和敵人字典
     */
    public GameState() {
        this.saveTime = System.currentTimeMillis();
        this.defeatedEnemies = new HashMap<>();
        this.playerEquipmentInventory = new ArrayList<>();
    }
    
    /**
     * 取得友好的保存時間字符串
     */
    public String getSaveTimeString() {
        return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date(saveTime));
    }
}
