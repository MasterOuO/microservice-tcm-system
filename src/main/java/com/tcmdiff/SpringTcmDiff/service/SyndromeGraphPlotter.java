package com.tcmdiff.SpringTcmDiff.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.text.DecimalFormat;
import java.util.*;
import org.springframework.stereotype.Service;

import com.tcmdiff.SpringTcmDiff.models.Relation;
import com.tcmdiff.SpringTcmDiff.models.Cause;

/**
 * 	處理diffservice後要透過症候&分數 用graphviz建立演變圖 
 *  換佈署Graphviz 行182要換執行路徑exe
 */
@Service
public class SyndromeGraphPlotter {	
	//打包路徑測試
    private static final Path OUTPUT_ROOT = Paths.get("./diffgraph");
    private static final Path DOT_DIR = OUTPUT_ROOT.resolve(Paths.get("dot"));
    public static final Path PNG_DIR = OUTPUT_ROOT.resolve(Paths.get("png"));
    public static final Path TMP_PNG_DIR = OUTPUT_ROOT.resolve(Paths.get("tmpPng"));
    
    /* 字形設定 */
    private static final String FONT_FAMILY ="DFKai-SB, BiauKai, Kaiti TC, KaiTi, TW-Kai, AR PL UKai TW"; // 標楷體
    
    
    // 避免 < > & 等字元
    private String htmlEscape(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }


    // 根據 score 畫顏色 
    private String getNodeColor(double score) {
        if (score >= 0.8 && score <= 1.0) return "red";
        if (score >= 0.6 && score < 0.8)  return "orange";
        if (score >= 0.5 && score < 0.6)  return "yellow";
        if (score > 0.4 && score < 0.5)  return "white"; 
        else return null; 
    }

    // 是否畫點
    private  boolean shouldDrawNode(double score) {
        return score > 0.4;
    }

    // 是否畫邊
    private  boolean shouldDrawEdge(double fromScore, double toScore) {
        return fromScore > 0.4;
    }

    // 存檔id對應表 
    private  Map<String, String> buildSyndromeIdMap(Set<String> names) {
        Map<String, String> idMap = new LinkedHashMap<>();
        int idx = 1;
        for (String name : names) {
            idMap.put(name, "n" + idx++);
        }
        return idMap;
    }

