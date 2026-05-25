package src;

import javax.swing.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.sound.sampled.*;

class GamePanel extends JPanel implements ActionListener, KeyListener, MouseListener, MouseMotionListener, MouseWheelListener {
    private javax.swing.Timer timer;
    private Player player;
    private java.util.List<Enemy> enemies;
    private int[][] map;
    private final int TILE_SIZE = 40;
    private final double BATTLE_PLAYER_SCALE = 1.0;

    // multi-map support
    private int mapIndex = 0;
    private int[][][] allMaps;
    private java.util.List<java.util.List<Enemy>> allEnemies;

    // 小遊戲狀態：-1 開始菜單, 0 普通遊戲, 1 戰鬥中, 2 戰鬥結算
    private int state = -1;  // 初始為開始菜單
    private java.util.List<Enemy> currentEnemies = new java.util.ArrayList<>();  // 當前戰鬥中的敵人列表
    private java.util.List<Enemy> originalBattleEnemies = new java.util.ArrayList<>();  // 參與當前戰鬥的原始敵人列表（用於逃跑時標記）
    private Enemy triggeredEnemy = null;  // 觸發戰鬥的敵人
    
    // 開始菜單選項
    private int selectedMainMenuOption = 0;  // 0 = NEW, 1 = LOAD, 2 = EXIT
    private Rectangle newGameRect, loadGameRect, exitRect;
    private boolean showLoadMenu = false;  // 是否顯示加載菜單
    private int selectedLoadSlot = 0;  // 加載菜單中選中的槽位
    // private String battleMessage = ""; // 戰鬥中的提示文字

    // 結算畫面資訊
    private int lastBattleExp = 0; // 本場獲得的經驗值
    private int expForNextLevel = 0; // 升級還需要的經驗值
    private int levelsGained = 0; // 玩家本場升級次數
    private int[] companionLevelsGained = new int[4];  // 伴侶本場升級次數（最多4個）
    private int[] companionExpForNextLevel = new int[4];  // 伴侶升級還需要的經驗值
    private String settlementDrops = ""; // 掉落物品文字
    private String previewDrops = ""; // 戰鬥中預覽的掉落物品文字
    private boolean isZeroExpSettlement = false; // 標記是否為敵人逃跑的無經驗結算
    
    // 逃跑結果訊息
    private String fleeMessage = ""; // 逃跑結果訊息（成功或失敗）
    private boolean showingFleeMessage = false; // 是否顯示逃跑訊息

    // 地圖介面選單
    private Rectangle menuRect;
    private Rectangle mapMenuPanelRect;
    private Rectangle statusTabRect;
    private Rectangle bagTabRect;
    private Rectangle saveTabRect;
    private Rectangle statusPlayerSelectorRect;
    private Rectangle[] statusCompanionSelectorRects = new Rectangle[4];
    private boolean showMapMenu = false;
    private int selectedMapTab = 0;  // 0=狀態, 1=背包, 2=保存
    private int hoveredMapTab = -1;
    private int selectedStatusActor = -1; // -1=玩家, 0..n=隊友
    private int selectedSaveSlot = 0;  // 當前選中的保存槽位
    private int bagScrollOffset = 0;  // 背包滾動偏移
    private final int BAG_VISIBLE_ITEMS = 8;  // 顯示項目數
    private final int MAX_SAVE_SLOTS = 3;  // 最多3個存檔槽位
    private static final String SAVE_DIR_NAME = "saves";
    private static final String SAVE_FILE_PREFIX = "savegame_";
    private static final String SAVE_FILE_EXT = ".dat";

    // 戰鬥動畫
    private boolean animating = false;
    private int animTicks = 0;
    private final int ANIM_DURATION = 60; // 幀數，約2秒（移動0.5秒 + 等待1秒 + 返回0.5秒）
    private String animAction = ""; // "playerAttack", "waitBeforeFlee" 或 "enemyAttack"
    private Skill selectedSkill = null; // 當前選擇的戰技（null 表示普通攻擊）
    private String selectingTargetMode = "";  // 目標選擇模式: "", "attack", "skill", "potion"
    private String selectingPotionType = "";  // 選擇的藥物類型: "small" 或 "large"（在potion模式中使用）
    private int targetingEnemyIndex = -1;  // 目標敵人的索引，用於動畫調整
    private int currentAttackingEnemyIndex = -1;  // 當前攻擊敵人的索引（敵人攻擊時使用）
    private boolean enemyAttackTargetIsPlayer = true;  // 敵人本次攻擊是否以玩家為目標
    private int enemyAttackTargetCompanionIndex = -1;  // 敵人本次攻擊的隊友目標索引
    private java.util.List<Integer> enemyBattleSlots = new java.util.ArrayList<>();  // 敵人的固定站位槽位
    private int[] battleScreenEnemyX = new int[10];  // 敵人在戰鬥螢幕上的X坐標（支持最多10個敵人）
    private int[] battleScreenEnemyY = new int[10];  // 敵人在戰鬥螢幕上的Y坐標（支持最多10個敵人）
    private int hoveredEnemyIndex = -1;  // 鼠標懸停的敵人索引，-1表示無敵人懸停
    private int damagedEnemyIndex = -1;  // 受傷的敵人索引，用於顯示傷害血條
    private int damagedEnemyTicks = 0;  // 受傷敵人血條顯示計時
    private final int DAMAGE_DISPLAY_DURATION = 60; // 傷害血條顯示時長（幀數）
    private final int HEAL_GLOW_DURATION = 40; // 治療高亮時長（幀數）
    private boolean playerTakingDamage = false;  // 玩家是否正在受傷
    private int playerDamageTicks = 0;  // 玩家受傷血條顯示計時
    private int[] companionTakingDamage = new int[4];  // 隊友是否正在受傷（使用計時值）
    private int playerHealGlowTicks = 0;  // 玩家治療發光計時
    private int[] companionHealGlowTicks = new int[4];  // 隊友治療發光計時
    private int battleScreenPlayerX = 0;  // 玩家在戰鬥螢幕上的X坐標
    private int battleScreenPlayerY = 0;  // 玩家在戰鬥螢幕上的Y坐標
    private int[] battleScreenCompanionX = new int[4];  // 隊友在戰鬥螢幕上的X坐標
    private int[] battleScreenCompanionY = new int[4];  // 隊友在戰鬥螢幕上的Y坐標
    private BufferedImage battlePlayerSprite;

    // 地圖玩家圖片
    private BufferedImage playerIdleRight;
    private BufferedImage playerIdleLeft;
    private BufferedImage playerWalkRight1;
    private BufferedImage playerWalkRight2;
    private BufferedImage playerWalkLeft1;
    private BufferedImage playerWalkLeft2;
    private BufferedImage statusPortraitImage;

    // 玩家目前面向方向：true = 右，false = 左
    private boolean playerFacingRight = true;

    // 走路動畫計時
    private int playerWalkAnimTick = 0;

    // 數字越小，左右腳切換越快
    private final int PLAYER_WALK_FRAME_DELAY = 8;

    // 當前行動角色
    private Object currentActor = null; // 當前正在行動的角色（Player 或 Companion）
    private boolean waitingForPlayerDecision = false; // 是否等待玩家決定行動

    // 戰鬥按鈕區域
    private Rectangle attackRect;
    private Rectangle healRect;
    private Rectangle skillRect;
    private Rectangle runRect;

    private boolean mouseDown = false; // 是否按住滑鼠
    private int mouseGridX, mouseGridY; // 滑鼠所在的格子
    private boolean keyDown = false; // 有方向鍵在按下

    // 音樂播放
    private Clip bgmClip;
    private String currentMusicFile = "";

    // 行動順序系統
    private java.util.List<BattleUnit> battleOrder = new java.util.ArrayList<>();
    private int currentActionIndex = 0;

    // 隊友系統
    private java.util.List<Companion> companions = new java.util.ArrayList<>();
    
    // 玩家路徑追蹤系統（用於隊友跟隨）
    private java.util.LinkedList<double[]> playerPathHistory = new java.util.LinkedList<>();

    public GamePanel() {
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);
        addMouseListener(this);
        addMouseMotionListener(this);
        addMouseWheelListener(this);
        
        // 確保獲取焦點
        setRequestFocusEnabled(true);

        initMaps();
        player = new Player(2 * TILE_SIZE, 2 * TILE_SIZE);
        // 初始化隊友
        companions.add(new Companion(3 * TILE_SIZE, 2 * TILE_SIZE, "月"));
        loadPlayerSprites();

        timer = new javax.swing.Timer(30, this); // 約33FPS
        timer.start();
        
        // 延遲後請求焦點
        javax.swing.SwingUtilities.invokeLater(() -> {
            requestFocusInWindow();
        });

