package com.fit.burpLoad;

import jdk.internal.org.objectweb.asm.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * @className: Translator
 * @description:
 * @author: Aim
 * @date: 2023/7/7
 **/
public class Translator {

    private static Map<String, Translator> map = new HashMap<String, Translator>();
    private Map<String, String> literal = new HashMap<String, String>();
    private Map<Pattern, String> regexp = new HashMap<Pattern, String>();
    boolean debug;

    public Translator() {
        System.out.println("  汉化处理已加载！");
    }

    public static String translate(String langFile, String str) throws Exception {
        Translator translator = map.get(langFile);
        if (translator == null) {
            translator = new Translator(langFile);
            map.put(langFile, translator);
        }
        if ((str == null) || (str.length() == 0)) {
            return str;
        }

        StringBuilder ret = new StringBuilder();
        for (String s : str.split("\n")) {
            if (ret.length() > 0) {
                ret.append("\n");
            }
            ret.append(translator.translate(s));
        }

        return ret.toString();
    }

    public Translator(String langFile) {
        try {
            if (langFile == null) {
                langFile = "cn.txt";
            }
            if (langFile.equals("debug")) {
                debug = true;
            }
            InputStream ism = null;
            File file = new File(langFile);
            if (file.isFile()) {
                ism = new FileInputStream(file);
            } else {
                ism = Translator.class.getResourceAsStream("cn.txt");
            }
            Pattern pattern = Pattern.compile(".*\\$[0-9].*");
            try (Scanner sc = new Scanner(ism, "UTF-8")) {
                while (sc.hasNextLine()) {
                    String[] inputs = sc.nextLine().split("\t", 2);
                    literal.put(inputs[0], inputs[1]);
                    // 如果您使用正则表达式，请编译
                    if (pattern.matcher(inputs[1]).matches()) {
                        String re = "(?m)^" + inputs[0] + "$";
                        try {
                            regexp.put(Pattern.compile(re), inputs[1].replace("\"", "\\\""));
                        } catch (PatternSyntaxException ignore) {
                        }
                    }
                }
            } finally {
                if (ism != null) {
                    ism.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String translate(String src) {
        if ((src == null) || (src.length() == 0)) {
            return src;
        }

        String dst = literal.get(src);
        if (dst == null) {
            dst = src;
            Iterator<Entry<Pattern, String>> iterator = regexp.entrySet().iterator();
            while (iterator.hasNext()) {
                Entry<Pattern, String> entry = iterator.next();
                Pattern pattern = entry.getKey();
                Matcher matcher = pattern.matcher(dst);
                dst = matcher.replaceAll((String) entry.getValue());
            }
        }
        // 使非翻译句子标准错误。
        if (debug && (src.equals(dst)
                && dst.length() == dst.getBytes().length // 只有那些没有翻译的
                && !src.matches("https?://.+")           // URL忽视
                && !src.matches("\\$?[ 0-9,./:]+")       // 忽略数字
                && !src.matches("^[-.\\w]+:?$")          // 忽略只有一个单词的情况
                && !src.matches("burp\\..*")             // 忽略以 burp 开头的内容。（类名?）
                && !src.matches("lbl.*")                 // 忽略以 lbl 开头的内容（标签 名称?）
                && src.length() > 1                      // 忽略一个字符
                && !src.matches("[- A-Z]+s?")            // 仅忽略大写字母
                && !src.matches("\\s+")                  // 只忽略空格
        )) {
            System.err.println("[" + src + "]");
        }
        return dst;
    }
}
