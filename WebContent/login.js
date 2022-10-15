let login_form = jQuery("#login_form");

function handleLoginResult(resultData) {
    console.log("handle login response");
    console.log(resultData);
    console.log(resultData["status"]);

    // If login succeeds, it will redirect the user to index.html
    if (resultData["status"] == "success") {
        window.location.replace("index.html");
    } else {
        // If login fails, the web page will display
        // error messages on <div> with id "login_error_message"
        console.log("show error message:");
        console.log(resultData["message"]);
        jQuery("#login_error_message").text(resultData["message"]);
    }
}
/**
 * Submit the form content with POST method
 * @param formSubmitEvent
 */
function submitLoginForm(formSubmitEvent) {
    console.log("submit login form");
    /**
     * When users click the submit button, the browser will not direct
     * users to the url defined in HTML form. Instead, it will call this
     * event handler when the event is triggered.
     */
    formSubmitEvent.preventDefault();

    jQuery.ajax(
        "api/login", {
            method: "POST",
            // Serialize the login form to the data sent by POST request
            data: login_form.serialize(),
            success: handleLoginResult,
            error: (resultData) => console.log(resultData)
        }
    );
}

// Bind the submit action of the form to a handler function
login_form.submit(submitLoginForm);