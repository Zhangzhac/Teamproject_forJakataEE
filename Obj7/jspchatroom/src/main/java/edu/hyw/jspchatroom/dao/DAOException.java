package edu.hyw.jspchatroom.dao;

/**
 * DAOException —— 把受检的 {@link java.sql.SQLException} 包装成运行时异常。
 *
 * <p>好处：JSP/Servlet 里调用 DAO 时不必为每个方法写 try/catch，
 * 页面代码保持干净；真正的 SQL 错误留给容器统一报 500，并打印完整堆栈，
 * 便于实验课上排查建表语句、字段名等问题。</p>
 */
public class DAOException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public DAOException(String message, Throwable cause) {
        super(message, cause);
    }
}
