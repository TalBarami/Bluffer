package Implementation.Tools;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Used to parse JSON files
 */
public class JsonParser {
    public JsonCreateObject reader(String jsonFile) {
        Gson gson = new Gson();
        JsonCreateObject data = null;
        try {
            BufferedReader br = new BufferedReader(new FileReader(jsonFile));
            //convert the json string back to object
            data = gson.fromJson(br, JsonCreateObject.class);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return data;
    }
}