/**
 * A HTTP plugin for Cordova / Phonegap
 */
package com.synconset;

import com.github.kevinsawicki.http.HttpRequest;
import com.github.kevinsawicki.http.HttpRequest.HttpRequestException;

import org.apache.cordova.CallbackContext;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.UnknownHostException;
import java.util.Map;

import javax.net.ssl.SSLHandshakeException;

public class CordovaHttpPostRaw extends CordovaHttp implements Runnable {

    private String body;

    public String getBody() {
        return this.body;
    }

    public CordovaHttpPostRaw(String urlString, String body, Map<String, String> headers, CallbackContext callbackContext) {
        super(urlString, null, headers, callbackContext);
        this.body = body;
    }

    @Override
    public void run() {
        try {
            HttpRequest request = HttpRequest.post(this.getUrlString());
            // Setup security properties.
            this.setupSecurity(request);
            // Setup charset, headers.
            request.acceptCharset(CHARSET);
            request.headers(this.getHeaders());
            // Send raw data.
            request.execute(this.getBody());
            int code = request.code();
            String body = request.body(CHARSET);
            JSONObject response = new JSONObject();
            this.addResponseHeaders(request, response);
            response.put("status", code);
            if (code >= 200 && code < 300) {
                response.put("data", body);
                this.getCallbackContext().success(response);
            } else {
                response.put("error", body);
                this.getCallbackContext().error(response);
            }
        } catch (JSONException e) {
            this.respondWithError("There was an error generating the response");
        }  catch (HttpRequestException e) {
            if (e.getCause() instanceof UnknownHostException) {
                this.respondWithError(0, "The host could not be resolved");
            } else if (e.getCause() instanceof SSLHandshakeException) {
                this.respondWithError("SSL handshake failed");
            } else {
                this.respondWithError("There was an error with the request");
            }
        }
    }
}
