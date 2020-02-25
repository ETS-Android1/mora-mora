package com.example.hendrik.mianamalaga.unused;

import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;

public class DropBoxClient {

    public static DbxClientV2 getClient(String accessToken) {
        DbxRequestConfig config = new DbxRequestConfig("dropBox/language-app", "en_US");          // Create DropBox client
        DbxClientV2 client = new DbxClientV2(config, accessToken);
        return client;
    }
}
