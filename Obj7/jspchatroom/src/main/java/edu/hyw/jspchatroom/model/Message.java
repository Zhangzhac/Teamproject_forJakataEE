package edu.hyw.jspchatroom.model;

import java.io.Serializable;
import java.util.Date;

/**
 * Message —— 聊天消息实体，对应数据库 messages 表的一行。
 *
 * <pre>
 *   messages
 *   ├─ id          消息编号（主键，自增）
 *   ├─ user_id     发送者编号（外键 → users.id）
 *   ├─ content     消息内容
 *   └─ created_at  发送时间
 * </pre>
 *
 * <p>{@code username} 不是 messages 表的字段，而是查询时
 * <b>JOIN users</b> 得到的发送者名字。这样页面只要一个 {@code c:forEach}
 * 就能同时拿到“用户、时间、内容”，无需在 JSP 里二次查询。</p>
 */
public class Message implements Serializable {

    private static final long serialVersionUID = 1L;

    private int id;
    private int userId;
    private String username;
    private String content;
    private Date createdAt;

    public Message() {
    }

    public Message(int id, int userId, String username, String content, Date createdAt) {
        this.id = id;
        this.userId = userId;
        this.username = username;
        this.content = content;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Message{id=" + id + ", username='" + username + "', content='" + content + "'}";
    }
}
