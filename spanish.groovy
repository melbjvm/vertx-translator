import org.vertx.groovy.core.http.HttpClient

def eb = vertx.eventBus
def logger = container.logger
HttpClient client = vertx.createHttpClient(port: 8090, host: "localhost")

eb.registerHandler("worker") { message ->

    def word = message.body.word
    logger.info "Received a request to translate '${ word }' into Spanish"

    client.getNow("http://localhost:8090/translate?word=" + word + "&language=spanish") { resp ->

        if(resp.statusCode == 200) {
            resp.bodyHandler { body ->
                def result = body.toString()
                logger.info("Reply from API is: ${result}");

                def reply = [
                    translation: result,
                    language: "Spanish"
                ]
                message.reply reply
            }
        } else {
            logger.error "Error! Reply with status code: ${resp.statusCode}";

            def reply = [
                translation:  "Woops!",
                language:  "Spanish"
            ]
            message.reply reply
        }
    }

}