        updateMapMusic();
    }

    private void loadPlayerSprites() {
        playerIdleRight = loadSprite("正右.png");
        playerIdleLeft = loadSprite("正左.png");

        playerWalkRight1 = loadSprite("右1.png");
        playerWalkRight2 = loadSprite("右2.png");

        playerWalkLeft1 = loadSprite("左1.png");
        playerWalkLeft2 = loadSprite("左2.png");

        statusPortraitImage = loadSprite("正面立繪.png");

        // 戰鬥畫面先用正右，也可以之後改成依方向切換
        battlePlayerSprite = playerIdleRight;
    }

    private BufferedImage loadSprite(String fileName) {
        try {
            File spriteFile = new File("resources" + File.separator + fileName);
            if (spriteFile.exists()) {
                return ImageIO.read(spriteFile);
            } else {
                System.out.println("找不到圖片：" + spriteFile.getPath());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private BufferedImage getCurrentPlayerSprite() {
        boolean movingRight = player.vx > 0;
        boolean movingLeft = player.vx < 0;

        // 向右走：右1 > 右2 > 右1 > 右2...
        if (movingRight) {
            playerFacingRight = true;
            if ((playerWalkAnimTick / PLAYER_WALK_FRAME_DELAY) % 2 == 0) {
                return playerWalkRight1;
            } else {
                return playerWalkRight2;
            }
        }

        // 向左走：左1 > 左2 > 左1 > 左2...
        if (movingLeft) {
            playerFacingRight = false;
            if ((playerWalkAnimTick / PLAYER_WALK_FRAME_DELAY) % 2 == 0) {
                return playerWalkLeft1;
            } else {
                return playerWalkLeft2;
            }
        }

        // 停下來：依照最後面向方向，變成正右或正左
        return playerFacingRight ? playerIdleRight : playerIdleLeft;
    }

    private void updatePlayerWalkAnimation() {
        if (player.vx != 0) {
            playerWalkAnimTick++;
        } else {
            playerWalkAnimTick = 0;
        }
    }

    private int getMapPlayerDrawSize(BufferedImage sprite, int baseSize) {
        if (sprite == playerIdleRight
                || sprite == playerWalkRight1
                || sprite == playerWalkRight2) {
            return (int) Math.round(baseSize * 1.8);
        }
        if (sprite == playerIdleLeft
                || sprite == playerWalkLeft1
                || sprite == playerWalkLeft2) {
            return (int) Math.round(baseSize * 1.44);
        }
        return baseSize;
    }

    private BufferedImage getBattlePlayerSprite(int moveDir) {
        if (moveDir > 0) {
            playerFacingRight = true;
            return (playerWalkAnimTick / PLAYER_WALK_FRAME_DELAY) % 2 == 0
                    ? playerWalkRight1
                    : playerWalkRight2;
        }
        if (moveDir < 0) {
            playerFacingRight = false;
            return (playerWalkAnimTick / PLAYER_WALK_FRAME_DELAY) % 2 == 0
                    ? playerWalkLeft1
                    : playerWalkLeft2;
        }

        playerFacingRight = true;
        return playerIdleRight;
    }

    private void updatePlayerWalkAnimationForBattle(boolean moving) {
        if (moving) {
            playerWalkAnimTick++;
        } else {
            playerWalkAnimTick = 0;
        }
    }

    private void initMaps() {
        allMaps = new int[3][][];

        // ------- 地圖 0：草原 -------
        allMaps[0] = new int[15][20];
        for (int y = 0; y < 15; y++)
            for (int x = 0; x < 20; x++) {
                boolean border = (x == 0 || y == 0 || x == 19 || y == 14);
                // 右邊開傳送門入口（行 7-8）
                boolean portal = (x == 19 && y >= 7 && y <= 8);
                allMaps[0][y][x] = (border && !portal) ? 1 : 0;
            }
        allMaps[0][4][4] = 1;
        allMaps[0][4][5] = 1;
        allMaps[0][8][10] = 1;
        allMaps[0][3][10] = 1;
        allMaps[0][3][11] = 1;
        allMaps[0][3][12] = 1;
        allMaps[0][10][3] = 1;
        allMaps[0][10][4] = 1;

        // ------- 地圖 1：地下城 -------
        allMaps[1] = new int[15][20];
        for (int y = 0; y < 15; y++)
            for (int x = 0; x < 20; x++) {
                boolean border = (x == 0 || y == 0 || x == 19 || y == 14);
                // 左邊開傳送門出口（行 7-8）
                boolean portalLeft = (x == 0 && y >= 7 && y <= 8);
                // 右邊開傳送門出口（行 7-8）
                boolean portalRight = (x == 19 && y >= 7 && y <= 8);
                allMaps[1][y][x] = (border && !portalLeft && !portalRight) ? 1 : 0;
            }
        // 地下城內部墙壁
        allMaps[1][3][5] = 1;
        allMaps[1][3][6] = 1;
        allMaps[1][3][7] = 1;
        allMaps[1][6][12] = 1;
        allMaps[1][6][13] = 1;
        allMaps[1][6][14] = 1;
        allMaps[1][9][8] = 1;
        allMaps[1][10][8] = 1;
        allMaps[1][11][8] = 1;
        allMaps[1][5][3] = 1;
        allMaps[1][5][4] = 1;
        allMaps[1][11][15] = 1;
        allMaps[1][12][15] = 1;

        // ------- 地圖 2：魔王殿 -------
        allMaps[2] = new int[15][20];
        for (int y = 0; y < 15; y++)
            for (int x = 0; x < 20; x++) {
                boolean border = (x == 0 || y == 0 || x == 19 || y == 14);
                // 左邊開傳送門出口（行 7-8）
                boolean portalLeft = (x == 0 && y >= 7 && y <= 8);
                allMaps[2][y][x] = (border && !portalLeft) ? 1 : 0;
            }
        // 魔王殿內部牆壁（環繞王座區）
        for (int x = 6; x <= 13; x++) {
            allMaps[2][4][x] = 1;
            allMaps[2][10][x] = 1;
        }
        for (int y = 5; y <= 9; y++) {
            allMaps[2][y][6] = 1;
            allMaps[2][y][13] = 1;
        }
        // 王座前障礙
        allMaps[2][8][9] = 1;
        allMaps[2][8][10] = 1;
        // 牆壁開口
        allMaps[2][7][6] = 0;
        allMaps[2][7][13] = 0;

        // ------- 敵人列表 -------
        allEnemies = new java.util.ArrayList<>();
        allEnemies.add(spawnEnemiesForMap(0));
        allEnemies.add(spawnEnemiesForMap(1));
        allEnemies.add(spawnEnemiesForMap(2));

        map = allMaps[0];
        enemies = allEnemies.get(0);
    }

    private java.util.List<Enemy> spawnEnemiesForMap(int idx) {
        java.util.List<Enemy> list = new java.util.ArrayList<>();
        if (idx == 0) {
            list.add(new Enemy(5, 3));
            list.add(new Enemy(7, 7));
        } else if (idx == 1) {
            list.add(new Enemy(10, 4));
            list.add(new Enemy(15, 8));
            list.add(new Enemy(12, 11));
        } else if (idx == 2) {
            Enemy boss = new Enemy(10, 7, "魔王", 12, 520, 70, 24, 20, 320, 28, true);
            boss.moveSpeed = 1.2;
            boss.roamRadius = 3 * 40;
            boss.detectRange = 6 * 40;
            boss.chaseLoseRange = 8 * 40;
            list.add(boss);
        }
        return list;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // 非戰鬥狀態時清除敵人懸停索引
        if (state != 1) {
            hoveredEnemyIndex = -1;
        }

        // 加載菜單（優先於主菜單）
        if (showLoadMenu) {
            drawLoadMenu(g2d);
            return;
        }
        
        // 開始菜單
        if (state == -1) {
            drawMainMenu(g2d);
            return;
        }

        // 若目前正在戰鬥結算，畫結算畫面
        if (state == 2) {
            g2d.setColor(new Color(0, 0, 0, 200));
            g2d.fillRect(0, 0, getWidth(), getHeight());

            Font chFont = new Font("Microsoft JhengHei", Font.BOLD, 24);
            if (!chFont.canDisplay('漢')) {
                chFont = new Font(Font.DIALOG, Font.BOLD, 24);
            }
            
            int centerX = getWidth() / 2;
            int centerY = getHeight() / 2;
            
            // 標題
            g2d.setFont(chFont);
            g2d.setColor(Color.YELLOW);
            if (isZeroExpSettlement) {
                g2d.drawString("敵人逃脫", centerX - 60, centerY - 200);
                g2d.setColor(Color.CYAN);
                g2d.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 18));
                g2d.drawString("敵人趁其不備逃脫了...", centerX - 120, centerY - 150);
            } else {
                g2d.drawString("戰鬥結束", centerX - 60, centerY - 200);
                
                // 獲得經驗值
                g2d.setColor(Color.CYAN);
                g2d.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 18));
                g2d.drawString("獲得經驗值: " + lastBattleExp, centerX - 80, centerY - 140);
                
                // 中間顯示角色資訊
                int charSpacing = 200;
                int charX1 = centerX - charSpacing;
                int charX2 = centerX + charSpacing;
                int charInfoY = centerY - 50;
                
                // 玩家信息
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Microsoft JhengHei", Font.BOLD, 20));
                g2d.drawString(player.name, charX1 - 30, charInfoY);
                
                g2d.setColor(new Color(150, 200, 255));
                g2d.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 16));
                String playerLevelStr = "等級: " + player.level;
                if (levelsGained > 0) {
                    playerLevelStr += " (+" + levelsGained + ")";
                }
                g2d.drawString(playerLevelStr, charX1 - 40, charInfoY + 40);
                
                g2d.setColor(new Color(100, 200, 255));
                g2d.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 14));
                g2d.drawString("Next level: " + expForNextLevel, charX1 - 60, charInfoY + 70);
                
                // 伴侶信息（第一個伴侶）
                if (companions.size() > 0) {
                    Companion c = companions.get(0);
                    g2d.setColor(Color.WHITE);
                    g2d.setFont(new Font("Microsoft JhengHei", Font.BOLD, 20));
                    g2d.drawString(c.name, charX2 - 30, charInfoY);
                    
                    g2d.setColor(new Color(150, 200, 100));
                    g2d.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 16));
                    String companionLevelStr = "等級: " + c.level;
                    if (companionLevelsGained[0] > 0) {
                        companionLevelStr += " (+" + companionLevelsGained[0] + ")";
                    }
                    g2d.drawString(companionLevelStr, charX2 - 40, charInfoY + 40);
                    
                    g2d.setColor(new Color(100, 180, 100));
                    g2d.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 14));
                    g2d.drawString("Next level: " + companionExpForNextLevel[0], charX2 - 60, charInfoY + 70);
                }
            }
            
            // 物品掉落
            if (!settlementDrops.isEmpty()) {
                g2d.setColor(new Color(200, 100, 200));
                g2d.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 16));
                g2d.drawString(settlementDrops, centerX - 100, centerY + 150);
            }

            // 提示按任意鍵或點擊繼續
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 14));
            g2d.drawString("按任意鍵或點擊畫面以繼續...", centerX - 120, getHeight() - 50);

            return;
        }

        // 若目前正在戰鬥，直接畫戰鬥畫面
        if (state == 1) {
            int centerX = getWidth() / 2;
            int centerY = getHeight() / 2;
            int playerX = centerX - 120;
            int playerY = centerY - 100;
            int companionX = centerX - 120;
            int companionY = centerY - 20;
            int enemyX = centerX + 120;

            // 背景半透明遮罩
            g2d.setColor(new Color(0, 0, 0, 200));
            g2d.fillRect(0, 0, getWidth(), getHeight());

            // 動畫偏移計算
            int offsetX = 0;
            int offsetY = 0;
            int moveDuration = 15;  // 移動時間
            int waitDuration = 30;  // 等待時間
            int returnDuration = 15; // 返回時間
            int targetOffsetX = 0;
            int targetOffsetY = 0;
            boolean healingSkillAnimation = isHealingSkillAnimation();
                boolean moonSliceAnimation = animating
                    && "playerAttack".equals(animAction)
                    && selectedSkill instanceof SkillMoonSlice;
            if (animating) {
                // 分三個階段：移動(15幀) + 等待(30幀) + 返回(15幀)
                
                // 根據當前行動者計算目標位置
                if (healingSkillAnimation) {
                    targetOffsetX = 0;
                    targetOffsetY = 0;
                } else if (moonSliceAnimation) {
                    if (currentActor instanceof Player) {
                        targetOffsetX = centerX - playerX;
                    } else if (currentActor instanceof Companion) {
                        targetOffsetX = centerX - companionX;
                    }
                    targetOffsetY = 0;
                } else if (currentActor instanceof Player) {
                    // 玩家走向敵人，停在敵人X-20的位置
                    targetOffsetX = enemyX - playerX - 20;
                } else if (currentActor instanceof Companion) {
                    // 隊友走向敵人，停在敵人X-20的位置
                    targetOffsetX = enemyX - companionX - 20;
                } else if (currentActor instanceof Enemy) {
                    // 敵人走向玩家/隊友，停在目標X+20的位置
                    int targetX = playerX;  // 預設目標是玩家
                    targetOffsetX = targetX - enemyX + 20;
                }
                
                // 如果有目標敵人或當前是敵人攻擊，計算Y座標
                if (!healingSkillAnimation && !moonSliceAnimation
                    && targetingEnemyIndex >= 0 && targetingEnemyIndex < currentEnemies.size()) {
                    int targetEnemySlot = getEnemyBattleSlot(targetingEnemyIndex);
                    int targetEnemyY = centerY - 80 + targetEnemySlot * 50;
                    if (currentActor instanceof Player) {
                        // 玩家走向敵人，需要到達敵人的Y座標
                        targetOffsetY = targetEnemyY - playerY;
                    } else if (currentActor instanceof Companion) {
                        // 隊友走向敵人，需要到達敵人的Y座標
                        targetOffsetY = targetEnemyY - (companionY + (companions.indexOf(currentActor) * 50));
                    }
                }
                
                // 如果是敵人攻擊，計算敵人向玩家走的偏移
                if (currentActor instanceof Enemy && currentAttackingEnemyIndex >= 0) {
                    int enemyStartY = centerY - 80;
                    int enemyInitialY = enemyStartY + currentAttackingEnemyIndex * 50;  // 敵人的初始Y座標（使用固定槽位）
                    int targetY = playerY;
                    if (!enemyAttackTargetIsPlayer
                            && enemyAttackTargetCompanionIndex >= 0
                            && enemyAttackTargetCompanionIndex < companions.size()) {
                        targetY = companionY + enemyAttackTargetCompanionIndex * 50;
                    }
                    targetOffsetY = targetY - enemyInitialY;  // 走向實際受擊者的Y座標
                }
                
                if (animTicks < moveDuration) {
                    // 移動階段：走向敵人
                    double progress = (double) animTicks / moveDuration;
                    offsetX = (int) (targetOffsetX * progress);
                    offsetY = (int) (targetOffsetY * progress);
                } else if (animTicks < moveDuration + waitDuration) {
                    // 等待階段：停在目標位置1秒
                    offsetX = targetOffsetX;
                    offsetY = targetOffsetY;
                } else {
                    // 返回階段：回到原始位置
                    int returnTicks = animTicks - moveDuration - waitDuration;
                    double progress = 1 - (double) returnTicks / returnDuration;
                    offsetX = (int) (targetOffsetX * progress);
                    offsetY = (int) (targetOffsetY * progress);
                }
            }

            int battleMoveDir = 0;
            if (animating
                    && currentActor instanceof Player
                    && !healingSkillAnimation
                    && ("playerAttack".equals(animAction) || "companionAttack".equals(animAction))) {
                int targetDir = Integer.compare(targetOffsetX, 0);
                if (animTicks < moveDuration) {
                    battleMoveDir = targetDir;
                } else if (animTicks >= moveDuration + waitDuration
                        && animTicks < moveDuration + waitDuration + returnDuration) {
                    battleMoveDir = -targetDir;
                }
            }

            // =============== 上方：敵人信息 ===============
            // 敵人信息已改成鼠標懸停時顯示在下方

            // =============== 中間：戰鬥動畫 ===============
            // 玩家
            int actualPlayerX = playerX;
            int actualPlayerY = playerY;
            boolean isPlayerDown = player.hp <= 0;
            if (currentActor instanceof Player
                    && !isPlayerDown
                    && !healingSkillAnimation
                    && ("playerAttack".equals(animAction) || "companionAttack".equals(animAction))) {
                // 根據目標敵人調整移動方向
                actualPlayerX = playerX + offsetX;
                actualPlayerY = playerY + offsetY;
            }

            // 繪製玩家
            boolean isPlayerActive = currentActor instanceof Player;
            double playerHealGlow = playerHealGlowTicks / (double) HEAL_GLOW_DURATION;
            if (healingSkillAnimation && currentActor instanceof Player) {
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
                BufferedImage battleSprite = getBattlePlayerSprite(battleMoveDir);
                if (battleSprite == null) {
                    battleSprite = battlePlayerSprite;
                }
                int spriteSize = (int) Math.round(getMapPlayerDrawSize(battleSprite, basePlayerSpriteSize)
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
                        // 高亮邊框
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
            // 更新玩家的戰鬥屏幕坐標（用於點擊檢測）
            battleScreenPlayerX = actualPlayerX;
            battleScreenPlayerY = actualPlayerY;
            
            // 顯示玩家受傷血條
            if (playerTakingDamage && playerDamageTicks > 0) {
                int barWidth = 50;
                int barHeight = 8;
                int barX = actualPlayerX - barWidth / 2;
                int barY = actualPlayerY - 30;
                
                // 背景
                g2d.setColor(Color.DARK_GRAY);
                g2d.fillRect(barX, barY, barWidth, barHeight);
                
                // 血條
                int hpWidth = (int) (barWidth * (player.hp / (double) player.maxHp));
                g2d.setColor(new Color(255, 165, 0));
                g2d.fillRect(barX, barY, hpWidth, barHeight);
                
                // 邊框
                g2d.setColor(Color.WHITE);
                g2d.setStroke(new BasicStroke(1.5f));
                g2d.drawRect(barX, barY, barWidth, barHeight);
            }

            // 繪製隊友
            for (int i = 0; i < companions.size(); i++) {
                Companion c = companions.get(i);
                int cx = companionX;
                int cy = companionY + i * 50;
                boolean isCompanionDown = c.hp <= 0;
                double companionHealGlow = companionHealGlowTicks[i] / (double) HEAL_GLOW_DURATION;

                boolean isCompanionActive = currentActor == c;
                int displayCx = cx;
                int displayCy = cy;
                if (isCompanionActive && !isCompanionDown
                        && ("playerAttack".equals(animAction) || "companionAttack".equals(animAction))) {
                    // 根據目標敵人調整移動方向
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
                    // 高亮邊框
                    g2d.setColor(Color.YELLOW);
                    g2d.setStroke(new BasicStroke(3));
                    g2d.drawOval(displayCx - 15, displayCy - 15, 30, 30);
                } else {
                    g2d.setColor(blendToWhite(new Color(100, 200, 100), companionHealGlow));
                    g2d.fillOval(displayCx - 12, displayCy - 12, 24, 24);
                }
                // 更新隊友的戰鬥屏幕坐標（用於點擊檢測）
                battleScreenCompanionX[i] = displayCx;
                battleScreenCompanionY[i] = displayCy;
                
                // 顯示隊友受傷血條
                if (companionTakingDamage[i] > 0) {
                    int barWidth = 50;
                    int barHeight = 8;
                    int barX = displayCx - barWidth / 2;
                    int barY = displayCy - 30;
                    
                    // 背景
                    g2d.setColor(Color.DARK_GRAY);
                    g2d.fillRect(barX, barY, barWidth, barHeight);
                    
                    // 血條
                    int hpWidth = (int) (barWidth * (c.hp / (double) c.maxHp));
                    g2d.setColor(new Color(255, 165, 0));
                    g2d.fillRect(barX, barY, hpWidth, barHeight);
                    
                    // 邊框
                    g2d.setColor(Color.WHITE);
                    g2d.setStroke(new BasicStroke(1.5f));
                    g2d.drawRect(barX, barY, barWidth, barHeight);
                }
            }

            // 繪製敵人（支持多個敵人）
            int enemyStartX = centerX + 120;
            int enemyStartY = centerY - 80;
            int enemySpacingY = 50;  // 敵人之間的垂直距離
            
            for (int i = 0; i < currentEnemies.size(); i++) {
                Enemy enemy = currentEnemies.get(i);
                int enemySlot = getEnemyBattleSlot(i);
                int actualEnemyX = enemyStartX;
                int actualEnemyY = enemyStartY + enemySlot * enemySpacingY;
                
                if ("enemyAttack".equals(animAction) && currentActionIndex < battleOrder.size()) {
                    BattleUnit currentUnit = battleOrder.get(currentActionIndex);
                    if (currentUnit.unit instanceof Enemy && currentUnit.unit == enemy) {
                        actualEnemyX = enemyStartX + offsetX;
                        actualEnemyY = enemyStartY + enemySlot * enemySpacingY + offsetY;
                    }
                }
                
                if (enemy.isBoss) {
                    g2d.setColor(new Color(150, 40, 30));
                    g2d.fillOval(actualEnemyX - 16, actualEnemyY - 16, 32, 32);
                    g2d.setColor(new Color(240, 200, 120));
                    g2d.setStroke(new BasicStroke(2f));
                    g2d.drawOval(actualEnemyX - 16, actualEnemyY - 16, 32, 32);
                } else {
                    g2d.setColor(Color.RED);
                    g2d.fillOval(actualEnemyX - 12, actualEnemyY - 12, 24, 24);
                }

                if (moonSliceAnimation) {
                    drawMoonSliceSlash(g2d, actualEnemyX, actualEnemyY);
                }
                
                // 更新該敵人的戰鬥屏幕坐標（用於點擊檢測）
                battleScreenEnemyX[i] = actualEnemyX;
                battleScreenEnemyY[i] = actualEnemyY;
                
                // 鼠標懸停敵人時顯示血量條形圖
                if (hoveredEnemyIndex == i) {
                    int barWidth = 50;
                    int barHeight = 8;
                    int barX = actualEnemyX - barWidth / 2;
                    int barY = actualEnemyY - 25;
                    
                    // 背景
                    g2d.setColor(Color.DARK_GRAY);
                    g2d.fillRect(barX, barY, barWidth, barHeight);
                    
                    // 血條
                    int hpWidth = (int) (barWidth * (enemy.hp / (double) enemy.maxHp));
                    g2d.setColor(new Color(255, 165, 0));
                    g2d.fillRect(barX, barY, hpWidth, barHeight);
                    
                    // 邊框
                    g2d.setColor(Color.WHITE);
                    g2d.setStroke(new BasicStroke(1.5f));
                    g2d.drawRect(barX, barY, barWidth, barHeight);
                }
                
                // 顯示敵人受傷血條（被攻擊後）
                if (damagedEnemyIndex == i && damagedEnemyTicks > 0) {
                    int barWidth = 50;
                    int barHeight = 8;
                    int barX = actualEnemyX - barWidth / 2;
                    int barY = actualEnemyY - 25;
                    
                    // 背景
                    g2d.setColor(Color.DARK_GRAY);
                    g2d.fillRect(barX, barY, barWidth, barHeight);
                    
                    // 血條
                    int hpWidth = (int) (barWidth * (enemy.hp / (double) enemy.maxHp));
                    g2d.setColor(new Color(255, 165, 0));
                    g2d.fillRect(barX, barY, hpWidth, barHeight);
                    
                    // 邊框
                    g2d.setColor(Color.WHITE);
                    g2d.setStroke(new BasicStroke(1.5f));
                    g2d.drawRect(barX, barY, barWidth, barHeight);
                }
            }

            // 繪製行動順序長條
            drawBattleOrderBar(g2d);
            
            // =============== 上方：敵人信息面板（鼠標懸停時） ===============
            if (hoveredEnemyIndex >= 0 && hoveredEnemyIndex < currentEnemies.size()) {
                Enemy enemy = currentEnemies.get(hoveredEnemyIndex);
                
                int infoPanelX = 20;
                int infoPanelY = 20;
                int infoPanelW = 220;
                int infoPanelH = 55;
                
                // 面板背景
                g2d.setColor(new Color(100, 0, 0, 200));
                g2d.fillRect(infoPanelX, infoPanelY, infoPanelW, infoPanelH);
                
                // 邊框
                g2d.setColor(new Color(200, 0, 0));
                g2d.setStroke(new BasicStroke(2));
                g2d.drawRect(infoPanelX, infoPanelY, infoPanelW, infoPanelH);
                
                // 敵人名字
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Microsoft JhengHei", Font.BOLD, 13));
                String enemyName = enemy.name != null ? enemy.name : ("敵人" + (hoveredEnemyIndex + 1));
                g2d.drawString(enemyName, infoPanelX + 10, infoPanelY + 18);
                
                // 敵人等級
                g2d.setColor(new Color(200, 200, 100));
                g2d.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 11));
                g2d.drawString("Lv." + enemy.level, infoPanelX + 10, infoPanelY + 35);
                
                // 敵人血量
                g2d.setColor(new Color(255, 100, 100));
                g2d.setFont(new Font("Microsoft JhengHei", Font.BOLD, 12));
                String hpText = "HP: " + enemy.hp + "/" + enemy.maxHp;
                g2d.drawString(hpText, infoPanelX + 100, infoPanelY + 35);
            }

            // =============== 下方：玩家/隊友信息面板 ===============
            int panelY = getHeight() - 180;
            int panelH = 160;

            // 面板背景
            g2d.setColor(new Color(20, 40, 80, 200));
            g2d.fillRect(0, panelY, getWidth(), panelH);
            g2d.setColor(new Color(100, 150, 200));
            g2d.setStroke(new BasicStroke(3));
            g2d.drawRect(0, panelY, getWidth(), panelH);

            // 計算角色面板寬度（平均分配）
            int characterCount = 1 + companions.size(); // 玩家 + 隊友
            int charPanelW = (getWidth() - 20) / characterCount;
            int charPanelH = 125;
            int charPanelY = panelY + 10;

            // 繪製玩家信息
            drawCharacterPanel(g2d, 10, charPanelY, charPanelW - 10, charPanelH,
                    player.name != null ? player.name : "玩家", player.hp, player.maxHp, 
                    player.mana, player.maxMana, player.cp, player.maxCp);

            // 繪製隊友信息
            for (int i = 0; i < companions.size(); i++) {
                Companion c = companions.get(i);
                int x = 10 + (i + 1) * charPanelW;
                drawCharacterPanel(g2d, x, charPanelY, charPanelW - 10, charPanelH, c.name, c.hp, c.maxHp,
                        c.mana, c.maxMana, c.cp, c.maxCp);
            }

            // =============== 下方：行動按鈕區域 ===============
            int btnAreaY = panelY + 130;
            int btnW = 85, btnH = 35, gap = 8;
            int startX = (getWidth() - (btnW * 4 + gap * 3)) / 2;

            attackRect = new Rectangle(startX, btnAreaY, btnW, btnH);
            skillRect = new Rectangle(startX + btnW + gap, btnAreaY, btnW, btnH);
            healRect = new Rectangle(startX + (btnW + gap) * 2, btnAreaY, btnW, btnH);
            runRect = new Rectangle(startX + (btnW + gap) * 3, btnAreaY, btnW, btnH);

            // 繪製按鈕
            g2d.setColor(new Color(60, 60, 100));
            g2d.fill(attackRect);
            g2d.fill(skillRect);
            g2d.fill(healRect);
            g2d.fill(runRect);

            g2d.setColor(new Color(150, 150, 200));
            g2d.setStroke(new BasicStroke(2));
            g2d.draw(attackRect);
            g2d.draw(skillRect);
            g2d.draw(healRect);
            g2d.draw(runRect);

            // 繪製按鈕文字
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Microsoft JhengHei", Font.BOLD, 13));
            FontMetrics fm = g2d.getFontMetrics();

            String[] btnTexts = { "攻擊", "戰技", "用藥", "逃跑" };
            Rectangle[] btnRects = { attackRect, skillRect, healRect, runRect };
            for (int i = 0; i < btnTexts.length; i++) {
                String text = btnTexts[i];
                Rectangle rect = btnRects[i];
                int tX = rect.x + (rect.width - fm.stringWidth(text)) / 2;
                int tY = rect.y + ((rect.height - fm.getHeight()) / 2) + fm.getAscent();
                g2d.drawString(text, tX, tY);
            }

            // 攻擊順序提示（左上角）
            // drawTurnOrder(g2d);  // 移除藍色和紅色方塊
            
            // 掉落物品預覽（戰鬥中顯示將掉落的物品，右上角）
            if (!previewDrops.isEmpty()) {
                g2d.setColor(new Color(200, 150, 100));
                g2d.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 12));
                FontMetrics fmDrops = g2d.getFontMetrics();
                int textWidth = fmDrops.stringWidth("掉落: " + previewDrops);
                g2d.drawString("掉落: " + previewDrops, getWidth() - textWidth - 20, 35);
            }

            // 顯示目標選擇提示
            if (!selectingTargetMode.isEmpty()) {
                int msgCenterX = getWidth() / 2;
                int msgCenterY = 80;
                
                g2d.setColor(new Color(255, 200, 0));  // 黃色
                g2d.setStroke(new BasicStroke(3));
                g2d.drawRect(msgCenterX - 180, msgCenterY - 20, 360, 40);
                
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Microsoft JhengHei", Font.BOLD, 14));
                FontMetrics fmTarget = g2d.getFontMetrics();
                
                String targetMsg = "";
                if ("attack".equals(selectingTargetMode)) {
                    targetMsg = "點擊要攻擊的敵人  (右鍵取消)";
                } else if ("skill".equals(selectingTargetMode)) {
                    targetMsg = "點擊目標  (右鍵取消)";
                } else if ("potion".equals(selectingTargetMode)) {
                    targetMsg = "點擊要治療的目標  (右鍵取消)";
                }
                
                int textX = msgCenterX - fmTarget.stringWidth(targetMsg) / 2;
                g2d.drawString(targetMsg, textX, msgCenterY + 5);
            }

            // 顯示逃跑結果訊息
            if (showingFleeMessage && !fleeMessage.isEmpty()) {
                int msgCenterX = getWidth() / 2;
                int msgCenterY = getHeight() / 2;
                
                // 半透明背景
                g2d.setColor(new Color(0, 0, 0, 150));
                g2d.fillRect(0, 0, getWidth(), getHeight());
                
                // 訊息框
                int boxWidth = 300;
                int boxHeight = 120;
                int boxX = msgCenterX - boxWidth / 2;
                int boxY = msgCenterY - boxHeight / 2;
                
                g2d.setColor(new Color(50, 50, 100, 200));
                g2d.fillRect(boxX, boxY, boxWidth, boxHeight);
                g2d.setColor(new Color(100, 150, 255));
                g2d.setStroke(new BasicStroke(2));
                g2d.drawRect(boxX, boxY, boxWidth, boxHeight);
                
                // 訊息文字
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Microsoft JhengHei", Font.BOLD, 16));
                FontMetrics fmMsg = g2d.getFontMetrics();
                int textX = msgCenterX - fmMsg.stringWidth(fleeMessage) / 2;
                g2d.drawString(fleeMessage, textX, msgCenterY - 20);
                
                // 提示文字
                g2d.setColor(Color.YELLOW);
                g2d.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 12));
                g2d.drawString("按任意鍵繼續...", msgCenterX - 60, msgCenterY + 30);
            }

            return;
        }

        // 非戰鬥時畫地圖與角色
        // 依 mapIndex 決定地圖主色調
        Color floorA, floorB, wallCol;
        if (mapIndex == 0) {
            floorA = new Color(0, 100, 0); // 草原深綠
            floorB = new Color(0, 120, 0); // 草原淺綠
            wallCol = new Color(100, 80, 50); // 泥土牆
        } else if (mapIndex == 1) {
            floorA = new Color(40, 35, 30); // 地下城深
            floorB = new Color(55, 50, 42); // 地下城淺
            wallCol = new Color(80, 70, 60); // 石頭牆
        } else {
            floorA = new Color(60, 22, 18); // 魔王殿深
            floorB = new Color(80, 30, 24); // 魔王殿淺
            wallCol = new Color(120, 70, 50); // 深紅石牆
        }
        int arc = TILE_SIZE / 4;
        for (int y = 0; y < map.length; y++) {
            for (int x = 0; x < map[0].length; x++) {
                if (map[y][x] == 1) {
                    g2d.setColor(wallCol);
                } else {
                    g2d.setColor(((x + y) % 2 == 0) ? floorA : floorB);
                }
                g2d.fillRoundRect(x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE, arc, arc);
            }
        }

        // 傳送門視覺效果
        g2d.setFont(new Font("Microsoft JhengHei", Font.BOLD, 11));
        if (mapIndex == 0) {
            Color portalColor = new Color(255, 200, 50, 180);
            g2d.setColor(portalColor);
            // 右邊界，第7-8行
            g2d.fillRoundRect(19 * TILE_SIZE, 7 * TILE_SIZE, TILE_SIZE, 2 * TILE_SIZE, 10, 10);
            g2d.setColor(portalColor.darker());
            g2d.drawString("→地下城", 19 * TILE_SIZE - 4, 7 * TILE_SIZE - 4);
        } else if (mapIndex == 1) {
            Color leftPortal = new Color(80, 160, 255, 180);
            Color rightPortal = new Color(220, 90, 70, 190);
            // 左邊界，第7-8行
            g2d.setColor(leftPortal);
            g2d.fillRoundRect(0, 7 * TILE_SIZE, TILE_SIZE, 2 * TILE_SIZE, 10, 10);
            g2d.setColor(leftPortal.darker());
            g2d.drawString("←草原", 2, 7 * TILE_SIZE - 4);
            // 右邊界，第7-8行
            g2d.setColor(rightPortal);
            g2d.fillRoundRect(19 * TILE_SIZE, 7 * TILE_SIZE, TILE_SIZE, 2 * TILE_SIZE, 10, 10);
            g2d.setColor(rightPortal.darker());
            g2d.drawString("→魔王殿", 19 * TILE_SIZE - 6, 7 * TILE_SIZE - 4);
        } else {
            Color portalColor = new Color(220, 90, 70, 190);
            // 左邊界，第7-8行
            g2d.setColor(portalColor);
            g2d.fillRoundRect(0, 7 * TILE_SIZE, TILE_SIZE, 2 * TILE_SIZE, 10, 10);
            g2d.setColor(portalColor.darker());
            g2d.drawString("←地下城", 2, 7 * TILE_SIZE - 4);
        }

        for (Enemy e : enemies) {
            if (e.shouldRenderOnMap()) {
                if (e.isBoss) {
                    g2d.setColor(new Color(150, 40, 30));
                    g2d.fillOval((int) e.x + 4, (int) e.y + 4, 32, 32);
                    g2d.setColor(new Color(240, 200, 120));
                    g2d.setStroke(new BasicStroke(2f));
                    g2d.drawOval((int) e.x + 4, (int) e.y + 4, 32, 32);
                } else {
                    g2d.setColor(Color.RED);
                    g2d.fillOval((int) e.x + 8, (int) e.y + 8, 24, 24);
                }
            }
        }
        // 顯示隊友
        for (Companion c : companions) {
            g2d.setColor(new Color(100, 200, 100)); // 綠色表示隊友
            g2d.fillOval((int) c.x + 8, (int) c.y + 8, 24, 24);
            // 顯示隊友名稱
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Microsoft JhengHei", Font.BOLD, 10));
            g2d.drawString(c.name, (int) c.x - 5, (int) c.y - 5);
        }
        
        int playerSpriteSize = 40;
        BufferedImage currentPlayerSprite = getCurrentPlayerSprite();
        int drawSize = getMapPlayerDrawSize(currentPlayerSprite, playerSpriteSize);
        int playerSpriteOffset = (TILE_SIZE - drawSize) / 2;

        if (currentPlayerSprite != null) {
            g2d.drawImage(currentPlayerSprite,
                    (int) player.x + playerSpriteOffset,
                    (int) player.y + playerSpriteOffset,
                    drawSize,
                    drawSize,
                    null);
        } else {
            g2d.setColor(Color.BLUE);
            g2d.fillOval((int) player.x + 8, (int) player.y + 8, 24, 24);
        }

        // 地圖介面：單一選單按鈕
        int mw = 90, mh = 32;
        menuRect = new Rectangle(10, getHeight() - mh - 10, mw, mh);

        g2d.setColor(new Color(30, 30, 45, 230));
        g2d.fillRoundRect(menuRect.x, menuRect.y, menuRect.width, menuRect.height, 8, 8);
        g2d.setColor(new Color(180, 200, 255));
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRoundRect(menuRect.x, menuRect.y, menuRect.width, menuRect.height, 8, 8);
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Microsoft JhengHei", Font.BOLD, 13));
        g2d.drawString("選單", menuRect.x + 28, menuRect.y + 21);

        if (showMapMenu) {
            int panelW = Math.min(getWidth() - 40, 680);
            int panelH = Math.min(getHeight() - 60, 460);
            int panelX = 20;
            int panelY = 20;
            mapMenuPanelRect = new Rectangle(panelX, panelY, panelW, panelH);

            // 面板背景
            GradientPaint panelGrad = new GradientPaint(panelX, panelY, new Color(20, 26, 48, 235),
                    panelX, panelY + panelH, new Color(8, 12, 24, 235));
            g2d.setPaint(panelGrad);
            g2d.fillRoundRect(panelX, panelY, panelW, panelH, 12, 12);
            g2d.setColor(new Color(110, 150, 220));
            g2d.setStroke(new BasicStroke(2));
            g2d.drawRoundRect(panelX, panelY, panelW, panelH, 12, 12);

            // 上方分頁
            int tabY = panelY + 10;
            int tabW = 140;
            int tabH = 38;
            int tabGap = 10;
            statusTabRect = new Rectangle(panelX + 14, tabY, tabW, tabH);
            bagTabRect = new Rectangle(statusTabRect.x + tabW + tabGap, tabY, tabW, tabH);
            saveTabRect = new Rectangle(bagTabRect.x + tabW + tabGap, tabY, tabW, tabH);

            Rectangle[] tabs = { statusTabRect, bagTabRect, saveTabRect };
            String[] tabTitles = { "狀態", "背包", "保存" };
            for (int i = 0; i < tabs.length; i++) {
                Rectangle r = tabs[i];
                boolean active = (selectedMapTab == i);
                boolean hover = (hoveredMapTab == i);
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

            // 分頁內容區
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

            if (selectedMapTab == 0) {
                // 狀態：左側立繪占位 + 右側單角色卡片（玩家/隊友可切換）
                int selectorX = contentX + 14;
                int selectorY = contentY + 12;
                int selectorW = 96;
                int selectorH = 28;
                int selectorGap = 8;

                statusPlayerSelectorRect = new Rectangle(selectorX, selectorY, selectorW, selectorH);
                boolean playerSelected = (selectedStatusActor == -1);
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

                for (int i = 0; i < statusCompanionSelectorRects.length; i++) {
                    statusCompanionSelectorRects[i] = null;
                }

                int selectorCursorX = selectorX + selectorW + selectorGap;
                for (int i = 0; i < companions.size() && i < statusCompanionSelectorRects.length; i++) {
                    Companion c = companions.get(i);
                    Rectangle selectorRect = new Rectangle(selectorCursorX, selectorY, selectorW, selectorH);
                    statusCompanionSelectorRects[i] = selectorRect;
                    boolean selected = (selectedStatusActor == i);
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

                // 如果選到不存在的隊友，自動回到玩家
                if (selectedStatusActor >= companions.size()) {
                    selectedStatusActor = -1;
                }

                boolean showingPlayer = selectedStatusActor == -1;
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
                    actorName = player.name;
                    actorLevel = player.level;
                    actorExp = player.exp;
                    actorExpToNext = player.expToNext;
                    actorHp = player.hp;
                    actorMaxHp = player.maxHp;
                    actorMana = player.mana;
                    actorMaxMana = player.maxMana;
                    actorCp = player.cp;
                    actorMaxCp = player.maxCp;
                    actorPatk = player.patk;
                    actorPdef = player.pdef;
                    actorMatk = player.matk;
                    actorMdef = player.mdef;
                    actorSpeed = player.battleSpeed;
                    actorSkills = player.skills;
                } else {
                    Companion actor = companions.get(selectedStatusActor);
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
                    int theme = Math.abs(selectedStatusActor) % 3;
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
                int portraitPadding = 10;
                int nameAreaHeight = 24;
                int imageX = portraitX + portraitPadding;
                int imageY = portraitY + portraitPadding;
                int imageW = portraitW - portraitPadding * 2;
                int imageH = portraitH - portraitPadding * 2 - nameAreaHeight;

                if (statusPortraitImage != null) {
                    double scale = Math.min(imageW / (double) statusPortraitImage.getWidth(),
                            imageH / (double) statusPortraitImage.getHeight());
                    int drawW = (int) Math.round(statusPortraitImage.getWidth() * scale);
                    int drawH = (int) Math.round(statusPortraitImage.getHeight() * scale);
                    int drawX = imageX + (imageW - drawW) / 2;
                    int drawY = imageY + (imageH - drawH) / 2;
                    g2d.drawImage(statusPortraitImage, drawX, drawY, drawW, drawH, null);
                }

                g2d.setColor(new Color(220, 230, 255));
                g2d.setFont(new Font("Microsoft JhengHei", Font.BOLD, 16));
                FontMetrics nameFm = g2d.getFontMetrics();
                int nameX = portraitX + (portraitW - nameFm.stringWidth(actorName)) / 2;
                int nameY = portraitY + portraitH - portraitPadding;
                g2d.drawString(actorName, nameX, nameY);

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
            } else if (selectedMapTab == 1) {
                // 背包
                java.util.List<String> bagItems = new java.util.ArrayList<>();
                bagItems.add("小藥 x" + player.smallPotions);
                bagItems.add("大藥 x" + player.largePotions);
                bagItems.add("金幣 x" + player.gold);

                int visibleStart = bagScrollOffset;
                int visibleEnd = Math.min(bagScrollOffset + BAG_VISIBLE_ITEMS, bagItems.size());
                int itemY = contentY + 34;
                for (int i = visibleStart; i < visibleEnd; i++) {
                    g2d.setColor(i == bagScrollOffset ? Color.CYAN : Color.WHITE);
                    g2d.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 15));
                    g2d.drawString("- " + bagItems.get(i), contentX + 14, itemY);
                    itemY += 30;
                }

                g2d.setColor(new Color(160, 180, 210));
                g2d.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 11));
                g2d.drawString("滑鼠滾輪可滾動項目", contentX + 14, contentY + contentH - 12);
            } else {
                // 保存
                int slotX = contentX + 12;
                int slotYBase = contentY + 16;
                int slotW = contentW - 24;
                int slotH = 76;

                for (int i = 0; i < MAX_SAVE_SLOTS; i++) {
                    int slotY = slotYBase + i * (slotH + 8);
                    g2d.setColor(i == selectedSaveSlot ? new Color(255, 200, 0, 180) : new Color(70, 80, 110, 180));
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

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // 開始菜單或加載菜單狀態下，只進行渲染，不進行遊戲邏輯更新
        if (state == -1 || showLoadMenu) {
            repaint();
            return;
        }
        
        if (state == 0) {
            // 如果有菜單打開，則暫停遊戲邏輯（不更新敵人、隊友、碰撞等）
            if (showMapMenu) {
                repaint();
                return;
            }
            
            // AABB 滑牆碰撞：x和y分別棄數
            if (mouseDown || keyDown) {
                double newX = player.x + player.vx;
                double newY = player.y + player.vy;
                boolean canX = !hitWall(newX, player.y);
                boolean canY = !hitWall(player.x, newY);
                if (canX)
                    player.x = newX;
                if (canY)
                    player.y = newY;
                if (!canX)
                    player.vx = 0;
                if (!canY)
                    player.vy = 0;
            }
            // 更新玩家走路動畫
            updatePlayerWalkAnimation();

            // 滑鼠長按方向
            if (mouseDown) {
                int tx = mouseGridX;
                int ty = mouseGridY;
                int dirX = Integer.compare(tx, (int) (player.x / TILE_SIZE));
                int dirY = Integer.compare(ty, (int) (player.y / TILE_SIZE));
                player.vx = dirX * player.speed;
                player.vy = dirY * player.speed;
            }

            // 更新敵人（僅在探索狀態時）
            if (state == 0) {
                double dt = 0.03;
                for (Enemy en : enemies)
                    en.update(dt, player.x, player.y, map);
            }

            // 更新隊友（跟隨玩家）
            for (Companion c : companions) {
                // 直接讓隊友跟隨玩家，不使用複雜的路徑歷史
                c.followPlayer(player, map);
            }

            // 碰到敵人（閃爍保護期內不可進入戰鬥）
            for (Enemy en : enemies) {
                if (!en.isDefeated() && !en.isBattleLocked() && Math.hypot(player.x - en.x, player.y - en.y) < 22) {
                    state = 1;
                    triggeredEnemy = en;  // 記錄觸發的敵人
                    // 隨機生成1-3個敵人加入戰鬥
                    spawnRandomEnemies();
                    playBattleMusic(); // 進入戰鬥時播放戰鬥音樂
                    previewDrops = generateDropsPreview(); // 預先計算掉落物品
                    break;
                }
            }

            // 地圖傳送切換
            checkPortal();
            repaint();  // 更新探索畫面
        } else if (state == 1 && animating) {
            animTicks++;

            boolean playerMovingInBattle = currentActor instanceof Player
                    && !isHealingSkillAnimation()
                    && ("playerAttack".equals(animAction) || "companionAttack".equals(animAction));
            updatePlayerWalkAnimationForBattle(playerMovingInBattle);
            
            // 更新受傷血條的顯示計時
            if (damagedEnemyTicks > 0) {
                damagedEnemyTicks--;
                if (damagedEnemyTicks == 0) {
                    damagedEnemyIndex = -1;
                }
            }
            if (playerDamageTicks > 0) {
                playerDamageTicks--;
                if (playerDamageTicks == 0) {
                    playerTakingDamage = false;
                }
            }
            if (playerHealGlowTicks > 0) {
                playerHealGlowTicks--;
            }
            for (int i = 0; i < companionTakingDamage.length; i++) {
                if (companionTakingDamage[i] > 0) {
                    companionTakingDamage[i]--;
                }
                if (companionHealGlowTicks[i] > 0) {
                    companionHealGlowTicks[i]--;
                }
            }
            
            if (animTicks >= ANIM_DURATION) {
                // 動畫順序：玩家攻擊或敵人攻擊
                if ("playerAttack".equals(animAction)) {
                    int dmg = 0;
                    Object attacker = currentActor;

                    // 確定攻擊者的屬性
                    int patk = 0;
                    if (attacker instanceof Player) {
                        patk = ((Player) attacker).patk;
                    } else if (attacker instanceof Companion) {
                        patk = ((Companion) attacker).patk;
                    } else {
                        patk = player.patk;
                    }

                    // 檢查是否使用戰技
                    if (selectedSkill != null) {
                        // 根據目標敵人索引攻擊
                        int targetIndex = (targetingEnemyIndex >= 0 && targetingEnemyIndex < currentEnemies.size()) 
                            ? targetingEnemyIndex : 0;
                        if (!currentEnemies.isEmpty() && targetIndex < currentEnemies.size()) {
                            selectedSkill.execute(this, player, currentEnemies.get(targetIndex));
                            
                            // 扣除 CP
                            if (attacker instanceof Player) {
                                ((Player) attacker).cp -= selectedSkill.cpCost;
                            } else if (attacker instanceof Companion) {
                                ((Companion) attacker).cp -= selectedSkill.cpCost;
                            }
                            
                            // 單體戰技補上被攻擊方 CP；範圍技由戰技內部處理
                            if (!(selectedSkill instanceof SkillMoonSlice)
                                    && targetIndex >= 0
                                    && targetIndex < currentEnemies.size()) {
                                currentEnemies.get(targetIndex).cp = Math.min(currentEnemies.get(targetIndex).cp + 5,
                                        currentEnemies.get(targetIndex).maxCp);
                            }

                            removeDefeatedEnemiesFromBattle();
                            if (tryEnterVictorySettlementIfNoEnemies()) {
                                selectedSkill = null;
                                return;
                            }
                        }
                        selectedSkill = null; // 重置戰技
                    } else {
                        // 普通攻擊，根據目標敵人索引攻擊
                        int targetIndex = (targetingEnemyIndex >= 0 && targetingEnemyIndex < currentEnemies.size()) 
                            ? targetingEnemyIndex : 0;
                        if (!currentEnemies.isEmpty() && targetIndex < currentEnemies.size()) {
                            Enemy targetEnemy = currentEnemies.get(targetIndex);
                            dmg = rollDamage(patk, targetEnemy.pdef);
                            targetEnemy.hp -= dmg;
                            
                            // 增加玩家 CP（最多到 200）
                            player.cp = Math.min(player.cp + 5, player.maxCp);
                            
                            // 增加敵人 CP（被動攻擊）
                            targetEnemy.cp = Math.min(targetEnemy.cp + 5, targetEnemy.maxCp);
                            
                            // 設置敵人受傷的血條顯示
                            damagedEnemyIndex = targetIndex;
                            damagedEnemyTicks = DAMAGE_DISPLAY_DURATION;
                            
                            // 檢查敵人是否被擊敗
                            if (targetEnemy.hp <= 0) {
                                targetEnemy.defeated = true;
                                removeEnemyAt(targetIndex);  // 移除已擊敗的敵人（保留其他敵人站位）
                                // 更新targetingEnemyIndex
                                if (targetIndex >= currentEnemies.size() && targetIndex > 0) {
                                    targetingEnemyIndex = targetIndex - 1;
                                } else if (targetIndex < currentEnemies.size()) {
                                    targetingEnemyIndex = targetIndex;
                                } else {
                                    targetingEnemyIndex = -1;
                                }
                                
                                // 如果所有敵人都被擊敗
                                if (currentEnemies.isEmpty()) {
                                    // 標記所有原始敵人為已擊敗，防止返回地圖時立即重新觸發戰鬥
                                    for (Enemy origEnemy : originalBattleEnemies) {
                                        origEnemy.defeated = true;
                                    }
                                    originalBattleEnemies.clear();
                                    
                                    isZeroExpSettlement = false; // 重置為敵人被擊敗狀態
                                    // 經驗分配給全隊
                                    int totalExp = 60;  // 假設每個敵人60經驗
                                    levelsGained = player.gainExp(totalExp / 2);
                                    for (int i = 0; i < companions.size(); i++) {
                                        Companion c = companions.get(i);
                                        companionLevelsGained[i] = c.gainExp(totalExp / 2);
                                        companionExpForNextLevel[i] = Math.max(0, c.expToNext - c.exp);
                                    }
                                    lastBattleExp = totalExp;
                                    expForNextLevel = Math.max(0, player.expToNext - player.exp);
                                    applyDropsFromPreview(); // 應用掉落物品
                                    settlementDrops = previewDrops.isEmpty() ? "" : "掉落物品: " + previewDrops; // 設置結算顯示
                                    state = 2; // 進入結算畫面
                                    playBattleEndMusic();  // 撥放結算音樂
                                    animating = false;
                                    animTicks = 0;
                                    currentActor = null;
                                    waitingForPlayerDecision = false;
                                    repaint();
                                    return;
                                }
                            }
                        }
                    }

                    // 攻擊完成後依 AT 重新排序
                    completeActionAndRefreshBattleOrder(selectedSkill != null ? 40 : 20);
                } else if ("companionAttack".equals(animAction)) {
                    // 隊友執行攻擊
                    if (currentActionIndex < battleOrder.size()) {
                        BattleUnit currentUnit = battleOrder.get(currentActionIndex);
                        if (currentUnit.unit instanceof Companion) {
                            Companion companion = (Companion) currentUnit.unit;
                            // 根據目標敵人索引攻擊
                            int targetIndex = (targetingEnemyIndex >= 0 && targetingEnemyIndex < currentEnemies.size()) 
                                ? targetingEnemyIndex : 0;
                            if (!currentEnemies.isEmpty() && targetIndex < currentEnemies.size()) {
                                Enemy targetEnemy = currentEnemies.get(targetIndex);
                                int dmg = rollDamage(companion.patk, targetEnemy.pdef);
                                targetEnemy.hp -= dmg;
                                
                                // 增加隊友 CP（最多到 200）
                                companion.cp = Math.min(companion.cp + 5, companion.maxCp);
                                
                                // 增加敵人 CP（被動攻擊）
                                targetEnemy.cp = Math.min(targetEnemy.cp + 5, targetEnemy.maxCp);
                                
                                // 設置敵人受傷的血條顯示
                                damagedEnemyIndex = targetIndex;
                                damagedEnemyTicks = DAMAGE_DISPLAY_DURATION;
                                
                                if (targetEnemy.hp <= 0) {
                                    targetEnemy.defeated = true;
                                    removeEnemyAt(targetIndex);
                                    // 更新targetingEnemyIndex
                                    if (targetIndex >= currentEnemies.size() && targetIndex > 0) {
                                        targetingEnemyIndex = targetIndex - 1;
                                    } else if (targetIndex < currentEnemies.size()) {
                                        targetingEnemyIndex = targetIndex;
                                    } else {
                                        targetingEnemyIndex = -1;
                                    }
                                    
                                    if (currentEnemies.isEmpty()) {
                                        // 標記所有原始敵人為已擊敗，防止返回地圖時立即重新觸發戰鬥
                                        for (Enemy origEnemy : originalBattleEnemies) {
                                            origEnemy.defeated = true;
                                        }
                                        originalBattleEnemies.clear();
                                        
                                        isZeroExpSettlement = false; // 重置為敵人被擊敗狀態
                                        int exp = 60;
                                        levelsGained = player.gainExp(exp / 2);
                                        for (int i = 0; i < companions.size(); i++) {
                                            Companion c = companions.get(i);
                                            companionLevelsGained[i] = c.gainExp(exp / 2);
                                            companionExpForNextLevel[i] = Math.max(0, c.expToNext - c.exp);
                                        }
                                        lastBattleExp = exp;
                                        expForNextLevel = Math.max(0, player.expToNext - player.exp);
                                        applyDropsFromPreview(); // 應用掉落物品
                                        settlementDrops = previewDrops.isEmpty() ? "" : "掉落物品: " + previewDrops; // 設置結算顯示
                                        state = 2; // 進入結算畫面
                                        playBattleEndMusic();  // 撥放結算音樂
                                        animating = false;
                                        animTicks = 0;
                                        repaint();
                                        return;
                                    }
                                }
                            }
                        }
                    }
                    // 隊友行動完成後依 AT 重新排序
                    completeActionAndRefreshBattleOrder(20);
                } else if ("enemyAttack".equals(animAction)) {
                    // 敵人執行攻擊，使用動畫開始時就決定的目標
                    Object target = getEnemyAttackTargetUnit();
                    if (target == null) {
                        chooseEnemyAttackTarget();
                        target = getEnemyAttackTargetUnit();
                    }

                    int dmg = 0;
                    int enemyPatk = 36;  // 預設敵人物攻
                    if (!currentEnemies.isEmpty()) {
                        enemyPatk = currentEnemies.get(0).patk;
                    }
                    
                    if (target instanceof Player) {
                        Player p = (Player) target;
                        dmg = rollDamage(enemyPatk, p.pdef);
                        p.hp = Math.max(0, p.hp - dmg);
                        playerTakingDamage = true;
                        playerDamageTicks = DAMAGE_DISPLAY_DURATION;
                        
                        // 增加玩家 CP（被攻擊）
                        p.cp = Math.min(p.cp + 5, p.maxCp);
                        
                    } else if (target instanceof Companion) {
                        Companion c = (Companion) target;
                        dmg = rollDamage(enemyPatk, c.pdef);
                        c.hp = Math.max(0, c.hp - dmg);
                        
                        // 增加隊友 CP（被攻擊）
                        c.cp = Math.min(c.cp + 5, c.maxCp);
                        
                        // 設置隊友受傷的血條顯示
                        for (int i = 0; i < companions.size(); i++) {
                            if (c == companions.get(i)) {
                                companionTakingDamage[i] = DAMAGE_DISPLAY_DURATION;
                                break;
                            }
                        }
                        
                        if (c.hp <= 0) {
                            // 隊友被擊敗（可在此處理特殊事件）
                            JOptionPane.showMessageDialog(this, c.name + "被擊敗！");
                        }
                    }

                    // 只有玩家與所有隊友都被擊敗才結束遊戲
                    if (areAllAlliesDefeated()) {
                        handlePartyDefeatReturnToMainMenu();
                        repaint();
                        return;
                    }

                    // 敵人行動完成後依 AT 重新排序
                    completeActionAndRefreshBattleOrder(20);
                }
            }
            repaint();
        } else if (state == 1 && !animating && currentEnemies.size() > 0) {
            // 檢查當前行動者
            removeInvisibleBattleUnits();
            sortBattleOrderByAt();
            currentActionIndex = 0;

            if (currentActionIndex < battleOrder.size()) {
                BattleUnit currentUnit = battleOrder.get(0);

                // 如果輪到玩家或隊友，等待玩家輸入
                if (currentUnit.unit instanceof Player || currentUnit.unit instanceof Companion) {
                    // 已倒下的角色跳過回合
                    if (currentUnit.unit instanceof Player && ((Player) currentUnit.unit).hp <= 0) {
                        removeInvisibleBattleUnits();
                        sortBattleOrderByAt();
                        currentActionIndex = 0;
                        return;
                    }
                    if (currentUnit.unit instanceof Companion && ((Companion) currentUnit.unit).hp <= 0) {
                        removeInvisibleBattleUnits();
                        sortBattleOrderByAt();
                        currentActionIndex = 0;
                        return;
                    }

                    if (!waitingForPlayerDecision) {
                        currentActor = currentUnit.unit;
                        waitingForPlayerDecision = true;
                    }
                }
                // 如果輪到敵人，在此直接執行（可以改成等待時間後自動）
                else if (currentUnit.unit instanceof Enemy) {
                    // 檢查該敵人是否仍在戰鬥中
                    boolean enemyStillFighting = false;
                    for (Enemy enemy : currentEnemies) {
                        if (enemy == currentUnit.unit) {
                            enemyStillFighting = true;
                            break;
                        }
                    }
                    
                    if (!enemyStillFighting) {
                        // 敵人已被擊敗，跳過該敵人的回合
                        removeInvisibleBattleUnits();
                        sortBattleOrderByAt();
                        currentActionIndex = 0;
                        return;
                    }
                    
                    // 敵人可能選擇逃脫
                    Enemy enemy = (Enemy) currentUnit.unit;
                    double hpRatio = enemy.hp / (double) enemy.maxHp;
                    double fleeChance = 0;
                    if (hpRatio < 0.5) {
                        fleeChance = (0.5 - hpRatio) / 0.5 * 0.55;
                    }
                    if (Math.random() < fleeChance) {
                        // 敵人逃脫
                        enemy.defeated = true;
                        enemy.setBattleLock(5000);  // 鎖定敵人，防止立即再戰
                        removeEnemy(enemy);
                        
                        // 如果所有敵人都逃脫
                        if (currentEnemies.isEmpty()) {
                            isZeroExpSettlement = true;
                            lastBattleExp = 0;
                            expForNextLevel = 0;
                            levelsGained = 0;
                            for (int i = 0; i < companionLevelsGained.length; i++) {
                                companionLevelsGained[i] = 0;
                                companionExpForNextLevel[i] = 0;
                            }
                            previewDrops = ""; // 敵人逃脫時清除掉落物品預覽
                            state = 2; // 進入結算畫面
                            playBattleEndMusic();  // 撥放結算音樂
                            currentActor = null;
                            waitingForPlayerDecision = false;
                            repaint();
                        } else {
                            // 還有其他敵人，依 AT 重新排序
                            removeInvisibleBattleUnits();
                            sortBattleOrderByAt();
                            currentActionIndex = 0;
                        }
                    } else {
                        // 敵人執行攻擊
                        chooseEnemyAttackTarget();
                        animating = true;
                        animAction = "enemyAttack";
                        animTicks = 0;
                        currentActor = (Enemy) currentUnit.unit;  // 設置當前行動者為敵人
                        
                        // 找出該敵人的固定站位槽位
                        currentAttackingEnemyIndex = -1;
                        for (int i = 0; i < currentEnemies.size(); i++) {
                            if (currentEnemies.get(i) == currentUnit.unit) {
                                currentAttackingEnemyIndex = getEnemyBattleSlot(i);
                                break;
                            }
                        }
                    }
                }
            }
            
            // 更新受傷血條的顯示計時
            if (damagedEnemyTicks > 0) {
                damagedEnemyTicks--;
                if (damagedEnemyTicks == 0) {
                    damagedEnemyIndex = -1;
                }
            }
            if (playerDamageTicks > 0) {
                playerDamageTicks--;
                if (playerDamageTicks == 0) {
                    playerTakingDamage = false;
                }
            }
            if (playerHealGlowTicks > 0) {
                playerHealGlowTicks--;
            }
            for (int i = 0; i < companionTakingDamage.length; i++) {
                if (companionTakingDamage[i] > 0) {
                    companionTakingDamage[i]--;
                }
                if (companionHealGlowTicks[i] > 0) {
                    companionHealGlowTicks[i]--;
                }
            }
            
            repaint();
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        // 加載菜單導航（優先處理）
        if (showLoadMenu) {
            int key = e.getKeyCode();
            switch (key) {
                case KeyEvent.VK_UP:
                    selectedLoadSlot = (selectedLoadSlot - 1 + MAX_SAVE_SLOTS) % MAX_SAVE_SLOTS;
                    repaint();
                    break;
                case KeyEvent.VK_DOWN:
                    selectedLoadSlot = (selectedLoadSlot + 1) % MAX_SAVE_SLOTS;
                    repaint();
                    break;
                case KeyEvent.VK_ENTER:
                    loadGame(selectedLoadSlot);
                    showLoadMenu = false;
                    break;
                case KeyEvent.VK_ESCAPE:
                    showLoadMenu = false;
                    repaint();
                    break;
            }
            return;
        }
        
        // 開始菜單導航
        if (state == -1) {
            int key = e.getKeyCode();
            switch (key) {
                case KeyEvent.VK_LEFT:
                    selectedMainMenuOption = (selectedMainMenuOption - 1 + 3) % 3;
                    break;
                case KeyEvent.VK_RIGHT:
                    selectedMainMenuOption = (selectedMainMenuOption + 1) % 3;
                    break;
                case KeyEvent.VK_UP:
                    selectedMainMenuOption = (selectedMainMenuOption - 1 + 3) % 3;
                    break;
                case KeyEvent.VK_DOWN:
                    selectedMainMenuOption = (selectedMainMenuOption + 1) % 3;
                    break;
                case KeyEvent.VK_ENTER:
                    if (selectedMainMenuOption == 0) {
                        startNewGame();
                    } else if (selectedMainMenuOption == 1) {
                        showLoadMenu = true;
                        selectedLoadSlot = 0;
                    } else if (selectedMainMenuOption == 2) {
                        // EXIT 按鈕
                        System.exit(0);
                    }
                    break;
            }
            repaint();
            return;
        }
        
        if (state == 2) {
            // 結算畫面中：按任意鍵返回地圖
            state = 0;
            currentEnemies.clear();
            originalBattleEnemies.clear(); // 清除原始敵人追蹤
            triggeredEnemy = null;  // 清除觸發敵人記錄
            isZeroExpSettlement = false; // 重置無經驗結算標記
            previewDrops = ""; // 清除掉落物品預覽
            
            // 清除受傷血條顯示
            damagedEnemyIndex = -1;
            damagedEnemyTicks = 0;
            playerTakingDamage = false;
            playerDamageTicks = 0;
            for (int i = 0; i < companionTakingDamage.length; i++) {
                companionTakingDamage[i] = 0;
            }
            updateMapMusic(); // 從結算返回地圖時恢復地圖音樂
            return;
        }
        
        // 地圖選單導航
        if (state == 0 && showMapMenu) {
            int key = e.getKeyCode();
            switch (key) {
                case KeyEvent.VK_ESCAPE:
                    showMapMenu = false;
                    hoveredMapTab = -1;
                    return;
                case KeyEvent.VK_LEFT:
                    selectedMapTab = (selectedMapTab - 1 + 3) % 3;
                    return;
                case KeyEvent.VK_RIGHT:
                    selectedMapTab = (selectedMapTab + 1) % 3;
                    return;
                case KeyEvent.VK_UP:
                    if (selectedMapTab == 2) {
                        selectedSaveSlot = (selectedSaveSlot - 1 + MAX_SAVE_SLOTS) % MAX_SAVE_SLOTS;
                    }
                    return;
                case KeyEvent.VK_DOWN:
                    if (selectedMapTab == 2) {
                        selectedSaveSlot = (selectedSaveSlot + 1) % MAX_SAVE_SLOTS;
                    }
                    return;
                case KeyEvent.VK_ENTER:
                    if (selectedMapTab == 2) {
                        saveGame(selectedSaveSlot);
                    }
                    return;
                default:
                    return;
            }
        }
        
        if (state == 0) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_LEFT:
                    player.vx = -player.speed;
                    player.vy = 0;
                    playerFacingRight = false;
                    mouseDown = false;
                    keyDown = true;
                    break;
                case KeyEvent.VK_RIGHT:
                    player.vx = player.speed;
                    player.vy = 0;
                    playerFacingRight = true;
                    mouseDown = false;
                    keyDown = true;
                    break;
                case KeyEvent.VK_UP:
                    player.vx = 0;
                    player.vy = -player.speed;
                    mouseDown = false;
                    keyDown = true;
                    break;
                case KeyEvent.VK_DOWN:
                    player.vx = 0;
                    player.vy = player.speed;
                    mouseDown = false;
                    keyDown = true;
                    break;
            }
        } else if (state == 1 && currentEnemies.size() > 0) {
            // 如果顯示逃跑訊息，按任意鍵關閉
            if (showingFleeMessage) {
                showingFleeMessage = false;
                if (fleeMessage.contains("成功")) {
                    // 逃跑成功：進入結算畫面（敵人已在逃跑時鎖定7秒）
                    isZeroExpSettlement = true;
                    lastBattleExp = 0;
                    expForNextLevel = 0;
                    levelsGained = 0;
                    for (int i = 0; i < companionLevelsGained.length; i++) {
                        companionLevelsGained[i] = 0;
                        companionExpForNextLevel[i] = 0;
                    }
                    previewDrops = ""; // 敵人逃脫時清除掉落物品預覽
                    state = 2; // 進入結算畫面
                    playBattleEndMusic();  // 撥放結算音樂
                    currentEnemies.clear();
                    triggeredEnemy = null;  // 清除觸發敵人記錄
                    currentActor = null;
                    waitingForPlayerDecision = false;
                } else {
                    // 逃跑失敗，推進到下一個行動者
                    completeActionAndRefreshBattleOrder(30);
                }
                repaint();
                return;
            }

            if (!animating && waitingForPlayerDecision) {
            int key = e.getKeyCode();
            
            if (key == KeyEvent.VK_A) { // 攻擊：進入目標選擇模式
                selectingTargetMode = "attack";
            } else if (key == KeyEvent.VK_S) { // 戰技菜單
                showSkillMenuForTargeting();
            } else if (key == KeyEvent.VK_H) { // 使用藥水
                showPotionMenuForTargeting();
            } else if (key == KeyEvent.VK_R) { // 逃跑（玩家或隊友）
                if (currentActor instanceof Player || currentActor instanceof Companion) {
                    if (Math.random() < 0.6) {
                        fleeMessage = "逃跑成功！敵人短暫失去戰意。";
                        showingFleeMessage = true;
                        // 鎖定所有原始敵人7秒，防止立即重新戰鬥
                        for (Enemy origEnemy : originalBattleEnemies) {
                            origEnemy.setBattleLock(7000);  // 7秒鎖定
                        }
                    } else {
                        fleeMessage = "逃跑失敗！";
                        showingFleeMessage = true;
                    }
                }
            }
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (state == 0) {
            player.vx = 0;
            player.vy = 0;
            keyDown = false;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    // ---------- mouse listener methods ----------
    @Override
    public void mousePressed(MouseEvent e) {
        Point p = e.getPoint();
        
        // 加載菜單點擊處理（優先處理）
        if (showLoadMenu) {
            for (int i = 0; i < MAX_SAVE_SLOTS; i++) {
                int slotX = getWidth() / 2 - 150 + 20;
                int slotY = getHeight() / 2 - 125 + 50 + i * 50;
                Rectangle slotRect = new Rectangle(slotX, slotY, 260, 40);
                
                if (slotRect.contains(p)) {
                    selectedLoadSlot = i;
                    loadGame(selectedLoadSlot);
                    showLoadMenu = false;
                    return;
                }
            }
            return;
        }
        
        // 開始菜單點擊處理
        if (state == -1) {
            if (newGameRect != null && newGameRect.contains(p)) {
                selectedMainMenuOption = 0;
                startNewGame();
                return;
            }
            if (loadGameRect != null && loadGameRect.contains(p)) {
                selectedMainMenuOption = 1;
                showLoadMenu = true;
                selectedLoadSlot = 0;
                repaint();
                return;
            }
            if (exitRect != null && exitRect.contains(p)) {
                selectedMainMenuOption = 2;
                System.exit(0);
                return;
            }
        }
        
        if (state == 2) {
            // 結算畫面中：點擊返回地圖
            state = 0;
            currentEnemies.clear();
            originalBattleEnemies.clear(); // 清除原始敵人追蹤
            triggeredEnemy = null;  // 清除觸發敵人記錄
            previewDrops = ""; // 清除掉落物品預覽
            updateMapMusic(); // 從結算返回地圖時恢復地圖音樂
            return;
        }
        if (state == 0) {
            if (menuRect != null && menuRect.contains(p)) {
                showMapMenu = !showMapMenu;
                if (showMapMenu) {
                    selectedMapTab = 0;
                    selectedStatusActor = -1;
                } else {
                    hoveredMapTab = -1;
                }
                return;
            }

            if (showMapMenu) {
                if (statusTabRect != null && statusTabRect.contains(p)) {
                    selectedMapTab = 0;
                    return;
                }
                if (bagTabRect != null && bagTabRect.contains(p)) {
                    selectedMapTab = 1;
                    bagScrollOffset = 0;
                    return;
                }
                if (saveTabRect != null && saveTabRect.contains(p)) {
                    selectedMapTab = 2;
                    return;
                }

                if (selectedMapTab == 0) {
                    if (statusPlayerSelectorRect != null && statusPlayerSelectorRect.contains(p)) {
                        selectedStatusActor = -1;
                        return;
                    }
                    for (int i = 0; i < statusCompanionSelectorRects.length; i++) {
                        Rectangle selectorRect = statusCompanionSelectorRects[i];
                        if (selectorRect != null && selectorRect.contains(p)) {
                            selectedStatusActor = i;
                            return;
                        }
                    }
                }

                if (selectedMapTab == 2 && mapMenuPanelRect != null && mapMenuPanelRect.contains(p)) {
                    int panelX = mapMenuPanelRect.x;
                    int panelY = mapMenuPanelRect.y;
                    int panelW = mapMenuPanelRect.width;
                    int panelH = mapMenuPanelRect.height;

                    int tabY = panelY + 10;
                    int tabH = 38;
                    int contentX = panelX + 14;
                    int contentY = tabY + tabH + 10;
                    int contentW = panelW - 28;
                    int slotX = contentX + 12;
                    int slotYBase = contentY + 16;
                    int slotW = contentW - 24;
                    int slotH = 76;

                    for (int i = 0; i < MAX_SAVE_SLOTS; i++) {
                        int slotY = slotYBase + i * (slotH + 8);
                        Rectangle slotRect = new Rectangle(slotX, slotY, slotW, slotH);
                        if (slotRect.contains(p)) {
                            selectedSaveSlot = i;
                            saveGame(selectedSaveSlot);
                            return;
                        }
                    }
                }

                // 選單開啟時，點擊內容區外不觸發移動
                return;
            }
        }
        // 戰鬥中顯示逃跑訊息時，點擊任何位置關閉
        if (state == 1 && showingFleeMessage) {
            showingFleeMessage = false;
            if (fleeMessage.contains("成功")) {
                // 逃跑成功：進入結算畫面（敵人已在逃跑時鎖定7秒）
                state = 2; // 進入結算畫面
                isZeroExpSettlement = true;
                lastBattleExp = 0;
                expForNextLevel = 0;
                levelsGained = 0;
                for (int i = 0; i < companionLevelsGained.length; i++) {
                    companionLevelsGained[i] = 0;
                    companionExpForNextLevel[i] = 0;
                }
                previewDrops = "";
                playBattleEndMusic();
                currentEnemies.clear();
                triggeredEnemy = null;  // 清除觸發敵人記錄
                currentActor = null;
                waitingForPlayerDecision = false;
                updateMapMusic();
            } else {
                // 逃跑失敗，推進到下一個行動者
                completeActionAndRefreshBattleOrder(30);
            }
            repaint();
            return;
        }
        if (state == 1 && !animating && waitingForPlayerDecision) {
            // 檢查鼠標按鍵
            int button = e.getButton();
            
            // 右鍵取消目標選擇模式
            if (button == MouseEvent.BUTTON3) {
                selectingTargetMode = "";
                selectingPotionType = "";
                selectedSkill = null;
                repaint();
                return;
            }
            
            // 左鍵進行目標選擇
            if (button != MouseEvent.BUTTON1) {
                return;
            }
            
            // 如果正在選擇目標，檢查點擊的目標
            if (!selectingTargetMode.isEmpty()) {
                // 計算滑鼠距離各個角色的距離
                int mouseX = (int) p.getX();
                int mouseY = (int) p.getY();
                
                if ("attack".equals(selectingTargetMode)) {
                    // 攻擊模式：只能選擇敵人
                    int targetIndex = findClosestEnemyIndexAt(mouseX, mouseY, 28);
                    if (targetIndex >= 0) {
                        targetingEnemyIndex = targetIndex;  // 記錄目標敵人索引
                        executeAttackAnimation();
                        return;
                    }
                    return;
                } else if ("skill".equals(selectingTargetMode)) {
                    // 戰技模式：可以選擇敵人或自己的隊伍
                    // 先檢查敵人
                    int targetIndex = findClosestEnemyIndexAt(mouseX, mouseY, 28);
                    if (targetIndex >= 0) {
                        executeSkillAnimation(currentEnemies.get(targetIndex));
                        return;
                    }
                    
                    // 檢查玩家
                    double dx = mouseX - battleScreenPlayerX;
                    double dy = mouseY - battleScreenPlayerY;
                    double dist = Math.hypot(dx, dy);
                    if (dist < 40) {
                        executeSkillAnimation(player);
                        return;
                    }
                    
                    // 檢查隊友
                    for (int i = 0; i < companions.size(); i++) {
                        dx = mouseX - battleScreenCompanionX[i];
                        dy = mouseY - battleScreenCompanionY[i];
                        dist = Math.hypot(dx, dy);
                        if (dist < 40) {
                            executeSkillAnimation(companions.get(i));
                            return;
                        }
                    }
                    return;
                } else if ("potion".equals(selectingTargetMode)) {
                    // 用藥模式：可以選擇玩家或隊友（自己人）
                    
                    // 檢查玩家
                    double dx = mouseX - battleScreenPlayerX;
                    double dy = mouseY - battleScreenPlayerY;
                    double dist = Math.hypot(dx, dy);
                    if (dist < 40) {
                        executePotionAnimation(player);
                        return;
                    }
                    
                    // 檢查隊友
                    for (int i = 0; i < companions.size(); i++) {
                        dx = mouseX - battleScreenCompanionX[i];
                        dy = mouseY - battleScreenCompanionY[i];
                        dist = Math.hypot(dx, dy);
                        if (dist < 40) {
                            executePotionAnimation(companions.get(i));
                            return;
                        }
                    }
                    return;
                }
                return;
            }
            
            // 按鈕點擊處理
            if (attackRect != null && attackRect.contains(p)) {
                selectingTargetMode = "attack";  // 進入攻擊目標選擇模式
                return;
            } else if (skillRect != null && skillRect.contains(p)) {
                showSkillMenuForTargeting();
                return;
            } else if (healRect != null && healRect.contains(p)) {
                showPotionMenuForTargeting();
                return;
            } else if (runRect != null && runRect.contains(p)) {
                if (currentActor instanceof Player || currentActor instanceof Companion) {
                    if (Math.random() < 0.6) {
                        fleeMessage = "逃跑成功！敵人短暫失去戰意。";
                        showingFleeMessage = true;
                        // 鎖定所有原始敵人7秒，防止立即重新戰鬥
                        for (Enemy origEnemy : originalBattleEnemies) {
                            origEnemy.setBattleLock(7000);  // 7秒鎖定
                        }
                    } else {
                        fleeMessage = "逃跑失敗！";
                        showingFleeMessage = true;
                    }
                }
                return;
            }
        }
            
        mouseDown = true;
        updateMouseDirection(e);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        mouseDown = false;
        player.vx = 0;
        player.vy = 0;
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (mouseDown) {
            updateMouseDirection(e);
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        int mouseX = e.getX();
        int mouseY = e.getY();

        // 地圖介面分頁 hover
        if (state == 0 && showMapMenu) {
            hoveredMapTab = -1;
            if (statusTabRect != null && statusTabRect.contains(mouseX, mouseY)) {
                hoveredMapTab = 0;
            } else if (bagTabRect != null && bagTabRect.contains(mouseX, mouseY)) {
                hoveredMapTab = 1;
            } else if (saveTabRect != null && saveTabRect.contains(mouseX, mouseY)) {
                hoveredMapTab = 2;
            }
        } else {
            hoveredMapTab = -1;
        }
        
        // 在戰鬥中檢測鼠標是否懸停在敵人上
        if (state == 1) {
            hoveredEnemyIndex = -1;  // 默認無敵人懸停
            
            for (int i = 0; i < currentEnemies.size(); i++) {
                int enemyX = battleScreenEnemyX[i];
                int enemyY = battleScreenEnemyY[i];
                
                // 檢查鼠標是否在敵人圓形範圍內（半徑12）
                double dx = mouseX - enemyX;
                double dy = mouseY - enemyY;
                double distance = Math.sqrt(dx * dx + dy * dy);
                
                if (distance <= 15) {  // 稍大一點的範圍便於選中
                    hoveredEnemyIndex = i;
                    break;
                }
            }
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
        hoveredEnemyIndex = -1;  // 鼠標離開時清除懸停狀態
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        if (state == 0 && showMapMenu && selectedMapTab == 1) {
            // 背包滾動
            int maxScroll = Math.max(0, 2 - BAG_VISIBLE_ITEMS);  // 2項物品
            bagScrollOffset -= e.getWheelRotation();
            bagScrollOffset = Math.max(0, Math.min(bagScrollOffset, maxScroll));
        }
    }

    private void updateMouseDirection(MouseEvent e) {
        int mx = e.getX() / TILE_SIZE;
        int my = e.getY() / TILE_SIZE;
        mouseGridX = mx;
        mouseGridY = my;
        // 計算與玩家的相對位置，更新速度方向
        int dirX = Integer.compare(mx, (int) (player.x / TILE_SIZE));
        int dirY = Integer.compare(my, (int) (player.y / TILE_SIZE));
        player.vx = dirX * player.speed;
        player.vy = dirY * player.speed;
    }

    // ── 碰撞 & 地圖 helpers ──────────────────────────────────────────
    private boolean hitWall(double px, double py) {
        int r = 10;
        int cx = (int) (px + 20), cy = (int) (py + 20);
        return isWallPixel(cx - r, cy - r) || isWallPixel(cx + r, cy - r)
                || isWallPixel(cx - r, cy + r) || isWallPixel(cx + r, cy + r);
    }

    private boolean isWallPixel(int px, int py) {
        int tx = px / TILE_SIZE, ty = py / TILE_SIZE;
        if (tx < 0 || ty < 0 || tx >= map[0].length || ty >= map.length)
            return true;
        return map[ty][tx] == 1;
    }

    private void checkPortal() {
        double cx = player.x + 20, cy = player.y + 20;
        boolean inPortalRows = cy >= 7 * TILE_SIZE && cy <= 9 * TILE_SIZE;
        if (mapIndex == 0 && inPortalRows && cx >= 19 * TILE_SIZE) {
            switchMap(1, 1 * TILE_SIZE, 7 * TILE_SIZE + 10);
        } else if (mapIndex == 1 && inPortalRows && cx <= TILE_SIZE) {
            switchMap(0, 18 * TILE_SIZE, 7 * TILE_SIZE + 10);
        } else if (mapIndex == 1 && inPortalRows && cx >= 19 * TILE_SIZE) {
            switchMap(2, 1 * TILE_SIZE, 7 * TILE_SIZE + 10);
        } else if (mapIndex == 2 && inPortalRows && cx <= TILE_SIZE) {
            switchMap(1, 18 * TILE_SIZE, 7 * TILE_SIZE + 10);
        }
    }

    private void switchMap(int newIdx, double spawnX, double spawnY) {
        // 清空原始敵人追蹤（避免舊地圖敵人幹擾）
        originalBattleEnemies.clear();
        
        mapIndex = newIdx;
        map = allMaps[mapIndex];
        // 重新進入地圖時重新生成敵人
        allEnemies.set(mapIndex, spawnEnemiesForMap(mapIndex));
        enemies = allEnemies.get(mapIndex);
        player.x = spawnX;
        player.y = spawnY;
        player.vx = 0;
        player.vy = 0;
        // 隊友跟隨到相同位置
        for (Companion c : companions) {
            c.x = spawnX;
            c.y = spawnY;
            c.vx = 0;
            c.vy = 0;
        }
        // 清空玩家路徑歷史（新地圖需要重新記錄）
        playerPathHistory.clear();
        updateMapMusic();
    }

    // 隨機生成1-3個敵人進入戰鬥
    private void spawnRandomEnemies() {
        currentEnemies.clear();
        enemyBattleSlots.clear();
        originalBattleEnemies.clear();  // 清除之前的原始敵人追蹤
        
        boolean allowExtraEnemies = true;
        // 觸發敵人必定會參與戰鬥
        if (triggeredEnemy != null && !triggeredEnemy.isDefeated() && !triggeredEnemy.isBattleLocked()) {
            Enemy newEnemy = new Enemy(triggeredEnemy);
            triggeredEnemy.setBattleLock(Long.MAX_VALUE);  // 立即鎖定原始敵人，防止重複戰鬥
            originalBattleEnemies.add(triggeredEnemy);  // 追蹤觸發敵人，以便戰鬥結束時標記
            currentEnemies.add(newEnemy);
            enemyBattleSlots.add(enemyBattleSlots.size());
            allowExtraEnemies = !triggeredEnemy.isBoss;
        }
        
        // 隨機生成0-2個額外敵人（虛擬敵人，不與地圖敵人關聯）
        if (allowExtraEnemies) {
            int additionalEnemyCount = (int)(Math.random() * 3);  // 0-2 個額外敵人
            for (int i = 0; i < additionalEnemyCount; i++) {
                // 隨機生成虛擬敵人，不涉及地圖敵人
                Enemy virtualEnemy = new Enemy((int)(Math.random() * 15), (int)(Math.random() * 10));
                currentEnemies.add(virtualEnemy);
                enemyBattleSlots.add(enemyBattleSlots.size());
            }
        }

        // 每次新戰鬥都重建初始行動順序
        initBattleOrder();
    }

    private int getEnemyBattleSlot(int enemyIndex) {
        if (enemyIndex >= 0 && enemyIndex < enemyBattleSlots.size()) {
            return enemyBattleSlots.get(enemyIndex);
        }
        return enemyIndex;
    }

    private void removeEnemyAt(int enemyIndex) {
        if (enemyIndex < 0 || enemyIndex >= currentEnemies.size()) {
            return;
        }
        Enemy removedEnemy = currentEnemies.remove(enemyIndex);
        if (enemyIndex < enemyBattleSlots.size()) {
            enemyBattleSlots.remove(enemyIndex);
        }
        if (removedEnemy != null) {
            for (int i = battleOrder.size() - 1; i >= 0; i--) {
                if (battleOrder.get(i).unit == removedEnemy) {
                    battleOrder.remove(i);
                }
            }
        }
    }

    private void removeEnemy(Enemy enemy) {
        int idx = currentEnemies.indexOf(enemy);
        if (idx >= 0) {
            removeEnemyAt(idx);
        }
    }

    // 生成掉落物品預覽（戰鬥中顯示，不修改玩家物品）
    private String generateDropsPreview() {
        double rand = Math.random();
        if (rand < 0.3) {
            return "小藥水 x1";
        } else if (rand < 0.6) {
            return "大藥水 x1";
        } else if (rand < 0.85) {
            int goldAmount = 30 + (int)(Math.random() * 40); // 30-69之間隨機
            return "金幣 x" + goldAmount;
        }
        return ""; // 20%的機率沒有物品掉落
    }

    // 根據預覽字符串應用掉落物品
    private void applyDropsFromPreview() {
        if (previewDrops.isEmpty()) {
            return;
        }
        
        if (previewDrops.contains("小藥水")) {
            player.smallPotions += 1;
        } else if (previewDrops.contains("大藥水")) {
            player.largePotions += 1;
        } else if (previewDrops.contains("金幣")) {
            // 從預覽字符串中提取金幣數量
            int startIdx = previewDrops.indexOf("x") + 1;
            if (startIdx > 0) {
                try {
                    int goldAmount = Integer.parseInt(previewDrops.substring(startIdx).trim());
                    player.gold += goldAmount;
                } catch (NumberFormatException e) {
                    // 如果解析失敗，默認加50
                    player.gold += 50;
                }
            }
        }
    }

    private String formatTimeAgo(long milliseconds) {
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

    private File getSaveFile(int slot) {
        File saveDir = new File(SAVE_DIR_NAME);
        if (!saveDir.exists()) {
            saveDir.mkdirs();
        }
        return new File(saveDir, SAVE_FILE_PREFIX + slot + SAVE_FILE_EXT);
    }

    private boolean isPlayerAlive() {
        return player != null && player.hp > 0;
    }

    private boolean areAllAlliesDefeated() {
        if (isPlayerAlive()) {
            return false;
        }
        for (Companion c : companions) {
            if (c.hp > 0) {
                return false;
            }
        }
        return true;
    }

    int getCurrentSkillCasterPatk(Player fallbackPlayer) {
        if (currentActor instanceof Companion) {
            return ((Companion) currentActor).patk;
        }
        if (currentActor instanceof Player) {
            return ((Player) currentActor).patk;
        }
        if (fallbackPlayer != null) {
            return fallbackPlayer.patk;
        }
        return player != null ? player.patk : 1;
    }

    void dealFlatDamageToAllEnemies(int damage) {
        int finalDamage = Math.max(1, damage);
        for (Enemy enemy : currentEnemies) {
            enemy.hp = Math.max(0, enemy.hp - finalDamage);
            enemy.cp = Math.min(enemy.cp + 5, enemy.maxCp);
        }
    }

    private void removeDefeatedEnemiesFromBattle() {
        for (int i = currentEnemies.size() - 1; i >= 0; i--) {
            Enemy enemy = currentEnemies.get(i);
            if (enemy.hp <= 0) {
                enemy.defeated = true;
                removeEnemyAt(i);
            }
        }

        if (targetingEnemyIndex >= currentEnemies.size()) {
            targetingEnemyIndex = currentEnemies.isEmpty() ? -1 : currentEnemies.size() - 1;
        }
    }

    private boolean tryEnterVictorySettlementIfNoEnemies() {
        if (!currentEnemies.isEmpty()) {
            return false;
        }

        for (Enemy origEnemy : originalBattleEnemies) {
            origEnemy.defeated = true;
        }
        originalBattleEnemies.clear();

        isZeroExpSettlement = false;
        int totalExp = 60;
        levelsGained = player.gainExp(totalExp / 2);
        for (int i = 0; i < companions.size(); i++) {
            Companion c = companions.get(i);
            companionLevelsGained[i] = c.gainExp(totalExp / 2);
            companionExpForNextLevel[i] = Math.max(0, c.expToNext - c.exp);
        }
        lastBattleExp = totalExp;
        expForNextLevel = Math.max(0, player.expToNext - player.exp);
        applyDropsFromPreview();
        settlementDrops = previewDrops.isEmpty() ? "" : "掉落物品: " + previewDrops;
        state = 2;
        playBattleEndMusic();
        animating = false;
        animTicks = 0;
        currentActor = null;
        waitingForPlayerDecision = false;
        repaint();
        return true;
    }

    private void chooseEnemyAttackTarget() {
        enemyAttackTargetIsPlayer = true;
        enemyAttackTargetCompanionIndex = -1;

        java.util.List<Integer> aliveCompanionIndexes = new java.util.ArrayList<>();
        for (int i = 0; i < companions.size(); i++) {
            if (companions.get(i).hp > 0) {
                aliveCompanionIndexes.add(i);
            }
        }

        boolean playerAlive = isPlayerAlive();
        if (playerAlive && !aliveCompanionIndexes.isEmpty()) {
            // 玩家與隊友都存活時，玩家 70%，隊友 30%
            if (Math.random() < 0.7) {
                enemyAttackTargetIsPlayer = true;
            } else {
                enemyAttackTargetIsPlayer = false;
                enemyAttackTargetCompanionIndex = aliveCompanionIndexes
                        .get((int) (Math.random() * aliveCompanionIndexes.size()));
            }
        } else if (playerAlive) {
            enemyAttackTargetIsPlayer = true;
        } else if (!aliveCompanionIndexes.isEmpty()) {
            enemyAttackTargetIsPlayer = false;
            enemyAttackTargetCompanionIndex = aliveCompanionIndexes
                    .get((int) (Math.random() * aliveCompanionIndexes.size()));
        }
    }

    private Object getEnemyAttackTargetUnit() {
        if (enemyAttackTargetIsPlayer) {
            return isPlayerAlive() ? player : null;
        }
        if (enemyAttackTargetCompanionIndex >= 0 && enemyAttackTargetCompanionIndex < companions.size()) {
            Companion c = companions.get(enemyAttackTargetCompanionIndex);
            return c.hp > 0 ? c : null;
        }
        return null;
    }

    void healAllAlliesByPercent(double percent) {
        int playerHeal = (int) Math.ceil(player.maxHp * percent);
        player.heal(playerHeal);
        playerHealGlowTicks = HEAL_GLOW_DURATION;

        for (int i = 0; i < companions.size(); i++) {
            Companion c = companions.get(i);
            int companionHeal = (int) Math.ceil(c.maxHp * percent);
            c.heal(companionHeal);
            if (i < companionHealGlowTicks.length) {
                companionHealGlowTicks[i] = HEAL_GLOW_DURATION;
            }
        }
    }

    private boolean isHealingSkillAnimation() {
        return animating && "playerAttack".equals(animAction) && selectedSkill instanceof SkillMoonlight;
    }

    private Color blendToWhite(Color base, double ratio) {
        double t = Math.max(0.0, Math.min(1.0, ratio));
        int r = (int) Math.round(base.getRed() + (255 - base.getRed()) * t);
        int g = (int) Math.round(base.getGreen() + (255 - base.getGreen()) * t);
        int b = (int) Math.round(base.getBlue() + (255 - base.getBlue()) * t);
        return new Color(r, g, b);
    }

    private void drawHealingAura(Graphics2D g2d, int centerX, int centerY) {
        double progress = Math.max(0.0, Math.min(1.0, animTicks / (double) ANIM_DURATION));
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
        double progress = Math.max(0.0, Math.min(1.0, animTicks / (double) ANIM_DURATION));
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

    private void handlePartyDefeatReturnToMainMenu() {
        JOptionPane.showMessageDialog(this, "全隊被打敗了，返回主畫面。", "戰鬥失敗", JOptionPane.INFORMATION_MESSAGE);

        // 清理戰鬥狀態，返回主選單
        animating = false;
        animTicks = 0;
        animAction = "";
        currentActor = null;
        waitingForPlayerDecision = false;
        currentActionIndex = 0;
        currentAttackingEnemyIndex = -1;
        enemyAttackTargetIsPlayer = true;
        enemyAttackTargetCompanionIndex = -1;
        targetingEnemyIndex = -1;
        hoveredEnemyIndex = -1;
        damagedEnemyIndex = -1;
        damagedEnemyTicks = 0;
        playerTakingDamage = false;
        playerDamageTicks = 0;
        showingFleeMessage = false;
        fleeMessage = "";
        selectingTargetMode = "";
        selectingPotionType = "";
        selectedSkill = null;

        currentEnemies.clear();
        enemyBattleSlots.clear();
        originalBattleEnemies.clear();
        battleOrder.clear();
        triggeredEnemy = null;
        previewDrops = "";
        settlementDrops = "";

        stopCurrentMusic();
        state = -1;
    }

    private void updateMapMusic() {
        if (mapIndex == 0) {
            playLoopingMapMusic("ED6101.wav");
        } else if (mapIndex == 1) {
            playLoopingMapMusic("ED6106.wav");
        } else if (mapIndex == 2) {
            playLoopingMapMusic("ED6106.wav");
        } else {
            stopCurrentMusic();
        }
    }

    private void playBattleMusic() {
        playLoopingMapMusic("ED6400.wav");
    }
    
    private void playBattleEndMusic() {
        playOneshotMusic("battle end.wav");
    }
    
    private void playOneshotMusic(String fileName) {
        stopCurrentMusic();
        
        File musicFile = new File("resources" + File.separator + fileName);
        System.out.println("嘗試播放結算音樂: " + fileName);
        System.out.println("檔案存在: " + musicFile.exists());
        System.out.println("檔案路徑: " + musicFile.getAbsolutePath());
        
        if (!musicFile.exists()) {
            System.err.println("找不到音樂檔案: " + fileName);
            return;
        }
        
        try {
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(musicFile);
            bgmClip = AudioSystem.getClip();
            bgmClip.open(audioStream);
            bgmClip.start();  // 只播放一次，不循環
            currentMusicFile = fileName;
            System.out.println("結算音樂已開始播放");
        } catch (Exception e) {
            System.err.println("無法播放音樂檔案: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void playLoopingMapMusic(String fileName) {
        if (fileName.equals(currentMusicFile) && bgmClip != null && bgmClip.isRunning()) {
            return;
        }

        stopCurrentMusic();

        File musicFile = new File("resources" + File.separator + fileName);
        if (!musicFile.exists()) {
            System.err.println("找不到地圖音樂檔案: " + fileName);
            return;
        }

        try {
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(musicFile);
            bgmClip = AudioSystem.getClip();
            bgmClip.open(audioStream);
            bgmClip.loop(Clip.LOOP_CONTINUOUSLY);
            bgmClip.start();
            currentMusicFile = fileName;
        } catch (Exception e) {
            System.err.println("無法播放音樂檔案: " + e.getMessage());
        }
    }
    private void stopCurrentMusic() {
        if (bgmClip != null) {
            if (bgmClip.isRunning()) {
                bgmClip.stop();
            }
            bgmClip.close();
            bgmClip = null;
        }
        currentMusicFile = "";
    }

    private BufferedImage loadBattlePlayerSprite() {
        try {
            File spriteFile = new File("resources" + File.separator + "正右.png");
            if (spriteFile.exists()) {
                return ImageIO.read(spriteFile);
            }
        } catch (IOException ignored) {
        }
        return null;
    }

    public void stopBackgroundMusic() {
        stopCurrentMusic();
    }

    // ────── 開始菜單 ──────────────────────────────────────────
    /**
     * 繪製開始菜單
     */
    private void drawMainMenu(Graphics2D g2d) {
        int width = getWidth();
        int height = getHeight();
        
        // 背景
        g2d.setColor(new Color(10, 10, 30));
        g2d.fillRect(0, 0, width, height);
        
        // 標題
        g2d.setFont(new Font("Microsoft JhengHei", Font.BOLD, 60));
        g2d.setColor(new Color(100, 200, 255));
        String title = "RPG 冒險";
        FontMetrics fm = g2d.getFontMetrics();
        int titleX = (width - fm.stringWidth(title)) / 2;
        g2d.drawString(title, titleX, height / 3);
        
        // 菜單選項
        int optionY = height / 2 + 50;
        int optionSpacing = 80;
        
        // NEW 遊戲按鈕
        int newGameX = width / 2 - 150;
        int newGameY = optionY;
        newGameRect = new Rectangle(newGameX, newGameY, 120, 50);
        
        // LOAD 遊戲按鈕
        int loadGameX = width / 2 + 30;
        int loadGameY = optionY;
        loadGameRect = new Rectangle(loadGameX, loadGameY, 120, 50);
        
        // EXIT 按鈕
        int exitX = width / 2 - 60;
        int exitY = optionY + optionSpacing;
        exitRect = new Rectangle(exitX, exitY, 120, 50);
        
        // 繪製按鈕
        drawMenuButton(g2d, newGameRect, "NEW", selectedMainMenuOption == 0);
        drawMenuButton(g2d, loadGameRect, "LOAD", selectedMainMenuOption == 1);
        drawMenuButton(g2d, exitRect, "EXIT", selectedMainMenuOption == 2);
        
        // 提示文字
        g2d.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 14));
        g2d.setColor(new Color(200, 200, 200));
        g2d.drawString("← → 方向鍵選擇   ENTER 確認", width / 2 - 120, height - 50);
    }
    
    /**
     * 繪製加載菜單
     */
    private void drawLoadMenu(Graphics2D g2d) {
        int width = getWidth();
        int height = getHeight();
        
        // 半透明背景
        g2d.setColor(new Color(0, 0, 0, 150));
        g2d.fillRect(0, 0, width, height);
        
        // 菜單框
        int menuW = 300;
        int menuH = 250;
        int menuX = (width - menuW) / 2;
        int menuY = (height - menuH) / 2;
        
        g2d.setColor(new Color(50, 50, 100, 250));
        g2d.fillRoundRect(menuX, menuY, menuW, menuH, 10, 10);
        g2d.setColor(new Color(100, 150, 255));
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRoundRect(menuX, menuY, menuW, menuH, 10, 10);
        
        // 標題
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Microsoft JhengHei", Font.BOLD, 16));
        g2d.drawString("選擇加載槽位", menuX + 80, menuY + 30);
        
        // 繪製存檔槽位選項
        for (int i = 0; i < MAX_SAVE_SLOTS; i++) {
            int slotX = menuX + 20;
            int slotY = menuY + 50 + i * 50;
            int slotW = menuW - 40;
            int slotH = 40;
            
            g2d.setColor(i == selectedLoadSlot ? new Color(255, 200, 0) : new Color(100, 100, 100));
            g2d.fillRoundRect(slotX, slotY, slotW, slotH, 5, 5);
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 13));
            
            // 檢查槽位是否有存檔
            File saveFile = getSaveFile(i);
            if (saveFile.exists()) {
                g2d.drawString("槽位 " + (i + 1) + " ✓", slotX + 10, slotY + 27);
            } else {
                g2d.drawString("槽位 " + (i + 1) + " (空)", slotX + 10, slotY + 27);
            }
        }
        
        // 提示文字
        g2d.setColor(Color.YELLOW);
        g2d.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 11));
        g2d.drawString("↑↓ 選擇  ENTER 確認  ESC 返回", menuX + 15, menuY + menuH - 15);
    }
    
    /**
     * 繪製菜單按鈕
     */
    private void drawMenuButton(Graphics2D g2d, Rectangle rect, String text, boolean selected) {
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
    
    /**
     * 初始化新遊戲
     */
    private void startNewGame() {
        state = 0;  // 切換到遊戲狀態

        // 重新建立角色，確保 NEW 時固定為 Lv1、滿 HP/MP、CP=0
        player = new Player(2 * TILE_SIZE, 2 * TILE_SIZE);
        companions.clear();
        companions.add(new Companion(3 * TILE_SIZE, 2 * TILE_SIZE, "月"));
        
        // 重置地圖
        mapIndex = 0;
        map = allMaps[mapIndex];
        allEnemies.set(mapIndex, spawnEnemiesForMap(mapIndex));
        enemies = allEnemies.get(mapIndex);

        // 清空戰鬥狀態
        currentEnemies.clear();
        enemyBattleSlots.clear();
        originalBattleEnemies.clear();
        battleOrder.clear();
        triggeredEnemy = null;
        currentActor = null;
        waitingForPlayerDecision = false;
        currentActionIndex = 0;
        currentAttackingEnemyIndex = -1;
        enemyAttackTargetIsPlayer = true;
        enemyAttackTargetCompanionIndex = -1;
        targetingEnemyIndex = -1;
        selectedSkill = null;
        selectingTargetMode = "";
        selectingPotionType = "";
        previewDrops = "";
        settlementDrops = "";
        showLoadMenu = false;
        showMapMenu = false;
        selectedMapTab = 0;
        selectedStatusActor = -1;
        
        // 恢復音樂
        updateMapMusic();
        
        repaint();
    }

    // ────── 保存/加載系統 ──────────────────────────────────────────
    /**
     * 保存遊戲進度到文件
     */
    private void saveGame(int slot) {
        try {
            GameState state = new GameState();
            
            // 保存玩家狀態
            state.playerX = player.x;
            state.playerY = player.y;
            state.playerLevel = player.level;
            state.playerExp = player.exp;
            state.playerHP = player.hp;
            state.playerMana = player.mana;
            state.playerCP = player.cp;
            state.playerMaxHP = player.maxHp;
            state.playerMaxMana = player.maxMana;
            state.playerMaxCP = player.maxCp;
            state.playerPatk = player.patk;
            state.playerPdef = player.pdef;
            state.playerMatk = player.matk;
            state.playerMdef = player.mdef;
            
            state.smallPotions = player.smallPotions;
            state.largePotions = player.largePotions;
            state.gold = player.gold;
            
            // 保存隊友狀態（假設只有一個隊友）
            if (!companions.isEmpty()) {
                Companion c = companions.get(0);
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
            
            // 保存地圖信息
            state.currentMapIndex = mapIndex;
            
            // 保存已擊敗的敵人（使用敵人的位置作為ID）
            int enemyId = 0;
            for (java.util.List<Enemy> enemyList : allEnemies) {
                for (Enemy e : enemyList) {
                    if (e.defeated) {
                        state.defeatedEnemies.put("enemy_" + enemyId, true);
                    }
                    enemyId++;
                }
            }
            
            // 序列化到文件
            File saveFile = getSaveFile(slot);
            String saveFileName = saveFile.getPath();
            FileOutputStream fos = new FileOutputStream(saveFile);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(state);
            oos.close();
            fos.close();
            
            System.out.println("遊戲已保存到槽位 " + (slot + 1) + " (" + saveFileName + ")");
            JOptionPane.showMessageDialog(this, "遊戲已保存到槽位 " + (slot + 1), "保存成功", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            System.err.println("保存遊戲時出錯: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "保存遊戲失敗: " + e.getMessage(), "保存失敗", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * 從文件加載遊戲進度
     */
    private void loadGame(int slot) {
        try {
            File saveFile = getSaveFile(slot);
            String saveFileName = saveFile.getPath();
            
            if (!saveFile.exists()) {
                JOptionPane.showMessageDialog(this, "槽位 " + (slot + 1) + " 沒有保存檔案", "無法加載", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            FileInputStream fis = new FileInputStream(saveFile);
            ObjectInputStream ois = new ObjectInputStream(fis);
            GameState gameStateData = (GameState) ois.readObject();
            ois.close();
            fis.close();
            
            // 恢復玩家狀態
            player.x = gameStateData.playerX;
            player.y = gameStateData.playerY;
            player.level = gameStateData.playerLevel;
            player.exp = gameStateData.playerExp;
            player.hp = gameStateData.playerHP;
            player.mana = gameStateData.playerMana;
            player.cp = gameStateData.playerCP;
            player.maxHp = gameStateData.playerMaxHP;
            player.maxMana = gameStateData.playerMaxMana;
            player.maxCp = gameStateData.playerMaxCP > 0 ? gameStateData.playerMaxCP : 200;
            player.cp = Math.min(player.cp, player.maxCp);
            player.patk = gameStateData.playerPatk;
            player.pdef = gameStateData.playerPdef;
            player.matk = gameStateData.playerMatk;
            player.mdef = gameStateData.playerMdef;
            
            player.smallPotions = gameStateData.smallPotions;
            player.largePotions = gameStateData.largePotions;
            player.gold = gameStateData.gold;
            
            // 恢復隊友狀態
            if (!companions.isEmpty()) {
                Companion c = companions.get(0);
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
            
            // 恢復地圖
            switchMap(gameStateData.currentMapIndex, gameStateData.playerX, gameStateData.playerY);
            
            // 恢復敵人擊敗狀態
            int enemyId = 0;
            for (java.util.List<Enemy> enemyList : allEnemies) {
                for (Enemy e : enemyList) {
                    if (gameStateData.defeatedEnemies.containsKey("enemy_" + enemyId)) {
                        e.defeated = true;
                    }
                    enemyId++;
                }
            }
            
            currentEnemies.clear();
            this.state = 0;
            repaint();
            
            System.out.println("遊戲已從槽位 " + (slot + 1) + " 加載（" + gameStateData.getSaveTimeString() + "）");
            JOptionPane.showMessageDialog(this, "遊戲已從槽位 " + (slot + 1) + " 加載\n保存時間: " + gameStateData.getSaveTimeString(), "加載成功", JOptionPane.INFORMATION_MESSAGE);
        } catch (ClassNotFoundException e) {
            System.err.println("無法識別保存檔案格式: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "保存檔案格式錯誤", "加載失敗", JOptionPane.ERROR_MESSAGE);
        } catch (IOException e) {
            System.err.println("加載遊戲時出錯: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "加載遊戲失敗: " + e.getMessage(), "加載失敗", JOptionPane.ERROR_MESSAGE);
        }
    }

    // 傷害小浮動：基礎傷害乘上 0.95 ~ 1.05
    private int rollDamage(int atk, int def) {
        int base = Math.max(1, atk - def);
        double factor = 0.95 + Math.random() * 0.10;
        return Math.max(1, (int) Math.round(base * factor));
    }

    // 初始化戰鬥行動順序
    private void initBattleOrder() {
        battleOrder.clear();
        currentActionIndex = 0;

        // 添加玩家
        BattleUnit playerUnit = new BattleUnit(player.battleSpeed, true, player);
        battleOrder.add(playerUnit);

        // 添加隊友
        for (Companion c : companions) {
            BattleUnit companionUnit = new BattleUnit(c.battleSpeed, true, c);
            battleOrder.add(companionUnit);
        }

        // 添加當前戰鬥中的所有敵人
        for (Enemy enemy : currentEnemies) {
            BattleUnit enemyUnit = new BattleUnit(enemy.speed, false, enemy);
            battleOrder.add(enemyUnit);
        }

        // 初始順序：SPD 越高越先手，等價於 AT 越低越先手
        sortBattleOrderByAt();
    }

    private void sortBattleOrderByAt() {
        battleOrder.sort((a, b) -> {
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

    private void removeInvisibleBattleUnits() {
        for (int i = battleOrder.size() - 1; i >= 0; i--) {
            if (!isBattleUnitVisible(battleOrder.get(i))) {
                battleOrder.remove(i);
            }
        }
    }

    private BattleUnit findBattleUnit(Object unit) {
        if (unit == null) {
            return null;
        }
        for (BattleUnit battleUnit : battleOrder) {
            if (battleUnit.unit == unit) {
                return battleUnit;
            }
        }
        return null;
    }

    private void completeActionAndRefreshBattleOrder(int actionCost) {
        BattleUnit actedUnit = findBattleUnit(currentActor);
        if (actedUnit != null) {
            actedUnit.addActionCost(actionCost);
        }

        removeInvisibleBattleUnits();
        sortBattleOrderByAt();
        currentActionIndex = 0;
        animating = false;
        animTicks = 0;
        animAction = "";
        currentActor = null;
        waitingForPlayerDecision = false;
        currentAttackingEnemyIndex = -1;
        enemyAttackTargetIsPlayer = true;
        enemyAttackTargetCompanionIndex = -1;
    }

    private int getPendingActionCostForPreview() {
        if ("enemyAttack".equals(animAction)) {
            return 20;
        }
        if (selectedSkill != null) {
            return 40;
        }
        if (!selectingPotionType.isEmpty()) {
            return 10;
        }
        if ("attack".equals(selectingTargetMode)) {
            return 20;
        }
        if ("skill".equals(selectingTargetMode)) {
            return 40;
        }
        if ("potion".equals(selectingTargetMode)) {
            return 10;
        }
        if (showingFleeMessage) {
            return 30;
        }
        return 20;
    }

    private int getProjectedBattleOrderIndex(Object unit, int actionCost) {
        if (unit == null || battleOrder.isEmpty()) {
            return -1;
        }

        java.util.List<BattleUnit> projectedOrder = new java.util.ArrayList<>();
        BattleUnit projectedUnit = null;
        for (BattleUnit battleUnit : battleOrder) {
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

    private java.util.List<Integer> buildVisibleBattleOrderIndices() {
        java.util.List<Integer> result = new java.util.ArrayList<>();
        for (int i = 0; i < battleOrder.size(); i++) {
            if (isBattleUnitVisible(battleOrder.get(i))) {
                result.add(i);
            }
        }
        return result;
    }

    private boolean isBattleOrderPreviewActive() {
        if (currentActor == null) {
            return false;
        }
        if (animating) {
            return true;
        }
        if (!selectingTargetMode.isEmpty()) {
            if ("attack".equals(selectingTargetMode)) {
                return hoveredEnemyIndex >= 0 && hoveredEnemyIndex < currentEnemies.size();
            }
            if ("skill".equals(selectingTargetMode)) {
                return hoveredEnemyIndex >= 0 && hoveredEnemyIndex < currentEnemies.size();
            }
            if ("potion".equals(selectingTargetMode)) {
                return hoveredEnemyIndex >= 0 && hoveredEnemyIndex < currentEnemies.size();
            }
        }
        return false;
    }

    private boolean isBattleUnitVisible(BattleUnit unit) {
        if (unit == null || unit.unit == null) {
            return false;
        }

        if (unit.unit instanceof Player) {
            return player != null && player.hp > 0;
        }

        if (unit.unit instanceof Companion) {
            return ((Companion) unit.unit).hp > 0;
        }

        if (unit.unit instanceof Enemy) {
            Enemy enemy = (Enemy) unit.unit;
            return enemy.hp > 0 && currentEnemies.contains(enemy);
        }

        return false;
    }

    // 繪製行動順序長條
    private void drawBattleOrderBar(Graphics2D g2d) {
        int barX = 20;
        int barY = 100;
        int unitWidth = 40;
        int unitHeight = 30;
        int gap = 5;
        int maxDisplay = 8; // 顯示最多 8 個單位

        // 標題
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Microsoft JhengHei", Font.BOLD, 12));
        g2d.drawString("行動順序:", barX, barY - 5);

        if (battleOrder.isEmpty()) {
            return;
        }

        // 僅顯示仍可參與戰鬥的單位（倒下隱藏，復活重現）
        java.util.List<Integer> visibleOrderIndices = buildVisibleBattleOrderIndices();

        if (visibleOrderIndices.isEmpty()) {
            return;
        }

        java.util.List<Integer> displayOrderIndices = visibleOrderIndices;

        int displayCount = Math.min(displayOrderIndices.size(), maxDisplay);

        // 繪製行動隊列
        for (int i = 0; i < displayCount; i++) {
            int orderIndex = displayOrderIndices.get(i);
            BattleUnit unit = battleOrder.get(orderIndex);
            int x = barX + i * (unitWidth + gap);
            int y = barY;

            // 決定填充顏色：根據單位類型選擇
            Color fillColor;
            if (unit.unit instanceof Player) {
                fillColor = new Color(0, 0, 255, 150);  // 藍色：玩家
            } else if (unit.unit instanceof Companion) {
                fillColor = new Color(0, 255, 0, 150);  // 綠色：隊友
            } else if (unit.unit instanceof Enemy) {
                fillColor = new Color(255, 0, 0, 150);  // 紅色：敵人
            } else {
                fillColor = new Color(100, 100, 100, 100);
            }

            // 最左側即為現在行動者，加亮填充顏色
            if (i == 0) {
                // 從半透明變成完全不透明，且亮度提升
                int r = Math.min(255, fillColor.getRed() + 80);
                int g_val = Math.min(255, fillColor.getGreen() + 80);
                int b = Math.min(255, fillColor.getBlue() + 80);
                fillColor = new Color(r, g_val, b, 255);
            }

            // 繪製填充
            g2d.setColor(fillColor);
            g2d.fillRect(x, y, unitWidth, unitHeight);

            // 繪製邊框
            if (i == 0) {
                // 當前行動者：金色粗邊框
                g2d.setColor(new Color(255, 200, 0));
                g2d.setStroke(new BasicStroke(3));
            } else {
                // 其他行動者：灰色細邊框
                g2d.setColor(new Color(100, 100, 100));
                g2d.setStroke(new BasicStroke(1));
            }
            g2d.drawRect(x, y, unitWidth, unitHeight);

            // 繪製標籤
            g2d.setStroke(new BasicStroke(1)); // 重置 stroke
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

        // 預覽時不重疊整列，只在預測位置畫目前角色的 ghost
        if (isBattleOrderPreviewActive()) {
            int previewIndex = getProjectedBattleOrderIndex(currentActor, getPendingActionCostForPreview());
            if (previewIndex >= 0 && previewIndex < maxDisplay) {
                BattleUnit currentActorUnit = findBattleUnit(currentActor);
                if (currentActorUnit != null && isBattleUnitVisible(currentActorUnit)) {
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

    // 繪製角色面板（用於戰鬥下方UI）
    private void drawCharacterPanel(Graphics2D g2d, int x, int y, int w, int h, String name, int hp, int maxHp,
            int mana, int maxMana, int cp, int maxCp) {
        // 面板背景
        g2d.setColor(new Color(50, 50, 80, 150));
        g2d.fillRect(x, y, w, h);
        // 面板邊框
        g2d.setColor(new Color(100, 200, 100));
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRect(x, y, w, h);

        // 角色名字
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Microsoft JhengHei", Font.BOLD, 12));
        g2d.drawString(name, x + 10, y + 18);

        // 血條背景
        int barX = x + 10;
        int barY = y + 25;
        int barW = w - 20;
        int barH = 12;

        // HP 血條
        g2d.setColor(new Color(80, 80, 80));
        g2d.fillRect(barX, barY, barW, barH);
        int hpWidth = (int) (barW * (hp / (double) maxHp));
        g2d.setColor(new Color(255, 165, 0));
        g2d.fillRect(barX, barY, hpWidth, barH);
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.setStroke(new BasicStroke(1));
        g2d.drawRect(barX, barY, barW, barH);
        
        // HP 標籤與數字
        g2d.setColor(new Color(200, 200, 200));
        g2d.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 9));
        g2d.drawString("HP", x + 10, barY - 2);
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 10));
        g2d.drawString(hp + "/" + maxHp, barX + 3, barY + 10);

        // MP 魔力條
        int barY2 = barY + 16;
        g2d.setColor(new Color(80, 80, 80));
        g2d.fillRect(barX, barY2, barW, barH);
        int manaWidth = (int) (barW * (mana / (double) maxMana));
        g2d.setColor(new Color(100, 150, 255));
        g2d.fillRect(barX, barY2, manaWidth, barH);
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.drawRect(barX, barY2, barW, barH);
        
        // MP 標籤
        g2d.setColor(new Color(200, 200, 200));
        g2d.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 9));
        g2d.drawString("MP", x + 10, barY2 - 2);
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 10));
        g2d.drawString(mana + "/" + maxMana, barX + 3, barY2 + 10);

        // CP 戰技值條
        int barY3 = barY2 + 16;
        g2d.setColor(new Color(80, 80, 80));
        g2d.fillRect(barX, barY3, barW, barH);
        int cpWidth = (int) (barW * (cp / (double) maxCp));
        g2d.setColor(new Color(100, 200, 100));
        g2d.fillRect(barX, barY3, cpWidth, barH);
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.drawRect(barX, barY3, barW, barH);
        
        // CP 標籤
        g2d.setColor(new Color(200, 200, 200));
        g2d.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 9));
        g2d.drawString("CP", x + 10, barY3 - 2);
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 10));
        g2d.drawString(cp + "/" + maxCp, barX + 3, barY3 + 10);
    }

    // 執行普通攻擊動畫
    private void executeAttackAnimation() {
        animating = true;
        if (currentActor instanceof Companion) {
            animAction = "companionAttack";
        } else {
            animAction = "playerAttack";
        }
        animTicks = 0;
        selectedSkill = null;
        waitingForPlayerDecision = false;
        selectingTargetMode = "";
        // targetingEnemyIndex 保持得到的目標敵人索引
        repaint();
    }

    // 執行戰技動畫
    private void executeSkillAnimation(Object target) {
        if (selectedSkill == null) {
            return;
        }

        if (target instanceof Enemy) {
            targetingEnemyIndex = currentEnemies.indexOf((Enemy) target);
        }

        animating = true;
        // 所有戰技統一走 playerAttack 分支，確保可正確結算 selectedSkill
        animAction = "playerAttack";
        animTicks = 0;
        waitingForPlayerDecision = false;
        selectingTargetMode = "";
        repaint();
    }

    private int findClosestEnemyIndexAt(int mouseX, int mouseY, int hitRadius) {
        int nearestIndex = -1;
        double nearestDist = Double.MAX_VALUE;

        for (int i = 0; i < currentEnemies.size(); i++) {
            double dx = mouseX - battleScreenEnemyX[i];
            double dy = mouseY - battleScreenEnemyY[i];
            double dist = Math.hypot(dx, dy);
            if (dist <= hitRadius && dist < nearestDist) {
                nearestDist = dist;
                nearestIndex = i;
            }
        }

        return nearestIndex;
    }

    // 執行使用藥水的動畫
    private void executePotionAnimation(Object target) {
        if (selectingPotionType.isEmpty()) {
            return;
        }
        
        Object currentCharacter = currentActor;
        int healAmount = 0;
        
        if ("small".equals(selectingPotionType)) {
            healAmount = 40;
        } else if ("large".equals(selectingPotionType)) {
            healAmount = 80;
        }
        
        // 給目標角色治療
        if (target instanceof Player) {
            ((Player) target).heal(healAmount);
        } else if (target instanceof Companion) {
            ((Companion) target).heal(healAmount);
        }
        
        // 消耗藥水
        if (currentCharacter instanceof Player) {
            Player p = (Player) currentCharacter;
            if ("small".equals(selectingPotionType)) {
                p.smallPotions = Math.max(0, p.smallPotions - 1);
            } else if ("large".equals(selectingPotionType)) {
                p.largePotions = Math.max(0, p.largePotions - 1);
            }
        }
        
        // 用藥完成後依 AT 重新排序
        completeActionAndRefreshBattleOrder(10);
        selectingTargetMode = "";
        selectingPotionType = "";
        repaint();
    }

    // 顯示戰技菜單並進入目標選擇模式
    private void showSkillMenuForTargeting() {
        Object currentCharacter = currentActor;
        if (currentCharacter == null) {
            currentCharacter = player;
        }

        java.util.List<Skill> skillList;
        
        if (currentCharacter instanceof Player) {
            skillList = ((Player) currentCharacter).skills;
        } else if (currentCharacter instanceof Companion) {
            skillList = ((Companion) currentCharacter).skills;
        } else {
            JOptionPane.showMessageDialog(this, "無法使用戰技！");
            return;
        }

        if (skillList.isEmpty()) {
            JOptionPane.showMessageDialog(this, "還沒有戰技可使用！");
            return;
        }

        // 創建技能列表
        String[] skillNames = new String[skillList.size()];
        for (int i = 0; i < skillList.size(); i++) {
            Skill skill = skillList.get(i);
            skillNames[i] = skill.name + " - " + skill.description;
        }

        // 使用 JList 創建可滾動的選擇窗口
        JList<String> skillListUI = new JList<>(skillNames);
        skillListUI.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        skillListUI.setSelectedIndex(0);
        skillListUI.setFont(new java.awt.Font("Microsoft JhengHei", java.awt.Font.PLAIN, 14));
        skillListUI.setFixedCellHeight(30);
        skillListUI.setVisibleRowCount(Math.min(8, skillNames.length));

        JScrollPane scrollPane = new JScrollPane(skillListUI);
        scrollPane.setPreferredSize(new java.awt.Dimension(400, 250));
        int result = JOptionPane.showConfirmDialog(this, scrollPane, "選擇戰技", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION && skillListUI.getSelectedIndex() >= 0) {
            int choice = skillListUI.getSelectedIndex();
            Skill skill = skillList.get(choice);
            
            // 檢查 CP 是否足夠
            if (currentCharacter instanceof Player) {
                Player p = (Player) currentCharacter;
                if (p.cp < skill.cpCost) {
                    JOptionPane.showMessageDialog(this, "CP 不足！需要 " + skill.cpCost + " CP，現有 " + p.cp + " CP。");
                    return;
                }
            } else if (currentCharacter instanceof Companion) {
                Companion c = (Companion) currentCharacter;
                if (c.cp < skill.cpCost) {
                    JOptionPane.showMessageDialog(this, "CP 不足！需要 " + skill.cpCost + " CP，現有 " + c.cp + " CP。");
                    return;
                }
            }
            
            selectedSkill = skill;
            selectingTargetMode = "skill";  // 進入戰技目標選擇模式
            repaint();
        }
    }

    // 顯示藥物菜單並進入目標選擇模式
    private void showPotionMenuForTargeting() {
        Object currentCharacter = currentActor;
        if (currentCharacter == null) {
            currentCharacter = player;
        }

        int smallPotions = 0;
        int largePotions = 0;
        
        if (currentCharacter instanceof Player) {
            Player p = (Player) currentCharacter;
            smallPotions = p.smallPotions;
            largePotions = p.largePotions;
        } else if (currentCharacter instanceof Companion) {
            // 隊友使用玩家的藥水
            smallPotions = player.smallPotions;
            largePotions = player.largePotions;
        }

        if (smallPotions + largePotions <= 0) {
            JOptionPane.showMessageDialog(this, "沒有可用的藥水！");
            return;
        }

        Object[] options = { "小藥 (+40)", "大藥 (+80)", "取消" };
        int choice = JOptionPane.showOptionDialog(this,
                "選擇藥水種類", "藥水",
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
                null, options, options[0]);

        if (choice == 0 && smallPotions > 0) {
            selectingPotionType = "small";
            selectingTargetMode = "potion";  // 進入藥水目標選擇模式
            repaint();
        } else if (choice == 1 && largePotions > 0) {
            selectingPotionType = "large";
            selectingTargetMode = "potion";  // 進入藥水目標選擇模式
            repaint();
        }
    }
}

