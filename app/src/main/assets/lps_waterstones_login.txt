// Value injection

var username = "%@"
var password = "%@"

// ** QUERY STRINGS - DELETE AS NECESSARY IF MERCHANT DOES NOT REQUIRE **

// Form selector queries

var formQuery = "form#loginForm"
var usernameInputQuery = "form#login-form input[type=email]"
var passwordInputQuery = "form#login-form input[type=password]"
var submitButtonQuery = "form#login-form button[type=submit]"


performLogin()


function performLogin() {


    // ** FORM **


    var f = document.querySelector(formQuery)

    if (!f) {
        return {
            "success": false,
            "error_message": "Failed to identify form."
        }
    }

    // Override the form's id to enable easier querying
    f.id = "login-form"


    // ** USERNAME **


    var u = document.querySelector(usernameInputQuery)

    if (!u) {
        return {
            "success": false,
            "error_message": "Failed to identify email/username input field."
        }
    }

    u.value = username

    
    // ** PASSWORD **


    var p = document.querySelector(passwordInputQuery)

    if (!p) {
        return {
            "success": false,
            "error_message": "Failed to identify password input field."
        }
    }

    p.value = password


    // ** SUBMIT **


    var b = document.querySelector(submitButtonQuery)

    if (!b) {
        return {
            "success": false,
            "error_message": "Failed to identify submit button."
        }
    }

    b.click()
    
    return {
        "success": true
    }
}