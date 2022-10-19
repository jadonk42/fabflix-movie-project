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


function splitCsvStringToList(csv_string, limit){
    let csv_list = csv_string.split(",")
    let list_string = "<ol>"
    for(let i = 0; i < Math.min(csv_list.length, limit); ++i){
        list_string += "<li>" + csv_list[i] + "</li>"
    }
    list_string += "</ol>"
    return list_string;
}

function convertCSVIntoHyperlinks(csv_string, csv_Id_string){
    let csv_list = csv_string.split(",");
    let csvId_list = csv_Id_string.split(",");
    let result = "<ol>";

    for(let i=0; i < Math.min(csv_list.length, 3); ++i){
        result += "<li> <a href=\"single-star.html?id=" + csvId_list[i] + "\">";
        result += csv_list[i] +"</a></li>";
    }
    result += "</ol>"
    return result;
}

/**
 * Takes json data about movie and puts the data into the html element.
 * @param resultData jsonObject
 */
function populateHTMLWithMovieData(resultData) {
    let movieTableElement = jQuery("#movie_table_body");
    let htmlString = "";
    for(let i =0; i < Math.min(20, resultData.length); ++i){
        htmlString += "<tr>";
        htmlString +=
            "<td>" +
            "<a href=\"single-movie.html?id=" + resultData[i]["movie_id"] + "\">"
            + resultData[i]["movie_title"] +
            "</a>" +
            "</td>";
        htmlString += "<td>" + resultData[i]["movie_year"] + "</td>";
        htmlString += "<td>" + resultData[i]["movie_director"] + "</td>";
        htmlString += "<td>" + splitCsvStringToList(resultData[i]["movie_genres"], 3) +"</td>";
        htmlString += "<td>" + convertCSVIntoHyperlinks(resultData[i]["movie_stars"], resultData[i]["movie_star_ids"])+"</td>";
        htmlString += "<td>" + resultData[i]["movie_rating"] + "</td>";
        htmlString += "</tr>";
    }

    movieTableElement.append(htmlString);
}

let method = getParameterByName('method');
let sort = getParameterByName('sort');
if (sort == null){
    sort = "rating"
}

console.log('About to send GET request to MoviesServlet!');
//if no method, default is top movies of a certain kind
if (method == null) {
    jQuery.ajax({
        dataType: "json",
        method: "GET",
        url: "api/movies?sort=" + sort,
        success: (resultData) => populateHTMLWithMovieData(resultData),
        error: (resultData) => console.log(resultData)
    });
}
else if (method =="search") {
    let name = getParameterByName('method');
    let year = getParameterByName('year');
    let director = getParameterByName('director');
    let star = getParameterByName('star');

    let url = `api/movies?method=search&name={name}&year={year}&director={director}&star={star}&sort={sort}`;

    jQuery.ajax({
        dataType: "json",
        method: "GET",
        url: url,
        success: (resultData) => populateHTMLWithMovieData(resultData),
        error: (resultData) => console.log(resultData)
    });
}

