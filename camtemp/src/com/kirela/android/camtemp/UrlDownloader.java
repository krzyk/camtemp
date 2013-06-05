package com.kirela.android.camtemp;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.Charset;

import android.util.Log;
import org.apache.commons.io.IOUtils;

public class UrlDownloader {
    private static final String LOG = UrlDownloader.class.getName();

    public String download(String strUrl) {
        InputStream is = null;
        try {
            URL url = new URL(strUrl);
            // TODO: handle proxy here
            // http://stackoverflow.com/questions/10811698/getting-wifi-proxy-settings-in-android/13616054#13616054
            // http://stackoverflow.com/questions/9446871/how-users-developers-can-set-the-androids-proxy-configuration-for-versions-2-x
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(20000);
            conn.setConnectTimeout(20000);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.connect();
            int resCode = conn.getResponseCode();
            Log.d(LOG, "Response code = " + resCode);
            return IOUtils.toString(conn.getInputStream(), Charset.forName("ISO-8859-2"));
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(e);
        } catch (ProtocolException e) {
            throw new IllegalArgumentException(e);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        } finally {
            IOUtils.closeQuietly(is);
        }
    }
}