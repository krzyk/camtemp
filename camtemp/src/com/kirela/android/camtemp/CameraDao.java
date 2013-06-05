package com.kirela.android.camtemp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CameraDao {
    public List<Camera> getAll(String url) {
        List<Camera> camList;
        String html = new UrlDownloader().download(url);
        Pattern selectPattern = Pattern.compile("<SELECT[^>]+?>(.*?)</SELECT>", Pattern.MULTILINE | Pattern.DOTALL);
        Matcher selectMatcher = selectPattern.matcher(html);
        if (selectMatcher.find()) {
            camList = new ArrayList<Camera>();
            String options = selectMatcher.group(1);
            Pattern cameraPattern = Pattern.compile("<OPTION value=([0-9]+)>([^<]+?)</OPTION>");
            Matcher cameraMatcher = cameraPattern.matcher(options);
            while (cameraMatcher.find()) {
                camList.add(new Camera(Integer.parseInt(cameraMatcher.group(1)), cameraMatcher.group(2)));
            }
        } else {
            camList = Arrays.asList(
                new Camera(472, "Wjazd do Krakowa"),
                new Camera(574, "Michałowice"));
        }
        return camList;
    }
}
