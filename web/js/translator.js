var eb = null;

function setup() {
    $('#translateButton').click(doTranslation);
    $('#populateButton').click(doPopulate);
    $('#clearButton').click(doClear);

    eb = new vertx.EventBus('http://localhost:8080/eventbus');

    eb.onopen = function() {
        console.log("Connected!");
        eb.registerHandler('translations', msgHandler);
    }

}

function msgHandler(reply) {

    var msg = "<b>" + reply['word'] + "</b> is <b>" + reply['translation'] + "</b> in " + reply['language'];

    console.log("Message: " + msg);

    $('#translations').prepend("<div class='translation " + reply['language'] + "'>" + msg + "</div>");

}

function doTranslation() {

    // grab the word and send it off for translation
    var inputWords = $('#input').val();
    eb.send('translator', { words: inputWords });

    // clear input for next time
    $('#input').val('');

    return false;
}

function doPopulate() {
    var words = [
        'cat',
        'cow',
        'dog',
        'duck',
        'family',
        'house',
        'rabbit',
        'sheep'
    ];

    $('#input').val(words.join('\n'));

    return false;
}

function doClear() {

    $('#translations').empty();
    return false;
}

$(document).ready(setup);