    // 處理 Graphviz label 引號問題 
    private String escapeLabel(String label) {
        return label.replace("\"", "“");
    }


    
    // 生成 Graphviz dot 語法 
    public String toDot(Map<String, Double> syndromeScores,
                               List<Relation> syndromeRelations,
                               List<Cause> causes
                               ) {

        // 篩選節點
        Map<String, Double> visibleSyndromes = new LinkedHashMap<>();
        for (Map.Entry<String, Double> entry : syndromeScores.entrySet()) {
            if (shouldDrawNode(entry.getValue())) {
                visibleSyndromes.put(entry.getKey(), entry.getValue());
            }
        }
        
        //兼併證候 沒有from就不顯示
        Map<String, List<String>> mergeDependencies = new HashMap<>();
        for (Relation r : syndromeRelations) {
            if ("兼併證候".equals(r.source)) {
                // r.to 是兼併結果(肝脾不和), r.from 是前提(肝氣鬱結)
                mergeDependencies.computeIfAbsent(r.to, k -> new ArrayList<>()).add(r.from);
            }
        }
        Iterator<Map.Entry<String, Double>> iterator = visibleSyndromes.entrySet().iterator();
        while (iterator.hasNext()) {
            String currentSyndrome = iterator.next().getKey();
            
            // 出現在依賴表裡是兼併證候
            if (mergeDependencies.containsKey(currentSyndrome)) {
                List<String> requiredFroms = mergeDependencies.get(currentSyndrome);
                boolean allPrerequisitesMet = true;
                
                // 檢查所有from存在visibleSyndromes
                for (String reqFrom : requiredFroms) {
                    if (!visibleSyndromes.containsKey(reqFrom)) {
                        allPrerequisitesMet = false;
                        break;
                    }
                }
                
                // 只要有一個前提沒達標，刪除
                if (!allPrerequisitesMet) {
                    iterator.remove(); 
                }
            }
        }
        
        
        // id對應
        Map<String, String> syndromeIdMap = buildSyndromeIdMap(visibleSyndromes.keySet());

        StringBuilder sb = new StringBuilder();
        DecimalFormat df = new DecimalFormat("0.000");

        sb.append("digraph G {\n");
        sb.append("  graph [rankdir=TB, charset=\"UTF-8\", dpi=150, labelloc=t, fontsize=20, ")
          .append("fontname=\"").append(FONT_FAMILY).append("\", nodesep=0.4, ranksep=0.6];\n");
        sb.append("  node  [shape=box, style=filled, fontname=\"").append(FONT_FAMILY)
          .append("\", fontsize=12, fontcolor=\"black\"];\n");
        sb.append("  edge  [fontname=\"").append(FONT_FAMILY).append("\", arrowsize=0.8];\n");

        
        // 先判斷cause點
        for (Cause cause : causes) {
            // 指向的症候沒有 或分數不夠 不畫
            if (!visibleSyndromes.containsKey(cause.to)) continue;

            String nodeId = cause.id;
/*
            String dotLabel = "";
              if (cause.cause != null && !cause.cause.isEmpty()) {
             
                // 前端成因陣列用 \l 串接起來對齊
                dotLabel = escapeLabel(String.join("\\l", cause.cause)) + "\\l"; 
            }
*/
            sb.append("  ").append(nodeId)
              
              .append("[shape=point, width=0.15, ") // align=left 文字靠左
              .append("fillcolor=\"#E6F5E6\", color=\"#006400\", fontcolor=\"#006400\"") // 字顏色
              .append("];\n");
        }
        sb.append("\n");
        
        // 節點
        for (Map.Entry<String, Double> entry : visibleSyndromes.entrySet()) {
            String name = entry.getKey();
            double score = entry.getValue();
            String fillColor = getNodeColor(score);
            if (fillColor == null) continue; // < 0.4 不畫
            String nodeId = syndromeIdMap.get(name);
            String label ="<" 
                + "<TABLE BORDER=\"0\" CELLBORDER=\"0\" CELLSPACING=\"0\">" 
                + "  <TR><TD><FONT FACE=\"" + FONT_FAMILY + "\" POINT-SIZE=\"12\"><B>" + htmlEscape(name) + "</B></FONT></TD></TR>" 
                + "  <TR><TD><FONT FACE=\"" + FONT_FAMILY + "\" POINT-SIZE=\"11\">(" + df.format(score) + ")</FONT></TD></TR>" 
                + "</TABLE>" 
                + ">";

            sb.append("  ").append(nodeId)
              .append(" [label=").append(label)
              .append(", fillcolor=\"").append(fillColor).append("\"];\n");
        }
        sb.append("\n");

        
        // cause的邊
        for (Cause cause : causes) {
            if (!visibleSyndromes.containsKey(cause.to)) continue;

            String fromId = cause.id;                 
            String toId   = syndromeIdMap.get(cause.to);  
//            String arrowLabel = cause.label;        

            if (fromId != null && toId != null) {
                sb.append("  ").append(fromId).append(" -> ").append(toId);
/*
                if (arrowLabel != null && !arrowLabel.isEmpty()) {
                    String dotLabel = escapeLabel(arrowLabel);
                    sb.append(" [label=\"").append(dotLabel).append("\"");
                    sb.append(", fontsize=10, fontcolor=\"#0000FF\"") 
                      .append(", fontname=\"").append(FONT_FAMILY).append("\"").append("]");
                }
*/              
                if (cause.cause != null && !cause.cause.isEmpty()) {
                	String dotLabel = escapeLabel(String.join("\\l", cause.cause)) + "\\l";
                    sb.append(" [label=\"").append(dotLabel).append("\"");
                    sb.append(", fontsize=10, fontcolor=\"#0000FF\"") 
                      .append(", fontname=\"").append(FONT_FAMILY).append("\"").append("]");
                }
                sb.append(";\n");
            }
        }
        sb.append("\n");
        
    	// 篩選重複relation，避免畫重複邊
    	Set<String> drawnEdges = new HashSet<>();
        // relation的邊
        for (Relation relation : syndromeRelations) {
            if (!syndromeScores.containsKey(relation.from) || !syndromeScores.containsKey(relation.to)) continue; // 確認有辨證分數
            Double fromScore = syndromeScores.get(relation.from);
            Double toScore   = syndromeScores.get(relation.to);
            if (fromScore == null || toScore == null) continue;
            if (!visibleSyndromes.containsKey(relation.from) || !visibleSyndromes.containsKey(relation.to)) continue; // 確認歸屬度> 0.4
            if (shouldDrawEdge(fromScore, toScore)) {
            	// 建立from->to的key，避免重複畫邊
            	String edgeKey = relation.from + "->" + relation.to;
            	if (drawnEdges.contains(edgeKey)) continue; 
            	drawnEdges.add(edgeKey);
            	
                String fromId = syndromeIdMap.get(relation.from);
                String toId   = syndromeIdMap.get(relation.to);
                String labelId = relation.label;
                sb.append("  ").append(fromId).append(" -> ").append(toId);
                //有邊的文字要產生
                if (labelId != null && !labelId.isEmpty()) {
                	String dotLabel = escapeLabel(labelId);     
                    sb.append(" [label=\"").append(dotLabel).append("\"");
                    sb.append(", fontsize=10, fontcolor=\"#0000FF\"") 
                    .append(", fontname=\"").append(FONT_FAMILY).append("\"").append("]");
                }
                sb.append(";\n");
            }
        }
        sb.append("\n");
        sb.append("}\n");
        return sb.toString();
    }

