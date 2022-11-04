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



jQuery.ajax({
    dataType: "json",
    method: "GET",
    url: "api/metadata",
    success: (resultData) => populateHTMLWithMetaData(resultData),
    error: (resultData) => console.log(resultData)
});
