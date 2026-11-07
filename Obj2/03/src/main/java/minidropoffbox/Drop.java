package minidropoffbox;
import java.time.Instant;
/**
 * Drop 类用于表示一次“资料投递”。
 * 每个上传的文件会被封装为一个 Drop 对象，包含唯一 ID、文件名、MIME 类型、
 * 文件的二进制内容、描述信息，以及上传时间。
 */
public class Drop {
    private final String id;        // 唯一 ID（由服务器生成）
    private final String filename;  // 文件名
    private final String mime;      // MIME 类型
    private final byte[] data;      // 文件数据（二进制）
    private final String desc;      // 用户描述
    private final Instant createdAt; // 上传时间

    /**
     * 构造方法，在创建 Drop 对象时初始化所有字段。
     *
     * @param id       系统生成的唯一 ID
     * @param filename 文件名
     * @param mime     MIME 类型
     * @param data     文件字节数组
     * @param desc     用户描述
     */
    public Drop(String id, String filename, String mime, byte[] data, String desc) {
        this.id = id;
        this.filename = filename;
        this.mime = mime;
        this.data = data;
        this.desc = desc;
        this.createdAt = Instant.now(); // 记录创建时间
    }

    public String getId() { return id; }             // 获取唯一 ID
    public String getFilename() { return filename; } // 获取文件名
    public String getMime() { return mime; }         // 获取 MIME 类型
    public byte[] getData() { return data; }         // 获取文件数据
    public String getDesc() { return desc; }         // 获取描述
    public Instant getCreatedAt() { return createdAt; } // 获取上传时间
}
