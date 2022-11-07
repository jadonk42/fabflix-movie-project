let movie_form = jQuery("#movie_form");
let star_form = jQuery("#star_form");


let tables = ["movies", "stars", "genres", "ratings",
    "stars_in_movies", "genres_in_movies", "customers", "creditcards",
    "sales", "employees"];


/**
 * Takes json data about movie and puts the data into the html element.
 * @param resultData jsonObject
 */
function populateHTMLWithMetaData(resultData, limit) {
    console.log("received " + resultData.length + " items");
    console.log(resultData);
    let metadataElement = jQuery("#metadataDiv");
    let htmlString = "";
    for (let i =0; i < resultData.length; ++i) {
        htmlString += "<p>" + tables[i] + "</p>"

        htmlString += "<table>";
        htmlString += "<tr><th>FIELD</th><th>TYPE</th><th>NULL</th><th>KEY</th><th>DEFAULT</th><th>EXTRA</th></tr>"

        let fieldsArray = resultData[i][tables[i]];
        for (let j = 0; j < fieldsArray.length; ++j) {
            htmlString += "<tr>";
            htmlString += "<td>" + fieldsArray[j]["field"] +  "</td>";
            htmlString += "<td>" + fieldsArray[j]["type"] +  "</td>";
            htmlString += "<td>" + fieldsArray[j]["nullOrNot"] +  "</td>";
            htmlString += "<td>" + fieldsArray[j]["key"] +  "</td>";
            htmlString += "<td>" + fieldsArray[j]["defaultVal"] +  "</td>";
            htmlString += "<td>" + fieldsArray[j]["extra"] +  "</td>";
            htmlString += "</tr>"
        }
        htmlString += "</table>"

    }

    metadataElement.append(htmlString);
}

function handleMovieResult(resultData) {
    console.log("handle movie response()");
    console.log(resultData);
    jQuery("#movieResponseMessages").text(resultData["message"]);
}

function handleStarResult(resultData) {
    console.log("handle star response()");
    console.log(resultData);
    jQuery("#starResponseMessages").text(resultData["message"]);
}


/**
 * Submit the form content with POST method
 * @param formSubmitEvent
 */
function submitMovieForm(formSubmitEvent) {
    console.log("submit star form");
    /**
     * When users click the submit button, the browser will not direct
     * users to the url defined in HTML form. Instead, it will call this
     * event handler when the event is triggered.
     */
    formSubmitEvent.preventDefault();

    jQuery.ajax(
        "api/add-movie", {
            method: "POST",
            // Serialize the login form to the data sent by POST request
            data: movie_form.serialize(),
            success: handleMovieResult,
            error: (resultData) => console.log(resultData)
        }
    );
}

// Bind the submit action of the form to a handler function
movie_form.submit(submitMovieForm);


/**
 * Submit the form content with POST method
 * @param formSubmitEvent
 */
function submitStarForm(formSubmitEvent) {
    console.log("submit star form");
    /**
     * When users click the submit button, the browser will not direct
     * users to the url defined in HTML form. Instead, it will call this
     * event handler when the event is triggered.
     */
    formSubmitEvent.preventDefault();

    jQuery.ajax(
        "api/add-star", {
            method: "POST",
            // Serialize the login form to the data sent by POST request
            data: star_form.serialize(),
            success: handleStarResult,
            error: (resultData) => console.log(resultData)
        }
    );
}

// Bind the submit action of the form to a handler function
star_form.submit(submitStarForm);


jQuery.ajax({
    dataType: "json",
    method: "GET",
    url: "api/metadata",
    success: (resultData) => populateHTMLWithMetaData(resultData),
    error: (resultData) => console.log(resultData)
});
