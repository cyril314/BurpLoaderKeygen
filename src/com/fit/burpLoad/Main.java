package com.fit.burpLoad;


import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;


public class Main {

    private static JFrame frame;
    private static JButton btn_run;
    private static JTextField text_cmd;
    private static JTextField text_license_title;
    private static JTextArea area_license;
    private static JTextArea request;
    private static JTextArea response;
    private static JLabel label0_1;
    private static JPanel panel1;
    private static JPanel panel2;
    private static JPanel panel3;
    private static JCheckBox check_autorun;
    private static JCheckBox check_ignore;
    private static String LatestVersion;
    private static final String BURP_URL = "https://portswigger.net/burp/releases";
    private static final String DownloadURL = BURP_URL + "/download?product=pro&type=Jar&version=";

    private static Properties configureProperties;

    /**
     * 获取路径中的所有目录
     *
     * @param files 返回目录集合
     * @param work  指定起始文件路径
     */
    private static void getWorkDirs(List<File> files, File work) {
        if (work.isDirectory()) {
            files.add(work);
            for (File file : work.listFiles()) {
                if (file.isDirectory()) {
                    getWorkDirs(files, file);
                }
            }
        }
    }

    /**
     * 根据路径查找指定文件名的路径或文件名
     *
     * @param LastFile  默认没发现的文件名
     * @param AgentPath 用于判断文件是否在指定类路径
     * @param findJar   查找文件正则格式
     * @return
     * @throws IOException
     */
    private static String readBurpFile(String LastFile, Path AgentPath, String findJar) throws IOException {
        File work = new File("").getCanonicalFile();
        List<File> files = new ArrayList<>();
        getWorkDirs(files, work);
        for (File file : files) {
            try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(Paths.get(file.getPath(), new String[0]), findJar)) {
                long newest_time = 0L;
                for (Path path : dirStream) {
                    if (!Files.isDirectory(path, new LinkOption[0])) {
                        System.out.println(path.toAbsolutePath());
                        if (newest_time < path.toFile().lastModified()) {
                            newest_time = path.toFile().lastModified();
                            if (AgentPath.getParent().toString().equals(path.toAbsolutePath().getParent().toString())) {
                                LastFile = path.getFileName().toString();
                            } else {
                                LastFile = path.toAbsolutePath().toString();
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return LastFile;
    }

    private static void loadProperties() {
        Properties prop = new Properties();
        try {
            Reader configureFileReader = new InputStreamReader(new FileInputStream("config.cfg"), StandardCharsets.UTF_8);
            prop.load(configureFileReader);
        } catch (IOException e) {
            prop.setProperty("auto_run", "0");
            prop.setProperty("ignore", "0");
        }
        configureProperties = prop;
    }


    private static void saveProperties() {
        try (OutputStream output = new FileOutputStream("config.cfg")) {
            configureProperties.store(output, null);
        } catch (IOException io) {
            io.printStackTrace();
        }
    }

    /**
     * 获取最新版本消息
     */
    private static String getLatestVersion() {
        try {
            String asyncBody = HttpUtils.getInstance().getJson(BURP_URL + "/data?pageSize=1");
            int targetIndex = asyncBody.indexOf("\"ProductId\":\"pro\",\"ProductPlatform\":\"Jar\",\"ProductPlatformLabel\":\"JAR\"");
            if (targetIndex != -1) {
                String result2 = asyncBody.substring(targetIndex + 166);
                return result2.substring(0, result2.indexOf("\""));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 获取执行命令
     */
    private static List<String> getCommand() throws IOException {
        Path AgentPath = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().getPath()).toPath();
        // 查找Burp的jar包名
        String LastFile = readBurpFile("burpsuite_jar_not_found.jar", AgentPath, "burpsuite_*.jar");
        // 执行命令集合
        List<String> runCommand = new ArrayList<String>();
        runCommand.add("java");
        String genFileName = readBurpFile("BurpLoaderKeygen_not_found.jar", AgentPath, "BurpLoaderKeygen*.jar");
        String agentCmd = "-Xbootclasspath/p:";
        if (isNewVer(LastFile)) {
            agentCmd = "-javaagent:";
        }
        if (genFileName.contains(".jar")) {
            runCommand.add(agentCmd + genFileName);
        } else {
            runCommand.add(agentCmd + AgentPath.toAbsolutePath().toFile().getPath());
        }

        if (Integer.parseInt(System.getProperty("java.version").split("\\.")[1]) >= 17) {
            runCommand.add("--add-opens=java.desktop/javax.swing=ALL-UNNAMED");
            runCommand.add("--add-opens=java.base/java.lang=ALL-UNNAMED");
        }

        runCommand.add("-jar");
        runCommand.add(LastFile);

        return runCommand;
    }

    private static boolean isNewVer(String fileName) {
        if (fileName.contains("202")) {
            return true;
        }
        return false;
    }

    /**
     * 程序入口
     */
    public static void main(String[] args) throws IOException {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            frame = new JFrame("Burp Suite Pro Loader & Keygen");
        } catch (Exception e) {
            e.printStackTrace();
        }

        // init
        loadProperties();
        List<String> commandList = getCommand();
        String commandString = String.join(" ", commandList);
        boolean isOld = isNewVer(commandString);
        System.out.println("The software obtained is an new version:" + isOld);

        if (configureProperties.getProperty("auto_run").equals("1")) {
            try {
                ProcessBuilder builder = new ProcessBuilder();
                builder.command(commandList).start();
                if (configureProperties.getProperty("ignore").equals("1") || commandString.contains(getLatestVersion())) {
                    System.exit(0);
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
        if (configureProperties.getProperty("ignore").equals("0")) {
            LatestVersion = getLatestVersion();
        }

        panel1 = new JPanel(null);
        panel2 = new JPanel(null);
        panel3 = new JPanel(null);
        btn_run = new JButton("Run");
        btn_run.setSize(60, 22);
        label0_1 = new JLabel("Checking the latest version of BurpSuite...");
        JLabel label1 = new JLabel("Run Command:", SwingConstants.RIGHT);
        JLabel label2 = new JLabel("License Text:", SwingConstants.RIGHT);
        text_cmd = new JTextField(commandString);
        text_license_title = new JTextField("licensed to surferxyz");
        area_license = new JTextArea(Keygen.generateLicense(text_license_title.getText(), isOld));
        request = new JTextArea();
        response = new JTextArea();
        check_autorun = new JCheckBox("Auto Run");
        check_ignore = new JCheckBox("Ignore Update");
        check_autorun.setBounds(200, 25, 120, 20);
        check_autorun.setSelected(configureProperties.getProperty("auto_run").equals("1"));
        check_autorun.addChangeListener(changeEvent -> {
            if (check_autorun.isSelected()) {
                configureProperties.setProperty("auto_run", "1");
            } else {
                configureProperties.setProperty("auto_run", "0");
            }
            saveProperties();
        });
        check_ignore.setBounds(320, 25, 160, 20);
        check_ignore.setSelected(configureProperties.getProperty("ignore").equals("1"));
        check_ignore.addChangeListener(changeEvent2 -> {
            if (check_ignore.isSelected()) {
                configureProperties.setProperty("ignore", "1");
            } else {
                configureProperties.setProperty("ignore", "0");
            }
            saveProperties();
        });
        label0_1.setLocation(150, 5);
        label1.setBounds(5, 70, 140, 22);
        text_cmd.setLocation(150, 70);
        label2.setBounds(5, 97, 140, 22);
        panel1.setLocation(5, 124);
        panel1.setBorder(BorderFactory.createTitledBorder("License"));
        panel2.setBorder(BorderFactory.createTitledBorder("Activation Request"));
        panel3.setBorder(BorderFactory.createTitledBorder("Activation Response"));
        frame.setLayout(null);
        frame.setMinimumSize(new Dimension(900, 600));
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setBackground(Color.LIGHT_GRAY);
        frame.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e2) {
                int H = Main.frame.getHeight() - 170;
                int W = Main.frame.getWidth();
                Main.text_cmd.setSize(W - 235, 22);
                Main.btn_run.setLocation(W - 80, 70);
                Main.label0_1.setSize(W - 170, 20);
                Main.text_license_title.setSize(W - 170, 22);
                Main.area_license.setSize(((W - 15) / 2) - 25, (H / 2) - 25);
                Main.request.setSize(((W - 15) / 2) - 25, (H / 2) - 25);
                Main.response.setSize(W - 43, (H / 2) - 25);
                Main.panel1.setSize(((W - 15) / 2) - 5, H / 2);
                Main.panel2.setBounds(3 + ((W - 15) / 2), 124, ((W - 15) / 2) - 5, H / 2);
                Main.panel3.setBounds(5, 129 + (H / 2), W - 23, H / 2);
            }
        });

        frame.add(check_autorun);
        frame.add(check_ignore);
        // 添加运行命令按钮
        btn_run.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e2) {
                super.mouseClicked(e2);
                try {
                    ProcessBuilder builder = new ProcessBuilder();
                    builder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
                    builder.redirectErrorStream(true);//捕获并显示流程
                    Process process = builder.command(commandList).start();
                    System.out.println("Execute the start BP command ...");
                    try (Scanner sc = new Scanner(process.getInputStream(), "UTF-8")) {
                        while (sc.hasNextLine()) {
                            System.out.println(sc.nextLine());
                        }
                    }
                } catch (IOException e12) {
                    e12.printStackTrace();
                }
            }
        });
        frame.add(btn_run);
        frame.add(label0_1);
        frame.add(label1);
        frame.add(label2);
        frame.add(panel1);
        frame.add(panel2);
        frame.add(panel3);
        frame.add(text_cmd);
        //添加激活消息
        text_license_title.setLocation(150, 97);
        text_license_title.setHorizontalAlignment(0);
        text_license_title.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e2) {
                Main.area_license.setText(Keygen.generateLicense(Main.text_license_title.getText(), isOld));
            }

            public void removeUpdate(DocumentEvent e2) {
                Main.area_license.setText(Keygen.generateLicense(Main.text_license_title.getText(), isOld));
            }

            public void changedUpdate(DocumentEvent e2) {
                Main.area_license.setText(Keygen.generateLicense(Main.text_license_title.getText(), isOld));
            }
        });
        frame.add(text_license_title);
        // 添加激活码展示
        area_license.setLineWrap(true);
        area_license.setEditable(false);
        area_license.setLocation(10, 15);
        area_license.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
        panel1.add(area_license);
        // 添加激活请求信息展示
        request.setLineWrap(true);
        request.setLocation(10, 15);
        request.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
        request.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e2) {
                Main.response.setText(Keygen.generateActivation(Main.request.getText(), isOld));
            }

