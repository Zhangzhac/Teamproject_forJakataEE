package edu.uestc.jspchatroom.service;

/**
 * TranslationProvider —— 翻译引擎的统一接口（实验扩展：消息翻译）。
 *
 * <p>把“调用哪个机翻服务”和“怎么用翻译”隔离开：</p>
 * <ul>
 *   <li>{@link MyMemoryProvider}：当前使用的免费实现（不需要注册拿 key）；</li>
 *   <li>以后想换成百度翻译 / 有道 / 大模型，只要再写一个实现类，
 *       把 {@code TranslateServlet} 里的 new 换掉即可，其它代码一行不用动。</li>
 * </ul>
 *
 * <p>这正好是小组分工的好切面：一个人写引擎、一个人写页面，互不影响。</p>
 */
public interface TranslationProvider {

    /**
     * 把一段文本翻译成目标语言。
     *
     * @param text       原文
     * @param targetLang 目标语言代码，如 {@code zh} / {@code en} / {@code ja} / {@code ko}
     * @return 译文
     * @throws Exception 网络异常、额度用尽、服务返回错误等
     */
    String translate(String text, String targetLang) throws Exception;
}
