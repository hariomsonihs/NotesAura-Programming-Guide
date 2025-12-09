package com.hariomsonihs.notesaura.utils;

import com.hariomsonihs.notesaura.R;
import java.util.HashMap;
import java.util.Map;

public class IconMapper {
    private static final Map<String, Integer> iconMap = new HashMap<>();
    
    static {
        // Programming Languages
        iconMap.put("java", R.drawable.ic_java);
        iconMap.put("python", R.drawable.ic_python);
        iconMap.put("javascript", R.drawable.ic_javascript);
        iconMap.put("c++", R.drawable.ic_cpp);
        iconMap.put("c", R.drawable.ic_c);
        iconMap.put("kotlin", R.drawable.ic_kotlin);
        
        // Web Development
        iconMap.put("html", R.drawable.ic_html);
        iconMap.put("css", R.drawable.ic_css);
        iconMap.put("react", R.drawable.ic_react);
       /* iconMap.put("angular", R.drawable.ic_angular);
        iconMap.put("nodejs", R.drawable.ic_nodejs);*/
        iconMap.put("web", R.drawable.ic_web);
        
        // Mobile Development
        iconMap.put("android", R.drawable.ic_android);
        iconMap.put("flutter", R.drawable.ic_flutter);
        iconMap.put("ios", R.drawable.ic_ios);
       /* iconMap.put("react native", R.drawable.ic_react_native);*/
        
        // Data Science & AI
        iconMap.put("machine learning", R.drawable.ic_ml);
        iconMap.put("data science", R.drawable.ic_data_science);
      /*  iconMap.put("ai", R.drawable.ic_ai);
        iconMap.put("deep learning", R.drawable.ic_deep_learning);*/
        
        // Database
        iconMap.put("sql", R.drawable.ic_database);
      /*  iconMap.put("mysql", R.drawable.ic_mysql);
        iconMap.put("mongodb", R.drawable.ic_mongodb);*/
        iconMap.put("database", R.drawable.ic_database);
        
        // Default fallback
        iconMap.put("default", R.drawable.ic_code);
    }
    
    public static int getIconForCategory(String categoryName) {
        if (categoryName == null) return iconMap.get("default");
        
        String lowerName = categoryName.toLowerCase().trim();
        
        // Direct match first
        if (iconMap.containsKey(lowerName)) {
            return iconMap.get(lowerName);
        }
        
        // Specific word matches (avoid partial conflicts)
        if (lowerName.equals("java") || lowerName.contains("java ") || lowerName.startsWith("java ") || lowerName.endsWith(" java")) {
            return iconMap.get("java");
        }
        if (lowerName.equals("python") || lowerName.contains("python ") || lowerName.startsWith("python ") || lowerName.endsWith(" python")) {
            return iconMap.get("python");
        }
        if (lowerName.equals("javascript") || lowerName.contains("javascript") || lowerName.contains("js")) {
            return iconMap.get("javascript");
        }
        if (lowerName.equals("c++") || lowerName.contains("c++") || lowerName.contains("cpp")) {
            return iconMap.get("c++");
        }
        if (lowerName.equals("c") && !lowerName.contains("++") && !lowerName.contains("css")) {
            return iconMap.get("c");
        }
        if (lowerName.contains("android")) {
            return iconMap.get("android");
        }
        if (lowerName.contains("web") || lowerName.contains("html") || lowerName.contains("css")) {
            return iconMap.get("web");
        }
        if (lowerName.contains("database") || lowerName.contains("sql")) {
            return iconMap.get("database");
        }
        
        return iconMap.get("default");
    }
    
    public static String getEmojiForCategory(String categoryName) {
        if (categoryName == null) return "💻";
        
        String lowerName = categoryName.toLowerCase().trim();
        
        if (lowerName.contains("java")) return "☕";
        if (lowerName.contains("python")) return "🐍";
        if (lowerName.contains("javascript")) return "🟨";
        if (lowerName.contains("web")) return "🌐";
        if (lowerName.contains("android")) return "🤖";
        if (lowerName.contains("ios")) return "🍎";
        if (lowerName.contains("flutter")) return "🦋";
        if (lowerName.contains("react")) return "⚛️";
        if (lowerName.contains("database") || lowerName.contains("sql")) return "🗄️";
        if (lowerName.contains("machine learning") || lowerName.contains("ai")) return "🤖";
        if (lowerName.contains("data")) return "📊";
        
        return "💻";
    }
}