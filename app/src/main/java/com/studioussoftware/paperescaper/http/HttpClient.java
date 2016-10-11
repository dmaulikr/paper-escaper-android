package com.studioussoftware.paperescaper.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Robbie Wolfe on 10/10/2016.
 */
public class HttpClient {

    private InputStream response;

    /**
     * Execute the given Http Request and store the response data
     * @param postRequest
     * @throws IOException
     */
    public void execute(HttpPost postRequest) throws IOException {
        response = postRequest.execute();
    }

    /**
     * After the Http Request was made, retrieve the response string
     * @return
     * @throws IOException
     */
    public String getResponseString() throws IOException {
        if (response == null) {
            return null;
        }
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[16384];
        while ((nRead = response.read(data, 0, data.length)) != -1) {
            outputStream.write(data, 0, nRead);
        }
        outputStream.flush();
        return outputStream.toString();
    }
}
