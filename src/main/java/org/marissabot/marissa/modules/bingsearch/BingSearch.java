package org.marissabot.marissa.modules.bingsearch;

import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.marissabot.marissa.lib.Persist;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class BingSearch {

    private static final String appid;
    private static final String MAX_RESULTS = "20";

    static {
        appid = Persist.load("bingsearch", "appid");
    }

    private BingSearch() {
    }

    public static List<String> search(String query) throws IOException {
        if (query == null || query.isEmpty())
            throw new IllegalArgumentException("cannot search for nothing");

        URI uri;

        try {
            uri = new URIBuilder()
                    .setScheme("https")
                    .setHost("api.cognitive.microsoft.com")
                    .setPath("/bing/v5.0/search")
                    .addParameter("q", query)
                    .addParameter("mkt", "en-GB")
                    .addParameter("count", MAX_RESULTS)
                    .build();
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Couldn't build URI for search", e);
        }

        LoggerFactory.getLogger(BingSearch.class).info("[GET] " + uri.toString());

        HttpGet get = new HttpGet(uri);

        get.addHeader("Ocp-Apim-Subscription-Key", appid);

        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        CloseableHttpResponse r = httpClient.execute(get);
        String searchResults = IOUtils.toString(r.getEntity().getContent());

        ObjectMapper o = new ObjectMapper();
        JsonNode n = o.readTree(searchResults);

        List<String> res = n.get("webPages").get("value").findValuesAsText("url");

        LoggerFactory.getLogger(BingSearch.class).info(String.join("\n", res));

        return mapRedirects(res);
    }

    public static List<String> imageSearch(String query) throws IOException {
        if (query == null || query.isEmpty())
            throw new IllegalArgumentException("cannot image search with nothing");

        URI uri;
        try {
            uri = new URIBuilder()
                    .setScheme("https")
                    .setHost("api.cognitive.microsoft.com")
                    .setPath("/bing/v5.0/images/search")
                    .addParameter("q", query)
                    .addParameter("mkt", "en-GB")
                    .addParameter("count", MAX_RESULTS)
                    .build();
        } catch (URISyntaxException e) {
            throw new IllegalStateException("couldn't build image search URI", e);
        }

        return mapRedirects(fetch(uri));
    }

    public static List<String> animatedSearch(String query) throws IOException {
        if (query == null || query.isEmpty())
            throw new IllegalArgumentException("cannot animated search with nothing");

        URI uri;
        try {
            uri = new URIBuilder()
                    .setScheme("https")
                    .setHost("api.cognitive.microsoft.com")
                    .setPath("/bing/v5.0/images/search")
                    .addParameter("q", query)
                    .addParameter("mkt", "en-GB")
                    .addParameter("imageType", "animatedGif")
                    .addParameter("count", MAX_RESULTS)
                    .build();
        } catch (URISyntaxException e) {
            throw new IllegalStateException("couldn't build animated search URI", e);
        }

        return mapRedirects(fetch(uri));
    }

    private static List<String> fetch(URI uri) throws IOException {
        LoggerFactory.getLogger(BingSearch.class).info("[GET] " + uri.toString());

        HttpGet get = new HttpGet(uri);

        get.addHeader("Ocp-Apim-Subscription-Key", appid);

        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        CloseableHttpResponse r = httpClient.execute(get);
        String searchResults = IOUtils.toString(r.getEntity().getContent());

        ObjectMapper o = new ObjectMapper();
        JsonNode n = o.readTree(searchResults);

        List<String> res = n.get("value").findValuesAsText("contentUrl");

        LoggerFactory.getLogger(BingSearch.class).info(String.join("\n", res));

        return res;
    }

    private static List<String> mapRedirects(List<String> uris) {
        return uris.parallelStream()
                .map((url) -> {
                    try {
                        URLConnection con = new URL(url).openConnection();
                        con.setReadTimeout(700); // give up early
                        LoggerFactory.getLogger(BingSearch.class).info("Following redirects for '" + url + "'");
                        con.connect();
                        InputStream is = con.getInputStream();
                        LoggerFactory.getLogger(BingSearch.class).info("Redirected URL is '" + con.getURL() + "'");
                        is.close();
                        return Optional.of(con.getURL().toString());
                    } catch (Exception e) {
                        return Optional.empty();
                    }
                })
                .filter(Optional::isPresent)
                .map(url -> (String) url.get()) // oh java
                .collect(Collectors.toList());
    }

}
