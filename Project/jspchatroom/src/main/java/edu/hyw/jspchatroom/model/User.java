package edu.hyw.jspchatroom.model;

import java.io.Serializable;
import java.util.Date;

/**
 * User —— 用户实体，对应数据库 users 表的一行。
 *
 * <p>字段与表结构一一对应（实验要求：用户编号、用户名、密码、注册时间、
 * 最近登录时间、在线状态）：</p>
 *
 * <pre>
 *   users
 *   ├─ id            用户编号（主键，自增）
 *   ├─ username      用户名（唯一）
 *   ├─ password      密码（加盐 SHA-256 摘要，见 PasswordUtil）
 *   ├─ created_at    注册时间
 *   ├─ last_login_at 最近登录时间
 *   └─ online        在线状态
 * </pre>
 *
 * <p>实现 {@link Serializable} 才能放进 HttpSession（Tomcat 重启时可能序列化会话）。</p>
 */
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    private int id;
    private String username;
    private String password;
    private Date createdAt;
    private Date lastLoginAt;
    private boolean online;

    public User() {
    }

    public User(int id, String username, String password, Date createdAt, Date lastLoginAt, boolean online) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.createdAt = createdAt;
        this.lastLoginAt = lastLoginAt;
        this.online = online;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(Date lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    @Override
    public String toString() {
        return "User{id=" + id + ", username='" + username + "', online=" + online + '}';
    }
}
