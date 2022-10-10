function splitCsvStringToList(csv_string, limit){
    let csv_list = csv_string.split(",")
    let list_string = "<ol>"
    for(let i = 0; i < Math.min(csv_list.length, limit); ++i){
        list_string += "<li>" + csv_list[i] + "</li>"
    }
    result += "</ol>"
    return list_string;
}

function convertCSVIntoHyperlinks(csv_string, csv_Id_string){
    let csv_list = csv_string.split(",");
    let csvId_list = csv_Id_string.split(",");
    let result = "<ol>";

    for(let i=0; i < 3; ++i){
        result += "<li> <a href=\"single-star.html?id=" + csvId_list[i] + "\">";
        result += csv_list[i] +"</a></li>";
        if(i !== csv_list.length-1){
            result += ", ";
        }
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

console.log('About to send GET request to MoviesServlet');

jQuery.ajax({
    dataType: "json",
    method: "GET",
    url: "api/movies",
    success: (resultData) => populateHTMLWithMovieData(resultData),
    error: (resultData) => console.log(resultData)
});