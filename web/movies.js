function splitCsvStringToList(csv_string, limit){
    let csv_list = csv_string.split(",")
    let list_string = "<ol>"
    for(let i = 0; i < Math.min(csv_list.length, limit); ++i){
        list_string += "<li>" + csv_list[i] + "</li>"
    }

    return list_string;
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
        htmlString += "<td>" + splitCsvStringToList(resultData[i]["movie_stars"], 3)+"</td>";
        htmlString += "<td>" + resultData[i]["movie_rating"] + "</td>";
        htmlString += "</tr>";
    }

    movieTableElement.append(htmlString);
}

console.log('About to send GET request to MoviesServlet');

jQuery.ajax({
    dataType: "json",
    method: "GET",
    url: "api/movies",
    success: (resultData) => populateHTMLWithMovieData(resultData),
    error: (resultData) => console.log(resultData)
});