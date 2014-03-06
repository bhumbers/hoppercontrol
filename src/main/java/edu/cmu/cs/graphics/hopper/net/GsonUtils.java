package edu.cmu.cs.graphics.hopper.net;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.FieldNamingStrategy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.lang.reflect.Field;

/**
 * Provides gson objects configured as necessary for crowdanim framework
 * (ie: all fields in lower case, as golang's mgo on the backend assumes as much)
 */
public class GsonUtils {
    public static Gson getCrowdAnimSenderGson() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setFieldNamingStrategy(new MyFieldNamingStrategy());
        Gson gson = gsonBuilder.create();
        return gson;
    }

    /** HACK, 2.28.2014: mGo's bson encoder/decoder on the backend requires all-lowercase field names for
     * unmarshaling to work as expected. However, based on Go's naming conventions, we end up reintroducing
     * capital letters when we pull JSON docs from the DB, unmarshal into Go, then remarshal to JSON for transport
     * So, we need to use a different gson with altered field name handling for receiving vs sending JSON *
     * Yep... */
    public static Gson getCrowdAnimReceiverGson() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE);
        Gson gson = gsonBuilder.create();
        return gson;
    }
}


//Source: http://stackoverflow.com/questions/6362587/parsing-json-maps-dictionaries-with-gson
class MyFieldNamingStrategy implements FieldNamingStrategy
{
    //Translates the Java field name into its JSON element name representation.
    @Override
    public String translateName(Field field)
    {
        String name = field.getName();
        return name.toLowerCase();
    }
}