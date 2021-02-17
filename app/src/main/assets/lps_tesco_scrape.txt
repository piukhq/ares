var queryString = ".pointvalue"

performPointsScraping()

function performPointsScraping() {
    var p = document.querySelector(queryString)

    if (!p) {
        return {
            "success": false,
            "error_message": "Element for query string not found."
        }
    } else {
        return {
            "success": true,
            "points": p.innerHTML
        }
    }
}
