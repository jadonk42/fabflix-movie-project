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


function removeSortByFromQueryString(queryString) {
    queryString = queryString.replace("&sortBy=ratingAsc", "");
    queryString = queryString.replace("&sortBy=ratingDesc", "");
    queryString = queryString.replace("&sortBy=alphaAsc", "");
    queryString = queryString.replace("&sortBy=alphaDesc", "");
    return queryString;
}

let sort_form = jQuery("#sort_form");
function handleSort() {
    //need to remove because otherwise, the sortBy param will stay and never be updated
    let queryString = removeSortByFromQueryString(window.location.search);
    window.location.replace("movies.html" + queryString + "&" +sort_form.serialize());
}
sort_form.submit(handleSort);


function splitCsvStringToList(csv_string, limit){
    let csv_list = csv_string.split(",")
    let list_string = "<ol>"
    for(let i = 0; i < Math.min(csv_list.length, limit); ++i){
        list_string += "<li> <a href=\"movies.html?method=browse&genre=" + csv_list[i] +  "&character=null" +  "\">";
        list_string += csv_list[i] + "</a></li>"
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
let sortBy = getParameterByName('sortBy');
if (sortBy == null){
    sortBy = "ratingDesc";
}

console.log('About to send GET request to MoviesServlet!');
//if no method, default is top movies of a certain kind
if (method == null) {
    jQuery.ajax({
        dataType: "json",
        method: "GET",
        url: "api/movies?sortBy=" + sortBy,
        success: (resultData) => populateHTMLWithMovieData(resultData),
        error: (resultData) => console.log(resultData)
    });
}
else if (method =="search") {
    let name = getParameterByName('name');
    let year = getParameterByName('year');
    let director = getParameterByName('director');
    let star = getParameterByName('star');

    let url = `api/movies/search?name=${name}&year=${year}&director=${director}&star=${star}&sortBy=${sortBy}`;

    jQuery.ajax({
        dataType: "json",
        method: "GET",
        url: url,
        success: (resultData) => populateHTMLWithMovieData(resultData),
        error: (resultData) => console.log(resultData)
    });
}
else if (method === "browse") {
    let genre = getParameterByName('genre');
    let character = getParameterByName('character');

    let url = `api/movies/browse?genre=${genre}&character=${character}&sortBy=${sortBy}`;

    jQuery.ajax({
        dataType: "json",
        method: "GET",
        url: url,
        success: (resultData) => populateHTMLWithMovieData(resultData),
        error: (resultData) => console.log(resultData)
    });
}