            public void removeUpdate(DocumentEvent e2) {
                Main.response.setText(Keygen.generateActivation(Main.request.getText(), isOld));
            }

            public void changedUpdate(DocumentEvent e2) {
                Main.response.setText(Keygen.generateActivation(Main.request.getText(), isOld));
            }
        });
        panel2.add(request);
        // 添加激活返回信息展示
        response.setLineWrap(true);
        response.setLocation(10, 15);
        response.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
        panel3.add(response);
        // 判断执行命令中是否没有可执行jar
        if (text_cmd.getText().contains("burpsuite_jar_not_found.jar")) {
            btn_run.setEnabled(false);
            check_autorun.setSelected(false);
            check_autorun.setEnabled(false);
        }
        frame.pack();
        frame.setVisible(true);
        btn_run.setFocusable(false);
        if (LatestVersion.equals("")) {
            label0_1.setText("Failed to check the latest version of BurpSuite");
        } else if (!commandString.contains(LatestVersion + ".jar")) {
            label0_1.setText("Latest version:" + LatestVersion + ". Click to download.");
            label0_1.setForeground(Color.BLUE);
            label0_1.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            label0_1.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e2) {
                    super.mouseClicked(e2);
                    try {
                        Desktop.getDesktop().browse(new URI(Main.DownloadURL + Main.LatestVersion));
                    } catch (Exception ignored) {
                    }
                }
            });
        } else {
            label0_1.setText("Your BurpSuite is already the latest version(" + LatestVersion + ")");
            label0_1.setForeground(new Color(0, 100, 0));
        }
    }
}