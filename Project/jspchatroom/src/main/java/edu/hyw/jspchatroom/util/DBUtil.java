package edu.hyw.jspchatroom.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * DBUtil —— JDBC 工具类（实验 07 的核心）。
 *
 * <p>职责：</p>
 * <ol>
 *   <li>加载 H2 驱动（com.h2database / org.h2.Driver）；</li>
 *   <li>集中管理数据库连接参数（H2 <b>文件模式</b>）；</li>
 *   <li>提供 {@link #getConnection()} 与关闭资源的静态方法。</li>
 * </ol>
 *
 * <p>连接参数的优先级：</p>
 * <pre>
 *   JVM 系统属性 -Dchatroom.jdbc.url=...   （方便测试/答辩时临时切换数据库）
 *   &gt; classpath 下的 db.properties（本实验默认指向项目内 db 文件夹）
 *   &gt; 内置默认值 jdbc:h2:file:~/jspchatroom-db/chatroom
 * </pre>
 *
 * <p>本实验把数据库文件放在<b>项目内的 db 目录</b>：</p>
 * <pre>
 *   jspchatroom/db/chatroom.mv.db
 * </pre>
 * <p>目录不存在时会在启动阶段自动创建（{@link #resolveDatabaseFile(String)}）。
 * 该文件不随 Tomcat 重启或重新部署 WAR 而丢失，这正是实验要求中
 * “服务器重启后仍能查看历史消息”的关键。</p>
 */
public final class DBUtil {

    /** H2 驱动类名。 */
    public static final String DRIVER = "org.h2.Driver";

    /** 默认 JDBC URL：找不到 db.properties 时的兜底配置。 */
    private static final String DEFAULT_URL =
            "jdbc:h2:file:~/jspchatroom-db/chatroom";

    private static final String DEFAULT_USER = "sa";
    private static final String DEFAULT_PASSWORD = "";

    /** H2 文件模式的 URL 前缀。 */
    private static final String FILE_URL_PREFIX = "jdbc:h2:file:";

    private static final String URL;
    private static final String USER;
    private static final String PASSWORD;

    /** 数据库文件（绝对路径）；内存数据库时为 null。 */
    private static Path databaseFile;

    static {
        Properties props = new Properties();
        boolean configured = false;
        // 读取 src/main/resources/db.properties（会被复制到 WEB-INF/classes）
        try (InputStream in = DBUtil.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (in != null) {
                props.load(new InputStreamReader(in, StandardCharsets.UTF_8));
                configured = true;
            }
        } catch (IOException e) {
            System.err.println("[DBUtil] 读取 db.properties 失败，使用内置默认配置: " + e.getMessage());
        }
        if (!configured) {
            System.err.println("[DBUtil] 未找到 db.properties，使用内置默认配置: " + DEFAULT_URL);
        }

        URL = System.getProperty("chatroom.jdbc.url",
                props.getProperty("jdbc.url", DEFAULT_URL)).trim();
        USER = System.getProperty("chatroom.jdbc.user",
                props.getProperty("jdbc.user", DEFAULT_USER)).trim();
        PASSWORD = System.getProperty("chatroom.jdbc.password",
                props.getProperty("jdbc.password", DEFAULT_PASSWORD));

        // 1. 加载驱动：这一步让 JVM 注册 org.h2.Driver（JDBC 4 之后虽可自动注册，
        //    但显式加载能给出更清晰的错误提示，也是教材上的标准流程）。
        try {
            Class.forName(DRIVER);
            System.out.println("[DBUtil] H2 驱动加载成功: " + DRIVER);
        } catch (ClassNotFoundException e) {
            // 驱动没打进 WEB-INF/lib 是最常见的错误，这里给出明确提示
            throw new ExceptionInInitializerError(
                    "[DBUtil] 找不到 H2 驱动 " + DRIVER
                            + "，请确认 h2-2.2.224.jar 已在 WEB-INF/lib 中（或 pom.xml 已引入 h2 依赖）");
        }
        System.out.println("[DBUtil] JDBC URL = " + URL);

        // 2. 解析出数据库文件路径（项目内的 db 目录），目录不存在时自动创建
        databaseFile = resolveDatabaseFile(URL);
        if (databaseFile != null) {
            Path dir = databaseFile.getParent();
            try {
                if (dir != null) {
                    Files.createDirectories(dir);   // 项目内还没有 db 目录时自动建好
                }
                System.out.println("[DBUtil] 数据库目录: " + (dir == null ? "-" : dir));
                System.out.println("[DBUtil] 数据库文件: " + databaseFile);
            } catch (IOException e) {
                System.err.println("[DBUtil] 创建数据库目录失败: " + e.getMessage());
            }
        }
    }

    private DBUtil() {
        // 工具类，禁止实例化
    }

    /**
     * 从 H2 的 file: URL 中解析出数据库文件的绝对路径。
     *
     * <p>支持三种写法：绝对路径、{@code ~} 开头（用户主目录）、相对路径
     * （相对 Tomcat 的工作目录，不推荐，容易找不到文件）。</p>
     *
     * @return 文件路径；非文件模式（如 mem:）返回 null
     */
    private static Path resolveDatabaseFile(String url) {
        if (url == null || !url.startsWith(FILE_URL_PREFIX)) {
            return null;
        }
        String path = url.substring(FILE_URL_PREFIX.length());
        int semicolon = path.indexOf(';');        // 去掉 ;MODE=... 之类的参数
        if (semicolon >= 0) {
            path = path.substring(0, semicolon);
        }
        path = path.trim();
        if (path.isEmpty()) {
            return null;
        }
        if (path.startsWith("~")) {               // H2 的 ~ 表示用户主目录
            path = System.getProperty("user.home") + path.substring(1);
        }
        return Paths.get(path).toAbsolutePath().normalize();
    }

    /**
     * 获取一条数据库连接。调用方必须在使用后关闭（建议 try-with-resources）。
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    /** 当前使用的 JDBC URL（页面/日志展示用）。 */
    public static String getUrl() {
        return URL;
    }

    /**
     * 数据库文件的绝对路径（页面/日志展示用）；内存数据库时返回 {@code (内存数据库)}。
     */
    public static String getDatabaseFilePath() {
        return databaseFile == null ? "(内存数据库)" : databaseFile.toString();
    }

    /** 静默关闭资源，避免大量 try/catch 噪音。 */
    public static void close(AutoCloseable... resources) {
        if (resources == null) {
            return;
        }
        for (AutoCloseable r : resources) {
            if (r != null) {
                try {
                    r.close();
                } catch (Exception ignore) {
                    // 关闭失败不影响业务
                }
            }
        }
    }

    /**
     * 数据库是否可以连通（DBInitListener 启动自检用）。
     * 连接失败时会把原因打印到控制台，方便课堂上定位问题。
     */
    public static boolean testConnection() {
        try (Connection c = getConnection();
             Statement st = c.createStatement()) {
            st.execute("SELECT 1");
            return true;
        } catch (SQLException e) {
            System.err.println("[DBUtil] 数据库连接失败: " + e.getMessage());
            return false;
        }
    }
}
