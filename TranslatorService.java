import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.HttpServerResponse;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.deploy.Verticle;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static java.lang.String.format;

/**
 * Verticle that starts a simple HTTP server to return translations for words
 */
public class TranslatorService extends Verticle {
    private Logger logger;
    public static final String UNKNOWN_WORD = "Unrecognised!";

    private final Map<String, Properties> dictionary = new HashMap<String, Properties>(3);

    @Override
    public void start() throws Exception {

        loadWords();

        logger = container.getLogger();

        HttpServer server = vertx.createHttpServer();

        server.requestHandler(new Handler<HttpServerRequest>() {
            public void handle(HttpServerRequest request) {
                String output = null;
                HttpServerResponse response = request.response;

                if(request.path.equals("/translate")) {
                    Map<String,String> params = request.params();

                    String input = params.get("word");
                    String language = params.get("language");


                    logger.info(format("Service received a request for '%s' in '%s'", input, language));

                    Properties words = dictionary.get(language);

                    if(words != null) {
                        output = words.getProperty(input);
                    }
                    if(output == null) output = UNKNOWN_WORD;

                    response.statusCode = 200;
                    response.end(output);
                } else {
                    response.statusCode = 404;
                    response.statusMessage = "Unrecognised";
                    response.end("Not found!");
                }
            }
        });

        server.listen(8090, "localhost");

    }

    private void loadWords() throws Exception {
        dictionary.put("french", getWordsFromFile("words/french.properties"));
        dictionary.put("spanish", getWordsFromFile("words/spanish.properties"));
        dictionary.put("italian", getWordsFromFile("words/italian.properties"));
    }

    private Properties getWordsFromFile(String fileName) throws Exception{
        Properties words = new Properties();
        words.load(getClass().getResourceAsStream(fileName));
        return words;
    }
}
