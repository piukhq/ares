var username = "%@"
var password = "%@"

var formQuery = "form#loginForm"
var usernameInputQuery = "form#login-form input[name=j_username]"
var passwordInputQuery = "form#login-form input[type=password]"
var submitButtonQuery = "form#login-form button[type=submit]"

var pointsValueQuery = ".sd-panel p:last-of-type b"

var incorrectCredentialsQuery = "div[class*=\"information_message negative\""

handleNavigation()

function handleNavigation() {
    // If we can identify the points value, scrape

    var pts = document.querySelector(pointsValueQuery)
    if (pts && pts.innerHTML !== "") {
        return {
            "points": pts.innerHTML
        }
    }


    // If we can't identify a points value, can we identify one of the following:
    // Recaptcha, incorrect credentials message

    var error = document.querySelector(incorrectCredentialsQuery)
    if (error && error.innerText !== "") {
        return {
            "did_attempt_login": true,
            "error_message": "Login failed. Incorrect credentials identified: " + error.innerText
        }
    }

    // TODO: Recaptcha


    // If we can identify the login form, login

    var f = document.querySelector(formQuery)
    if (f) {
        // Override the form's id to enable easier querying
        f.id = "login-form"

        var u = document.querySelector(usernameInputQuery)
        if (!u) {
            return {
                "error_message": "Login failed. Email/username input field could not be identified."
            }
        }
        u.value = username

        var p = document.querySelector(passwordInputQuery)
        if (!p) {
            return {
                "error_message": "Login failed. Password input field could not be identified."
            }
        }
        p.value = password

        var b = document.querySelector(submitButtonQuery)
        if (!b) {
            return {
                "error_message": "Login failed. Submit button could not be identified."
            }
        }
        b.click()

        return {
            "did_attempt_login": true
        }
    }

    // If we cannot identify a login form, points value, recaptcha or incorrect credentials
    // We should assume the client is redirecting, and the idle timer should handle moving to retry state
    return {}
}
