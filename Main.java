import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.deploy.Verticle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static java.lang.String.format;

/**
 * Main verticle to serve files and load Italian worker verticle
 */
public class Main extends Verticle {

    private Logger logger;
    private List<String> words = new ArrayList<String>();

    @Override
    public void start() throws Exception {
        logger = container.getLogger();

        // start web server on port 8080 to server up website
        startWebServer();

        // setup event bus handlers for messages from the browser
        // and messages back from the worker verticles
        setupEventBusHandlers();

        startTimer();

        // deploy our French verticle
        container.deployVerticle("french.js");
    }

    private void startWebServer() {
        JsonObject config = new JsonObject();

        config.putString("web_root", "web");
        config.putString("index_page", "index.html");
        config.putString("host", "localhost");
        config.putNumber("port", 8080);

        // create a SOCK JS bridge as well
        config.putBoolean("bridge", true);
        config.putArray("inbound_permitted", createAddressPermission("translator"));
        config.putArray("outbound_permitted", createAddressPermission("translations"));

        container.deployModule("vertx.web-server-v1.0", config, 1);
    }

    private void startTimer() {
        vertx.setPeriodic(1000, new Handler<Long>() {
            @Override
            public void handle(Long aLong) {
                if (!words.isEmpty()) {

                    final String word = words.get(0).trim();
                    logger.info(format("Requesting translation for %s from a worker", word));

                    // forward it on to a worker to translate it
                    Handler<Message<JsonObject>> handler = new Handler<Message<JsonObject>>() {
                        @Override
                        public void handle(Message<JsonObject> response) {
                            handleTranslationResponse(response.body, word);
                        }
                    };

                    JsonObject msg = new JsonObject();
                    msg.putString("word", word);
                    vertx.eventBus().send("worker", msg, handler);

                    // remove from list
                    words.remove(0);
                }
            }
        });
    }

    private JsonArray createAddressPermission(String addr) {
        JsonObject obj = new JsonObject();
        obj.putString("address", addr);
        return new JsonArray(new Object[] {obj});
    }

    private void setupEventBusHandlers() {
        EventBus eventBus = vertx.eventBus();

        // register to receive messages sent by the browser to the "translator" address
        eventBus.registerHandler("translator",new Handler<Message<JsonObject>>() {
            @Override
            public void handle(Message<JsonObject> message) {
                handleTranslationRequest(message);
            }
        });
    }

    private void handleTranslationRequest(final Message<JsonObject> message) {

        JsonObject body = message.body;
        String sWords = body.getString("words");
        words.addAll(Arrays.asList(sWords.split("\\s")));
        logger.info(format("Now have %s words to translate", words.size()));

    }

    private void handleTranslationResponse(JsonObject workerResponse, String word) {

        JsonObject reply = new JsonObject();

        reply.putString("word", word);
        reply.putString("language", workerResponse.getString("language"));
        reply.putString("translation", workerResponse.getString("translation"));

        vertx.eventBus().publish("translations", reply);
    }
}
