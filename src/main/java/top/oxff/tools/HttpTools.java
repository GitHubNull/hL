package top.oxff.tools;

import burp.api.montoya.http.handler.HttpRequestToBeSent;
import burp.api.montoya.http.message.HttpHeader;
import burp.api.montoya.proxy.ProxyHttpRequestResponse;


import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class HttpTools {
    /**
     * 判断一个 Unicode 代码点是否为可打印字符。
     *
     * @param codePoint Unicode 代码点
     * @return 如果是可打印字符，返回 true；否则返回 false
     */
    public static boolean isPrintable(int codePoint) {
        // 排除控制字符
        if (Character.isISOControl(codePoint)) {
            return false;
        }
        // 排除代理对（Surrogates）
        if (codePoint >= 0xD800 && codePoint <= 0xDFFF) {
            return false;
        }
        // 确保字符在 Unicode 中有定义
        return Character.isDefined(codePoint);
    }

    /**
     * 从 HTTP 请求头中解析出字符集（charset）。
     *
     * @param headers HTTP 请求头
     * @return 如果指定了 charset，则返回对应的 Charset 对象；否则返回 UTF-8
     */
    private static Charset getCharsetFromHeaders(List<HttpHeader> headers) {
        String contentType = null;
        for (HttpHeader header : headers) {
            String name = header.name().toLowerCase();
            if (name.equals("content-type")) {
                contentType = header.value();
                break;
            }
        }
        if (contentType != null) {
            String[] parts = contentType.split(";");
            for (String part : parts) {
                part = part.trim();
                if (part.toLowerCase().startsWith("charset=")) {
                    String charsetName = part.substring(8).trim();
                    try {
                        return Charset.forName(charsetName);
                    } catch (Exception e) {
                        // 如果指定的 charset 无效，则忽略并继续
                        break;
                    }
                }
            }
        }
        // 默认使用 UTF-8
        return StandardCharsets.UTF_8;
    }

    /**
     * 检查 HTTP 请求体是否全部由可打印字符组成。
     *
     * @param httpRequestToBeSent HTTP 请求
     * @return 如果请求体全部由可打印字符组成，则返回 true；否则返回 false
     */
    public static boolean isHttpBodyPrintable(HttpRequestToBeSent httpRequestToBeSent) {
        byte[] bodyBytes = httpRequestToBeSent.body().getBytes();
        List<HttpHeader> headers = httpRequestToBeSent.headers();
        Charset charset = getCharsetFromHeaders(headers);

        String body;
        try {
            body = new String(bodyBytes, charset);
        } catch (Exception e) {
            // 如果解码失败，则认为正文不可打印
            return false;
        }

        int length = body.length();
        for (int offset = 0; offset < length;) {
            int codePoint = body.codePointAt(offset);
            if (!isPrintable(codePoint)) {
                return false;
            }
            offset += Character.charCount(codePoint);
        }
        return true;
    }


    /**
     * 判断一个字符串是否全部由可打印字符组成。
     *
     * @param str 要检查的字符串
     * @return 如果字符串全部由可打印字符组成，则返回 true；否则返回 false
     */
    public static boolean isStringPrintable(String str) {
        if (str == null) {
            return false;
        }

        int length = str.length();
        for (int offset = 0; offset < length;) {
            int codePoint = str.codePointAt(offset);
            if (!isPrintable(codePoint)) {
                return false;
            }
            offset += Character.charCount(codePoint);
        }
        return true;
    }

    /**
     * 检查传入的字节数组组成的是不是可打印字符串。
     * 默认使用 UTF-8 编码进行解码。
     *
     * @param data 字节数组
     * @return 如果字节数组解码后的字符串全部由可打印字符组成，则返回 true；否则返回 false
     */
    public static boolean isBytesPrintable(byte[] data) {
        return isBytesPrintable(data, StandardCharsets.UTF_8);
    }

    /**
     * 检查传入的字节数组组成的是不是可打印字符串。
     *
     * @param data    字节数组
     * @param charset 解码所使用的字符集
     * @return 如果字节数组解码后的字符串全部由可打印字符组成，则返回 true；否则返回 false
     */
    public static boolean isBytesPrintable(byte[] data, Charset charset) {
        if (data == null || charset == null) {
            return false;
        }

        String str;
        try {
            str = new String(data, charset);
        } catch (Exception e) {
            // 如果解码失败，则认为不可打印
            return false;
        }

        return isStringPrintable(str);
    }

    /**
     * 检查传入的 ProxyHttpRequestResponse 对象中的请求报文的所有字节是否都是可打印字符串。
     *
     * @param proxyRequestResponse ProxyHttpRequestResponse 对象
     * @return 如果请求报文全部由可打印字符组成，则返回 true；否则返回 false
     */
    public static boolean isProxyHttpRequestResponsePrintable(ProxyHttpRequestResponse proxyRequestResponse) {
        if (proxyRequestResponse == null) {
            return false;
        }

        // 获取请求字节数组
        byte[] requestBytes = proxyRequestResponse.request().toByteArray().getBytes();

        // 获取请求头列表
        List<HttpHeader> headers = proxyRequestResponse.request().headers();

        // 解析 charset
        Charset charset = getCharsetFromHeaders(headers);

        // 检查字节数组是否可打印
        return isBytesPrintable(requestBytes, charset);
    }
}