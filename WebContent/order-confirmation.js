function populateHTMLWithMovieData(resultData) {
    let movieTableElement = jQuery("#confirmation_table_body");
    let htmlString = "";
    for (let i = 0; i < resultData.length; i++) {
        htmlString += "<tr>";

        let movieTitle = resultData[i]["movie_name"];
        let quantity = resultData[i]["movie_quantity"];

        htmlString += "<td>" + "Test" + "</td>";
        htmlString += "<td> $20 </td>";
        htmlString += "<td>" + "Test" + "</td>";
        htmlString += "</tr>";
    }
    movieTableElement.append(htmlString);
}

populateHTMLWithMovieData([1, 2, 3]);

// let url = `api/movie-confirmation`;
//
// jQuery.ajax({
//     dataType: "json",
//     method: "GET",
//     url: url,
//     success: (resultData) => populateHTMLWithMovieData(resultData),
//     error: (resultData) => console.log(resultData)
// });