function populateHTMLWithMovieData(resultData) {
    let movieTableElement = jQuery("#confirmation_table_body");
    let htmlString = "";
    for (let i = 0; i < resultData.length; i++) {
        console.log(resultData[i]);
        htmlString += "<tr>";

        htmlString += "<td>" + resultData[i]["movie_name"] + "</td>";
        htmlString += "<td>" + resultData[i]["movie_price"] + "</td>";
        htmlString += "<td>" + resultData[i]["movie_quantity"] + "</td>";
        htmlString += "</tr>";
    }
    movieTableElement.append(htmlString);
}

let url = `api/movie-confirmation`;

jQuery.ajax({
    dataType: "json",
    method: "GET",
    url: url,
    success: (resultData) => populateHTMLWithMovieData(resultData),
    error: (resultData) => console.log(resultData)
});