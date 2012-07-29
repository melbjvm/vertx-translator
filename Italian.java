import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.http.HttpClient;
import org.vertx.java.core.http.HttpClientResponse;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.deploy.Verticle;

public class Italian extends Verticle {
    Logger logger;
    HttpClient client;

    @Override
    public void start() throws Exception {

        logger = container.getLogger();
        client = vertx.createHttpClient()
            .setHost("localhost")
            .setPort(8090);

        vertx.eventBus().registerHandler("worker", new Handler<Message<JsonObject>>() {

            @Override
            public void handle(final Message<JsonObject> message) {

                String word = message.body.getString("word");
                logger.info("Received a request to translate '%s' into Italian");

                String url = "http://localhost:8090/translate?word=" + word + "&language=italian";

                client.getNow(url, new Handler<HttpClientResponse>() {

                    @Override
                    public void handle(HttpClientResponse resp) {

                        if(resp.statusCode == 200) {
                            resp.bodyHandler(new Handler<Buffer>() {
                                @Override
                                public void handle(Buffer buffer) {
                                    message.reply(createReply(buffer.toString()));
                                }
                            });

                        } else {
                            message.reply(createReply("Woops!"));
                        }

                    }

                });

            }

        });
    }

    private JsonObject createReply(String translation) {
        JsonObject reply = new JsonObject();
        reply.putString("translation", translation);
        reply.putString("language", "Italian");
        return reply;
    }
}
