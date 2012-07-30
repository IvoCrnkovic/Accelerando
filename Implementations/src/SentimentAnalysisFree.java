import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.io.InputStream;
import org.json.JSONObject;
import org.json.JSONArray;

import com.mashape.client.*;
import com.mashape.client.exceptions.MashapeClientException;
import com.mashape.client.http.HttpClient;
import com.mashape.client.http.HttpMethod;
import com.mashape.client.http.auth.Auth;
import com.mashape.client.http.auth.QueryAuth;
import com.mashape.client.http.auth.HeaderAuth;
import com.mashape.client.http.auth.CustomHeaderAuth;
import com.mashape.client.http.auth.MashapeAuth;
import com.mashape.client.http.auth.BasicAuth;
import com.mashape.client.http.callback.MashapeCallback;

public class SentimentAnalysisFree {

    private List<Auth> authHandlers;

    public SentimentAnalysisFree (String publicKey, String privateKey) {
        authHandlers = new LinkedList<Auth>();
        authHandlers.add(new MashapeAuth(publicKey, privateKey));
        
    }
    
    /**
     * Synchronous call with optional parameters.
     */
    public JSONObject classifytext(String lang, String text, String exclude) throws MashapeClientException {

        // combine all the params into a hash map
        Map<String, String> parameters = new HashMap<String, String>();
        if (lang != null && !lang.equals("")) {
            parameters.put("lang", lang);
        }
        
        if (text != null && !text.equals("")) {
            parameters.put("text", text);
        }
        
        if (exclude != null && !exclude.equals("")) {
            parameters.put("exclude", exclude);
        }
        
        return (JSONObject) HttpClient.doRequest(
                    HttpMethod.POST,
                    "https://chatterbox-analytics-sentiment-analysis-free.p.mashape.com/sentiment/current/classify_text/",
                    parameters,
                    true,
                    authHandlers);
    }

    /**
     * Synchronous call without optional parameters.
     */
    public JSONObject classifytext(String lang, String text) throws MashapeClientException {
        return classifytext(lang, text, "");
    }


    /**
     * Asynchronous call with optional parameters.
     */
    public Thread classifytext(String lang, String text, String exclude, MashapeCallback mashapeCallback) {

        // combine all the params into a hash map
        Map<String, String> parameters = new HashMap<String, String>();
        if (lang != null && !lang.equals("")) {
            parameters.put("lang", lang);
        }
        
        if (text != null && !text.equals("")) {
            parameters.put("text", text);
        }
        
        if (exclude != null && !exclude.equals("")) {
            parameters.put("exclude", exclude);
        }
        
        return HttpClient.doRequest(
                    HttpMethod.POST,
                    "https://chatterbox-analytics-sentiment-analysis-free.p.mashape.com/sentiment/current/classify_text/",
                    parameters,
                    true,
                    authHandlers,
                    mashapeCallback);
    }

    /**
     * Asynchronous call without optional parameters.
     */
    public Thread classifytext(String lang, String text, MashapeCallback mashapeCallback) {
        return classifytext(lang, text, "", mashapeCallback);
    }

}
