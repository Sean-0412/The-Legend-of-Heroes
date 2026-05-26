package src;

import javax.swing.*;

/**
 * 非純文字 RPG 範例
 *
 * 遊戲包含：
 * - 使用箭頭鍵移動玩家
 * - 簡單的地圖(牆壁/地面)
 * - 靠近敵人即可進入戰鬥
 * - 戰鬥中可以選擇攻擊、使用技能或道具
    * - 玩家和隊友都有等級、經驗、生命、魔力、戰技值等屬性
    * - 簡單的升級系統
 * - 可以保存和加載遊戲進度
 * - 這只是個起點，你可以在此基礎上擴充技能、道具、等級、地圖貼圖等等。
 */
public class RPGGame extends JFrame {
    private GamePanel gamePanel;

    public RPGGame() {
        setTitle("簡易RPG");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 650);
        setLocationRelativeTo(null);
        setResizable(false);

        gamePanel = new GamePanel();
        add(gamePanel);

        // 視窗關閉時停止音樂
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                gamePanel.stopBackgroundMusic();
            }
        });

        setVisible(true);
        gamePanel.requestFocusInWindow();  // 確保 GamePanel 獲得焦點
    }

    public static void main(String[] args) {
        // 啟動在 Swing Event Dispatch Thread
        SwingUtilities.invokeLater(RPGGame::new);
    }
}
