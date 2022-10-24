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


function resetFromQueryString(queryString) {
    let limitRegExp = /&limit=[0-9]+/;
    queryString = queryString.replace("&sortBy=ratingAsc", "");
    queryString = queryString.replace("&sortBy=ratingDesc", "");
    queryString = queryString.replace("&sortBy=alphaAsc", "");
    queryString = queryString.replace("&sortBy=alphaDesc", "");
    queryString = queryString.replace(limitRegExp, "");

    return queryString;
}

/**
 * how the button to handle sort and pagination will work
 */
let sort_form = jQuery("#sort_form");
function handleSort() {
    //need to remove because otherwise, the sortBy param will stay and never be updated
    let queryString = resetFromQueryString(window.location.search);
    window.location.replace("movies.html" + queryString + "&" +sort_form.serialize());
}
sort_form.submit(handleSort);


function splitCsvStringToList(csv_string, limit){
    let csv_list = csv_string.split(",")
    let list_string = "<ol>"
    for(let i = 0; i < Math.min(csv_list.length, limit); ++i){
        list_string += "<li  class=\"redirect-link\"> <a href=\"movies.html?method=browse&genre=" + csv_list[i] +  "&character=null&sortBy=ratingDesc&limit=10&page=1" +  "\">";
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
        result += "<li  class=\"redirect-link\"> <a href=\"single-star.html?id=" + csvId_list[i] + "\">";
        result += csv_list[i] +"</a></li>";
    }
    result += "</ol>"
    return result;
}

/**
 * Takes json data about movie and puts the data into the html element.
 * @param resultData jsonObject
 */
function populateHTMLWithMovieData(resultData, limit) {
    console.log("received " + resultData.length + " items");
    let movieTableElement = jQuery("#movie_table_body");
    let htmlString = "";
    for(let i =0; i < Math.min(limit, resultData.length); ++i){
        htmlString += "<tr>";
        htmlString +=
            "<td class=\"redirect-link\">" +
            "<a href=\"single-movie.html?id=" + resultData[i]["movie_id"] + "\">"
            + resultData[i]["movie_title"] +
            "</a>" +
            "</td>";
        htmlString += "<td>" + resultData[i]["movie_year"] + "</td>";
        htmlString += "<td>" + resultData[i]["movie_director"] + "</td>";
        htmlString += "<td>" + splitCsvStringToList(resultData[i]["movie_genres"], 3) +"</td>";
        htmlString += "<td>" + convertCSVIntoHyperlinks(resultData[i]["movie_stars"], resultData[i]["movie_star_ids"])+"</td>";
        htmlString += "<td>" + resultData[i]["movie_rating"] + "</td>";
        htmlString += "<td>" + "$20" + "<br><br><form action='shopping-cart.html'> <input type='submit' value='Add Movie' class='AddMovie'> </form>"
            + "</td>";
        htmlString += "</tr>";
    }

    movieTableElement.append(htmlString);
}

let method = getParameterByName('method');
let sortBy = getParameterByName('sortBy');
let limit = getParameterByName('limit');
let page = getParameterByName('page');
if (sortBy == null){
    sortBy = "ratingDesc";
}
if (limit == null){
    limit = "10";
}
if (page == null){
    page = "1";
}
//changes default selections
jQuery("#sortBy").val(sortBy);
jQuery("#limit").val(limit);
jQuery(".current-page-span").append(" Page " + page + " ");

/**
 * next and previous buttons
 */
function handlePrev() {
    let pageRegExp = /&page=[0-9]+/;
    let queryString = window.location.search;
    if (page != 1) {
        queryString = queryString.replace(pageRegExp, "&page=" + (parseInt(page)-1));
    }
    window.location.replace(queryString);
}


function handleNext() {
    let pageRegExp = /&page=[0-9]+/;
    let queryString = window.location.search;
    queryString = queryString.replace(pageRegExp, "&page=" + (parseInt(page)+1));
    window.location.replace(queryString);
}

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

    let url = `api/movies/search?method=${method}&name=${name}&year=${year}&director=${director}&star=${star}&sortBy=${sortBy}&limit=${limit}&page=${page}`;

    console.log('About to send GET request to SearchMoviesServlet!');
    jQuery.ajax({
        dataType: "json",
        method: "GET",
        url: url,
        success: (resultData) => populateHTMLWithMovieData(resultData, limit),
        error: (resultData) => console.log(resultData)
    });
}
else if (method === "browse") {
    let genre = getParameterByName('genre');
    let character = getParameterByName('character');

    let url = `api/movies/browse?method=${method}&genre=${genre}&character=${character}&sortBy=${sortBy}&limit=${limit}&page=${page}`;

    console.log('About to send GET request to BrowseMoviesServlet!');
    jQuery.ajax({
        dataType: "json",
        method: "GET",
        url: url,
        success: (resultData) => populateHTMLWithMovieData(resultData, limit),
        error: (resultData) => console.log(resultData)
    });
}

