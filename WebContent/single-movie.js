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

function convertStarsIntoHyperlinks(csv_stars_string, csv_starId_string){
    let csv_star_list = csv_stars_string.split(",");
    let csv_starId_list = csv_starId_string.split(",");
    let result = "";

    for(let i=0; i < csv_star_list.length; ++i){
        result += "<a href=\"single-star.html?id=" + csv_starId_list[i] + "\">";
        result += csv_star_list[i] +"</a>";
        if(i !== csv_star_list.length-1){
            result += ", ";
        }
    }
    return result;
}

/**
 * Takes json data about movie and puts the data into the html element.
 * @param resultData jsonObject
 */
function populateHTMLWithSingleMovieData(resultData) {
    console.log(resultData);
    let movieInformationList = jQuery("#single-movie-info-list");
    let htmlString = "";
    htmlString += "<h3>" + resultData["movie_title"] + "</h3>";
    htmlString += "<p>" + resultData["movie_year"] + "</p>";
    htmlString += "<p>Directed by " + resultData["movie_director"] + "</p>";
    htmlString += "<p>Genres: " + resultData["movie_genres"].replace(",", ", ") + "</p>";
    htmlString += "<p>Starring: " + convertStarsIntoHyperlinks(resultData["movie_stars"], resultData["movie_star_ids"])+ "</p>";
    htmlString += "<p>" + resultData["movie_rating"] + "</p>";

    movieInformationList.append(htmlString);
}

console.log('About to send GET request to SingleMovieServlet');

let movieId = getParameterByName('id');

jQuery.ajax({
    dataType: "json",
    method: "GET",
    url: "api/single-movie?id=" + movieId,
    success: (resultData) => populateHTMLWithSingleMovieData(resultData),
    error: (resultData) => console.log(resultData)
});