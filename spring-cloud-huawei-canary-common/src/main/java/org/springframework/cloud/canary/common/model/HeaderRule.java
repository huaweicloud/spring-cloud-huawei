package org.springframework.cloud.canary.common.model;

/**
 * @Author GuoYl123
 * @Date 2019/10/17
 **/
public class HeaderRule {
    //正则
    private String regex;
    //是否区分大小写 false区分 true不区分
    private Boolean caseInsensitive = false;
    //精准匹配
    private String exact;

    public HeaderRule() {
    }

    public boolean match(String str) {
        if (!caseInsensitive) {
            str = str.toLowerCase();
            exact = exact == null ? null : exact.toLowerCase();
            regex = regex == null ? null : regex.toLowerCase();
        }
        if (exact != null && !str.equals(exact)) {
            return false;
        }
        if (regex != null && !str.matches(regex)) {
            return false;
        }
        return true;
    }

    public String getRegex() {
        return regex;
    }

    public void setRegex(String regex) {
        this.regex = regex;
    }

    public Boolean getCaseInsensitive() {
        return caseInsensitive;
    }

    public void setCaseInsensitive(Boolean caseInsensitive) {
        this.caseInsensitive = caseInsensitive;
    }

    public String getExact() {
        return exact;
    }

    public void setExact(String exact) {
        this.exact = exact;
    }

    @Override
    public String toString() {
        return "HeaderRule{" +
                "regex='" + regex + '\'' +
                ", caseInsensitive=" + caseInsensitive +
                ", exact='" + exact + '\'' +
                '}';
    }
}
