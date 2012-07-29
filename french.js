load('vertx.js')

var eb = vertx.eventBus;
var logger = vertx.logger;
var client = vertx.createHttpClient()
    .setPort(8090)
    .setHost('localhost');

var msgHandler = function(message, replier) {

    var word = message['word'];
    logger.info("Received a request to translate '" + word + "' into French");

    client.getNow("http://localhost:8090/translate?word=" + word + "&language=french", function(resp) {

        if(resp.statusCode == 200) {
            resp.bodyHandler(function(body) {

                var result = body.toString();

                logger.info('Reply from API is: ' + result);
                replier(
                    {
                        translation: result,
                        language: "French"
                    }
                );

            });
        } else {
            logger.error("Error! Reply with status code: " + resp.statusCode);
            replier(
                {
                    translation: "Woops!",
                    language: "French"
                }
            );
        }

    });
}

eb.registerHandler('worker', msgHandler);
