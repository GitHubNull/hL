package top.oxff.controler;

import burp.api.montoya.http.handler.*;
import burp.api.montoya.logging.Logging;
import top.oxff.GlobalVar;
import top.oxff.tools.HttpTools;

public class HighLightHttpHandler implements HttpHandler {

    private final Logging logger;

    public HighLightHttpHandler(Logging logger) {
        this.logger = logger;
    }

    @Override
    public RequestToBeSentAction handleHttpRequestToBeSent(HttpRequestToBeSent httpRequestToBeSent) {
        logger.logToOutput("HighLightHttpHandler: " + httpRequestToBeSent.method() + " " + httpRequestToBeSent.pathWithoutQuery());
        if (GlobalVar.isHighLightPathEmpty() && GlobalVar.isHighLightSelectStringEmpty()){
            return RequestToBeSentAction.continueWith(httpRequestToBeSent);
        }
        String path = httpRequestToBeSent.pathWithoutQuery();
        if (GlobalVar.isHighLightPath(path)){
            httpRequestToBeSent.annotations().setHighlightColor(GlobalVar.requestPathHighlightColor.get(path));
        } else if (!GlobalVar.isHighLightSelectStringEmpty() && HttpTools.isHttpBodyPrintable(httpRequestToBeSent)){
            String bodyStr = httpRequestToBeSent.bodyToString();
            for (String selectStr : GlobalVar.highLightSelectStringSet) {
                if (bodyStr.contains(selectStr)){
                    httpRequestToBeSent.annotations().setHighlightColor(GlobalVar.selectStringHighlightColor.get(selectStr));
                    break;
                }
            }
        }
        return RequestToBeSentAction.continueWith(httpRequestToBeSent);
    }

    @Override
    public ResponseReceivedAction handleHttpResponseReceived(HttpResponseReceived httpResponseReceived) {
        return ResponseReceivedAction.continueWith(httpResponseReceived);
    }
}