    /* 輸出 .dot 檔案 */
    public Path writeDot(String dot, String fileName) throws IOException {
        //產dot放的資料夾 +檔名
    	if (!Files.exists(DOT_DIR)) Files.createDirectories(DOT_DIR);
        Path p = DOT_DIR.resolve(fileName);
        
        try (BufferedWriter w = Files.newBufferedWriter(p, StandardCharsets.UTF_8)) {
            w.write(dot);
        }
        return p;
    }

    // .dot 轉 .png 
    public Path dotToPng(Path dotFile) throws IOException, InterruptedException {
    	//產圖放的資料夾
    	if (!Files.exists(TMP_PNG_DIR)) Files.createDirectories(TMP_PNG_DIR);
        
        //檔名決定
        String baseName = stripExtension(dotFile.getFileName().toString());
        Path pngOut = TMP_PNG_DIR.resolve(baseName + ".png");
        
        // 使用 Graphviz 的 dot 指令
        String dotExe = "dot";
        ProcessBuilder pb = new ProcessBuilder(
                dotExe, "-Tpng",
                dotFile.toAbsolutePath().toString(),
                "-o", pngOut.toAbsolutePath().toString()
        );
        
        //錯誤輸出stderr也合併到正常輸出stdout
        pb.redirectErrorStream(true);
 
        // 讀取輸出
        Process proc = pb.start();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(proc.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.isEmpty()) System.out.println("[dot] " + line);
            }
        }

        int exit = proc.waitFor();
        if (exit != 0) {
            throw new IOException("Graphviz dot 轉檔失敗（exit=" + exit + "），請確認已安裝 Graphviz 且 dot 在 PATH。");
        }

        return pngOut;
    }

    private String stripExtension(String filename) {
        int i = filename.lastIndexOf('.');
        return (i == -1) ? filename : filename.substring(0, i);
    }
}
