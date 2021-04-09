var queryString = ".quantity"

performPointsScraping()

function performPointsScraping() {
    var p = document.querySelector(queryString)

    if (!p) {
        return {
            "success": false,
            "error_message": "Element for query string not found."
        }
    } else {
        var num = p.innerHTML.match(/\d+/);
        return {
            "success": true,
            "points": num[0]
        }
    }
}
