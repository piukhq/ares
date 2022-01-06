var username = "%@"
var password = "%@"

var formQuery = "form"
var usernameInputQuery = "form#login-form input[type=email]"
var passwordInputQuery = "form#login-form input[type=password]"
var submitButtonQuery = "form#login-form button[type=submit]"
var pointsValueQuery = "#pointSummary [class*=Points] [class*=Points]"
var incorrectCredentialsQuery = "p.ui-component__notice__error-text"

handleNavigation()

function handleNavigation() {
    // If we can identify the points value, scrape

    var pts = document.querySelector(pointsValueQuery)

    if (pts) {
        var num = pts.innerHTML.match(/\d+/);
        return {
            "points": num[0]
        }
    }

    // If we can't identify a points value, can we identify incorrect credentials message

    var error = document.querySelector(incorrectCredentialsQuery)
    if (error && error.innerHTML !== "") {
        return {
            "did_attempt_login": true,
            "error_message": "Login failed. Incorrect credentials identified: " + error.innerHTML
        }
    }

    // If we can identify the login form, login

    var f = document.querySelector(formQuery)
    if (f) {
        // Override the form's id to enable easier querying
        f.id = "login-form"

        var u = document.querySelector(usernameInputQuery)
        var p = document.querySelector(passwordInputQuery)
        var b = document.querySelector(submitButtonQuery)

        if (u && p && b) {
            var nativeInputValueSetter = Object.getOwnPropertyDescriptor(window.HTMLInputElement.prototype, "value").set
            nativeInputValueSetter.call(u, username)
            nativeInputValueSetter.call(p, password)

            var valueSetterEvent = new Event('input', { bubbles: true })
            u.dispatchEvent(valueSetterEvent)
            p.dispatchEvent(valueSetterEvent)
            b.click()

            return {
                "did_attempt_login": true
            }
        }
    }

    // If we cannot identify a login form, points value, recaptcha or incorrect credentials
    // We should assume the client is redirecting, and the idle timer should handle moving to retry state
    return {}
}
