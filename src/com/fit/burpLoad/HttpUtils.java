package com.fit.burpLoad;

import javax.net.ssl.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

/**
 * @className: HttpUtils
 * @description:
 * @author: Aim
 * @date: 2023/6/16
 **/
public class HttpUtils {

    private static final String C_TYPE_FORM = "application/x-www-form-urlencoded;charset=utf-8";
    private static final String C_TYPE_JSON = "application/json; charset=utf-8";
    private static final String CHARSET = "utf-8";

    private static HttpUtils instance = null;

    public static void main(String[] args) throws SocketTimeoutException, IOException {
        String resp = getInstance().getJson("https://portswigger.net/burp/releases/data?pageSize=1");
        System.out.println(resp);
    }

    public static HttpUtils getInstance() {
        if (instance == null) {
            return new HttpUtils();
        }
        return instance;
    }

    public static String getparam(String url, String label) {
        String before = label + "=";
        String[] split = url.substring(url.indexOf('?') + 1).split("&");
        for (String str : split) {
            if (str.startsWith(before)) {
                return str.substring(before.length());
            }
        }
        return null;
    }

    /**
     * 以application/json; charset=utf-8方式传输
     *
     * @param url
     * @param jsonContent
     */
    public String postJson(String url, String jsonContent) throws IOException {
        return doRequest("POST", url, jsonContent, 15000, 15000, C_TYPE_JSON, null);
    }

    /**
     * 以application/json; charset=utf-8方式传输
     *
     * @param url
     */
    public String getJson(String url) throws IOException {
        return doRequest("GET", url, null, 15000, 15000, C_TYPE_JSON, null);
    }

    /**
     * POST 以application/x-www-form-urlencoded;charset=utf-8方式传输
     *
     * @param url
     */
    public String postForm(String url) throws IOException {
        return postForm(url, null);
    }

    /**
     * POST 以application/x-www-form-urlencoded;charset=utf-8方式传输
     *
     * @param url
     * @param params
     */
    public String postForm(String url, Map<String, String> params) throws IOException {
        return doRequest("POST", url, buildQuery(params), 15000, 15000, C_TYPE_FORM, null);
    }

    /**
     * GET 以application/x-www-form-urlencoded;charset=utf-8方式传输
     *
     * @param url
     */
    public String getForm(String url) throws IOException {
        return getForm(url, null);
    }

    /**
     * GET 以application/x-www-form-urlencoded;charset=utf-8方式传输
     *
     * @param url
     * @param params
     */
    public String getForm(String url, Map<String, String> params) throws IOException {
        return doRequest("GET", url, buildQuery(params), 15000, 15000, C_TYPE_FORM, null);
    }

    /**
     * @param method         请求的method post/get
     * @param uri            请求url
     * @param requestContent 请求参数
     * @param connTimeout    请求超时
     * @param readTimeout    响应超时
     * @param c_type         请求格式  xml/json等等
     * @param headerMap      请求header中要封装的参数
     * @return
     */
    private String doRequest(String method, String uri, String requestContent, int connTimeout, int readTimeout, String c_type,
                             Map<String, String> headerMap) throws IOException {
        URL url = new URL(uri);
        HttpURLConnection conn = null;
        OutputStream out = null;
        String rsp = null;
        try {
            if ("https".equals(url.getProtocol())) {
                SSLContext ctx = null;
                try {
                    ctx = SSLContext.getInstance("TLSv1.2");
                    ctx.init(new KeyManager[0], new TrustManager[]{new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                        }

                        @Override
                        public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                        }

                        @Override
                        public X509Certificate[] getAcceptedIssuers() {
                            return null;
                        }
                    }}, new SecureRandom());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                HttpsURLConnection connHttps = (HttpsURLConnection) url.openConnection();
                connHttps.setSSLSocketFactory(ctx.getSocketFactory());
                connHttps.setHostnameVerifier(new HostnameVerifier() {
                    public boolean verify(String hostname, SSLSession session) {
                        return true;
                    }
                });
                conn = connHttps;
            } else {
                conn = (HttpURLConnection) url.openConnection();
            }
            if (headerMap != null) {
                for (Map.Entry<String, String> entry : headerMap.entrySet()) {
                    conn.setRequestProperty(entry.getKey(), entry.getValue());
                }
            }
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setRequestMethod(method);
            conn.setReadTimeout(readTimeout);
            conn.setConnectTimeout(connTimeout);
            conn.setRequestProperty("Content-Type", c_type);
            conn.setRequestProperty("Accept", "text/xml,text/javascript,text/html,application/json");
            if (requestContent != null && requestContent.trim().length() > 0) {
                out = conn.getOutputStream();
                out.write(requestContent.getBytes(CHARSET));
            }
            rsp = getResponseAsString(conn);
        } finally {
            if (out != null) {
                out.close();
            }
            if (conn != null) {
                conn.disconnect();
            }
        }
        return rsp;
    }

    private String getResponseAsString(HttpURLConnection conn) throws IOException {
        if (HttpURLConnection.HTTP_OK == conn.getResponseCode()) {
            return getStreamAsString(conn.getInputStream(), CHARSET);
        } else {
            String msg = getStreamAsString(conn.getErrorStream(), CHARSET);
            if (msg != null && msg.trim().length() > 0) {
                throw new IOException(conn.getResponseCode() + ":" + conn.getResponseMessage());
            } else {
                return msg;
            }
        }
    }

    private String getStreamAsString(InputStream stream, String charset) throws IOException {
        try (Scanner sc = new Scanner(stream, charset)) {
            StringBuilder sb = new StringBuilder();
            while (sc.hasNextLine()) {
                sb.append(sc.nextLine());
            }
            return sb.toString();
        } finally {
            if (stream != null) {
                stream.close();
            }
        }
    }

    private String buildQuery(Map<String, String> params) throws IOException {
        if (params == null || params.isEmpty()) {
            return "";
        }

        StringBuilder query = new StringBuilder();
        Set<Map.Entry<String, String>> entries = params.entrySet();
        boolean hasParam = false;

        for (Map.Entry<String, String> entry : entries) {
            String name = entry.getKey();
            String value = entry.getValue();
            if (hasParam) {
                query.append("&");
            } else {
                hasParam = true;
            }
            query.append(name).append("=").append(URLEncoder.encode(value, CHARSET));
        }
        return query.toString();
    }
}
