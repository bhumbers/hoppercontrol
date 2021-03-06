package edu.cmu.cs.graphics.hopper.net;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;


/**
 * Used for sending gameplay data off to remote server for analysis
 */
public class SnapServerInterface extends ServerInterface {
    public SnapServerInterface(String hostURI, int hostPort) {
        super(hostURI, hostPort);
    }

    public void sendPlaySnap(HopperPlaySnap snap) {
        HttpPost msg;
        try {
            msg = new HttpPost(new URI("http", null, hostURI, hostPort, "/snap", "", "anchor"));
        }
        catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }

        msg.setHeader("Content-Type", "application/json");

        //Write snap to JSON and attach as body
        try {
            String snapJson = gsonSend.toJson(snap);
            msg.setEntity(new StringEntity(snapJson));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return;
        }

        try {
            HttpResponse response = session.execute(msg);
            HttpEntity respEntity = response.getEntity();

            if (respEntity != null) {
                String content =  EntityUtils.toString(respEntity);
                System.out.println("Snap response: " + content);
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
