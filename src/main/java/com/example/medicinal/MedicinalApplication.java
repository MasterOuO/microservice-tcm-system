package com.example.medicinal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import java.io.File;
import java.io.IOException;

@SpringBootApplication
public class MedicinalApplication {

    private static Process nodeProcess;

    public static void main(String[] args) {
        SpringApplication.run(MedicinalApplication.class, args);
    }

    /**
     * 當 MedicinalApplication 啟動完成後，自動拉起同專案目錄下的 node server.js
     */
    /*@EventListener(ApplicationReadyEvent.class)
    public void startNodeService() {
        try {
            // 自動鎖定專案下的 midicinal_system 資料夾 (C:\tcm_2026\midicinal\midicinal_system)
            File workingDir = new File("C:/tcm_2026/midicinal/midicinal_system");

            // 若目錄不存在，嘗試以相對路徑抓取當前專案下的 midicinal_system
            if (!workingDir.exists()) {
                workingDir = new File(System.getProperty("user.dir"), "midicinal_system");
            }

            boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");
            String[] command = isWindows 
                ? new String[]{"cmd.exe", "/c", "node", "server.js"} 
                : new String[]{"node", "server.js"};

            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.directory(workingDir);
            processBuilder.inheritIO(); // 將 Node.js 輸出日誌導流至 Eclipse Console

            System.out.println("🚀 [MedicinalService] 正在啟動 Node.js 中藥微服務...");
            nodeProcess = processBuilder.start();

            // 註冊關閉掛鉤 (在 Eclipse 按 Terminate 終止時，自動結束 Node.js 行程)
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                if (nodeProcess != null && nodeProcess.isAlive()) {
                    System.out.println("🛑 [MedicinalService] 正在停止 Node.js 微服務...");
                    nodeProcess.destroy();
                }
            }));

        } catch (IOException e) {
            System.err.println("❌ 啟動 Node.js 中藥服務失敗: " + e.getMessage());
        }
    }*/
}