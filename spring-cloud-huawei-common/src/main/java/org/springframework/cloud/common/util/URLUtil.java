package org.springframework.cloud.common.util;

/**
 * @Author wangqijun
 * @Date 17:26 2019-08-07
 **/
public class URLUtil {
  private static final String SCHEMA_SEPRATOR = "://";

  /**
   * Convert url to http or https
   * @param restUrl
   * @param scheme
   * @return
   */
  public static String transform(String restUrl, String scheme) {
    if (restUrl == null) {
      return null;
    }
    return scheme + SCHEMA_SEPRATOR
        + restUrl.substring(restUrl.indexOf(SCHEMA_SEPRATOR) + SCHEMA_SEPRATOR.length());
  }

  /**
   * Compare two urls, if the domain and port are the same, they are considered equal
   * @param url1
   * @param url2
   * @return
   */
  public static boolean isEquals(String url1, String url2) {
    if (url1 == null || url2 == null) {
      return false;
    }
    String url1ComparePart = url1.substring(url1.indexOf(SCHEMA_SEPRATOR) + SCHEMA_SEPRATOR.length());
    String url2ComparePart = url2.substring(url2.indexOf(SCHEMA_SEPRATOR) + SCHEMA_SEPRATOR.length());
    return removeSlash(url1ComparePart).equals(removeSlash(url2ComparePart));
  }

  private static String removeSlash(String url) {
    if (url.endsWith("/")) {
      String result = url.substring(0, url.length() - 1);
      return result;
    }
    return url;
  }
}
