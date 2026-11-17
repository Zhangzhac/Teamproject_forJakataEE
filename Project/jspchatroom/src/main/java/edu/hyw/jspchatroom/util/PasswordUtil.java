package edu.hyw.jspchatroom.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HexFormat;

/**
 * PasswordUtil —— 密码摘要工具（实验 07「思考与扩展 2：密码安全性」的落地实现）。
 *
 * <p>本实验的登录校验要求“从数据库读取密码后比较”。如果把明文密码直接入库，
 * 一旦数据库泄露，所有账号都会暴露。因此这里改用
 * <b>随机盐 + SHA-256 摘要</b> 的方式存储：</p>
 *
 * <pre>
 *   数据库 password 字段的格式：  salt(32 位十六进制) + "$" + sha256(salt + 明文) 的十六进制
 *   例如： 3f9c...e1$9a7b...
 * </pre>
 *
 * <p>登录时无法从摘要反推出明文，只能用同样的“盐 + 明文”再算一次摘要，
 * 与库中的值做定长比较（{@link MessageDigest#isEqual}）来判断密码是否正确。</p>
 *
 * <p>说明：真实项目更推荐 BCrypt / Argon2 这类自带“慢哈希”特性的算法，
 * 这里用 JDK 自带 API 实现，便于课堂演示且不引入额外依赖。</p>
 */
public final class PasswordUtil {

    private static final String ALGORITHM = "SHA-256";

    /** 盐的字节数，16 字节 = 32 个十六进制字符。 */
    private static final int SALT_BYTES = 16;

    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordUtil() {
    }

    /**
     * 生成“盐$摘要”形式的密码串，用于写入 users.password 字段。
     *
     * @param rawPassword 用户输入的明文密码
     * @return 可直接入库的密码串
     */
    public static String hash(String rawPassword) {
        if (rawPassword == null) {
            throw new IllegalArgumentException("密码不能为 null");
        }
        byte[] salt = new byte[SALT_BYTES];
        RANDOM.nextBytes(salt);
        String saltHex = HexFormat.of().formatHex(salt);
        return saltHex + "$" + HexFormat.of().formatHex(digest(salt, rawPassword));
    }

    /**
     * 校验明文密码与数据库中保存的密码串是否匹配。
     *
     * <p>为了兼容“早期版本直接存明文”的旧数据，当库中的值里没有 {@code $}
     * 分隔符时，按明文比较处理。</p>
     *
     * @param rawPassword 用户本次输入的明文密码
     * @param stored      数据库中读取到的 password 字段值
     * @return 匹配返回 true
     */
    public static boolean matches(String rawPassword, String stored) {
        if (rawPassword == null || stored == null || stored.isEmpty()) {
            return false;
        }
        int sep = stored.indexOf('$');
        if (sep < 0) {
            // 兼容明文存储的历史数据
            return MessageDigest.isEqual(
                    rawPassword.getBytes(StandardCharsets.UTF_8),
                    stored.getBytes(StandardCharsets.UTF_8));
        }
        String saltHex = stored.substring(0, sep);
        String expected = stored.substring(sep + 1);
        byte[] salt;
        try {
            salt = HexFormat.of().parseHex(saltHex);
        } catch (IllegalArgumentException e) {
            return false; // 数据被破坏，直接判定失败
        }
        String actual = HexFormat.of().formatHex(digest(salt, rawPassword));
        // 定长比较，避免通过比较耗时猜测密码
        return MessageDigest.isEqual(
                actual.getBytes(StandardCharsets.UTF_8),
                expected.getBytes(StandardCharsets.UTF_8));
    }

    private static byte[] digest(byte[] salt, String rawPassword) {
        try {
            MessageDigest md = MessageDigest.getInstance(ALGORITHM);
            md.update(salt);
            md.update(rawPassword.getBytes(StandardCharsets.UTF_8));
            return md.digest();
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 是 JDK 必备算法，正常不会发生
            throw new IllegalStateException("当前 JDK 不支持 " + ALGORITHM, e);
        }
    }
}
