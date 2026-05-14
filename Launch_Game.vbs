Set objShell = CreateObject("WScript.Shell")
Dim strPath, strCmd

' 獲取腳本目錄
strPath = CreateObject("Scripting.FileSystemObject").GetParentFolderName(WScript.ScriptFullName)

' 編譯代碼到 bin 資料夾並啟動遊戲
strCmd = "cd /d """ & strPath & """ && if exist bin rmdir /s /q bin && mkdir bin && javac -encoding UTF-8 -d bin src\*.java && java -cp bin RPGGame"

' 執行命令，隱藏窗口（第二個參數 0 = 隱藏）
objShell.Run "cmd.exe /c " & strCmd, 0, True
