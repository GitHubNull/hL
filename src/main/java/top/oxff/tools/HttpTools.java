package top.oxff.tools;

import burp.api.montoya.http.handler.HttpRequestToBeSent;
import burp.api.montoya.http.message.HttpHeader;
import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.proxy.ProxyHttpRequestResponse;
import top.oxff.model.SelectionPositionType;


import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.IllegalFormatCodePointException;
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

    // 获取http请求头第一行的结束位置
    public static int getHttpRequestFirstLineEndPosition(byte[] requestBytes, int requestLength) {
        if (requestBytes == null || requestLength <= 0){
            return -1;
        }
        int end = -1;
        for (int i = 0; i < requestLength; i++) {
            if (requestBytes[i] == '\n') {
                end = i;
                break;
            } else if (requestBytes[i] == '\r' && requestBytes[i + 1] == '\n') {
                end = i + 1;
                break;
            }
        }
        return end;
    }

    // 获取http请求头结束位置
    public static int getHttpRequestEndPosition(byte[] requestBytes, int requestLength) {
        if (requestBytes == null || requestLength <= 0){
            return -1;
        }

        int end = -1;
        for (int i = 0; i < requestLength; i++) {
            if (requestBytes[i] == '\n' && requestBytes[i + 1] == '\n') {
                end = i + 1;
                break;
            } else if (requestBytes[i] == '\r' && requestBytes[i + 1] == '\n'  && (requestBytes[i + 2] == '\n' || requestBytes[i + 2] == '\r')) {
                end = i + 2;
                break;
            }
        }
        return end;
    }

    /**
     * 根据给定的HttpRequestResponse对象和选择的文本范围，确定选择的文本位置类型
     *
     * @param httpRequestResponse HttpRequestResponse对象，包含HTTP请求和响应信息
     * @param start 选择范围的起始位置
     * @param end 选择范围的结束位置
     * @return 返回选择的文本位置类型，如果选择范围无效或请求为空，则返回NONE
     */
    public static SelectionPositionType getSelectHttpRequestTextPositionType(HttpRequestResponse httpRequestResponse, int start,
                                                                                 int end) {
        // 检查输入参数的有效性，如果无效则返回NONE
        if (httpRequestResponse == null || start < 0 || end <= 0 || end < start) {
            return SelectionPositionType.NONE;
        }

        // 获取HTTP请求对象
        HttpRequest httpRequest = httpRequestResponse.request();
        // 如果请求对象为空，则返回NONE
        if (httpRequest == null) {
            return SelectionPositionType.NONE;
        }

        // 将HTTP请求转换为字节数组
        byte[] requestBytes = httpRequest.toByteArray().getBytes();
        // 如果字节数组为空，则返回NONE
        if (requestBytes == null) {
            return SelectionPositionType.NONE;
        }

        // 获取请求的总长度
        int requestLength = requestBytes.length;
        // 如果请求长度小于等于0，则返回NONE
        if (requestLength == 0) {
            return SelectionPositionType.NONE;
        }

        // 如果选择范围超出请求长度，则返回NONE
        if (start >= requestLength || end > requestLength) {
            return SelectionPositionType.NONE;
        }

        // 获取请求头第一行的结束位置
        int headerFirstLineEndPosition = getHttpRequestFirstLineEndPosition(requestBytes, requestLength);
        // 如果无法确定第一行的结束位置，则返回NONE
        if (headerFirstLineEndPosition == -1) {
            return SelectionPositionType.NONE;
        }

        // 获取请求头的结束位置
        int headerEndPosition = getHttpRequestEndPosition(requestBytes, requestLength);
        // 如果无法确定请求头的结束位置，则返回NONE
        if (headerEndPosition == -1) {
            return SelectionPositionType.NONE;
        }

        // 获取请求体的起始位置
        int bodyStartPosition = httpRequest.bodyOffset();

        // 根据选择范围和请求头、请求体的位置关系，确定并返回选择的文本位置类型
        if (0 == start) {
            if (end < headerFirstLineEndPosition) {
                return SelectionPositionType.SUB_HEADER_FIRST_LINE;
            } else if (end == headerFirstLineEndPosition) {
                return SelectionPositionType.HEADER_FIRST_LINE;
            } else if (end < headerEndPosition) {
                return SelectionPositionType.SUB_HEADER_LINES;
            } else if (end == headerEndPosition) {
                return SelectionPositionType.ALL_HEADER_LINES;
            } else if (end <= bodyStartPosition) {
                return SelectionPositionType.ALL_HEADER_LINES_AND_BLANK_LINES_BEFORE_BODY;
            } else if (end > bodyStartPosition && end < requestLength){
                return SelectionPositionType.ALL_HEADER_LINES_AND_BLANK_LINES_AND_SUB_BODY;
            }else {
                return SelectionPositionType.ALL;
            }
        } else {
            if (start < headerFirstLineEndPosition && end < headerFirstLineEndPosition) {
                return SelectionPositionType.IN_HEADER_FIRST_LINE;
            } else if (start >= headerFirstLineEndPosition && start < headerEndPosition && end < headerEndPosition) {
                return SelectionPositionType.HEADER_OTHER_LINES;
            } else if (start >= headerEndPosition && start < bodyStartPosition) {
                return SelectionPositionType.BETWEEN_HEADER_END_AND_BODY;
            } else if (start == bodyStartPosition) {
                if (end < requestLength) {
                    return SelectionPositionType.SUB_BODY;
                } else {
                    return SelectionPositionType.BODY;
                }
            } else if (start > bodyStartPosition) {
                if (end < requestLength) {
                    return SelectionPositionType.IN_BODY;
                } else {
                    return SelectionPositionType.SUB_BODY;
                }
            }else {
                return SelectionPositionType.NONE;
            }
        }
    }
}