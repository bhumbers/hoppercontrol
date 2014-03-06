package edu.cmu.cs.graphics.hopper.net;

import com.google.gson.reflect.TypeToken;
import edu.cmu.cs.graphics.hopper.control.BipedHopperControl;
import edu.cmu.cs.graphics.hopper.control.ControlProvider;
import edu.cmu.cs.graphics.hopper.control.ControlProviderDefinition;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Used to retrieve controls from knowledge base during control testing phase
 */
public class ContolServerInterface extends ServerInterface {
    public ContolServerInterface(String hostURI, int hostPort) {
        super(hostURI, hostPort);
    }

    /** Synchronously asks server to provide a control suitable for given context *
     * Yeah, you heard me right... a synchronous network call. Legit. */
    public ControlProviderDefinition getControlForContext(PlayContext context) {
        HttpPost msg;
        try {
            msg = new HttpPost(new URI("http", null, hostURI, hostPort, "/retrieve_control", "", "anchor"));
        }
        catch (URISyntaxException e) {
            e.printStackTrace();
            return null;
        }

        msg.setHeader("Content-Type", "application/json");

        //Write context to JSON and attach as body
        try {
            String body = gsonSend.toJson(context);
            msg.setEntity(new StringEntity(body));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }

        try {
            HttpResponse response = session.execute(msg);
            HttpEntity respEntity = response.getEntity();

            if (respEntity != null) {
                String content =  EntityUtils.toString(respEntity);
                System.out.println("Control retrieval response: " + content);

                //HACK: To ensure correct control typing, provide a biped hopper type
                //TODO: Figure how to handle this for generic control types
                Type controlProvType = new TypeToken<ControlProviderDefinition<BipedHopperControl>>(){}.getType();

                ControlProviderDefinition controlDef = gsonReceive.fromJson(content, controlProvType);
                return controlDef;
            }
            else {
                return null;
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
