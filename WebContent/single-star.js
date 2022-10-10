/**
 * Retrieve parameter from request URL, matching by parameter name
 * @param target String
 * @returns {*}
 */
function getParameterByName(target) {
    // Get request URL
    let url = window.location.href;
    // Encode target parameter name to url encoding
    target = target.replace(/[\[\]]/g, "\\$&");

    // Ues regular expression to find matched parameter value
    let regex = new RegExp("[?&]" + target + "(=([^&#]*)|&|#|$)"),
        results = regex.exec(url);
    if (!results) return null;
    if (!results[2]) return '';

    // Return the decoded parameter value
    return decodeURIComponent(results[2].replace(/\+/g, " "));
}

function convertCSVIntoHyperlinks(csv_string, csv_Id_string){
    let csv_list = csv_string.split(",");
    let csvId_list = csv_Id_string.split(",");
    let result = "";

    for(let i=0; i < csv_list.length; ++i){
        result += "<a href=\"single-movie.html?id=" + csvId_list[i] + "\">";
        result += csv_list[i] +"</a>";
        if(i !== csv_list.length-1){
            result += ", ";
        }
    }
    return result;
}

function populateHTMLWithSingleStarData(resultData){
    console.log(resultData);
    let starInformationList = jQuery("#single-star-info-list");
    let htmlString = "";

    htmlString += "<h3>" + resultData["star_name"] + "</h3>";
    if(resultData["star_dob"] != null){
        htmlString += "<p>Born in " + resultData["star_dob"] + "</p>";
    }
    else{
        htmlString += "Birth Year: N/A"
    }
    htmlString += "<p>Stars in " + convertCSVIntoHyperlinks(resultData["movie_titles"], resultData["movie_ids"]) + "</p>";

    starInformationList.append(htmlString);
}

console.log('About to send GET request to SingleStarServlet');

let starId = getParameterByName('id');

jQuery.ajax({
    dataType: "json",
    method: "GET",
    url: "api/single-star?id=" + starId,
    success: (resultData) => populateHTMLWithSingleStarData(resultData),
    error: (resultData) => console.log(resultData)
});