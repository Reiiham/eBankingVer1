package ma.ensa.ebankingver1.ai;

import java.util.Map;

public class AIResponse {
    private String intent;
    private String message;
    private boolean success;
    private Map<String, String> parameters;

    public AIResponse(String intent, String message, boolean success, Map<String, String> parameters) {
        this.intent = intent;
        this.message = message;
        this.success = success;
        this.parameters = parameters;
    }

    public String getIntent() {
        return intent;
    }

    public String getMessage() {
        return message;
    }

    public boolean isSuccess() {
        return success;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }
}
/*
public class AIResponse {
    private String responseText;
    private boolean success;

    public AIResponse(String responseText, boolean success) {
        this.responseText = responseText;
        this.success = success;
    }

    public AIResponse() {
        this.responseText = "";
        this.success = false;
    }

    public String getResponseText() {
        return responseText;
    }

    public void setResponseText(String responseText) {
        this.responseText = responseText;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}

 */