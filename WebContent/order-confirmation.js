function populateHTMLWithMovieData(resultData) {
    let movieTableElement = jQuery("#confirmation_table_body");
    let htmlString = "";
    let totalPrice = 0;
    for (let i = 0; i < resultData.length; i++) {
        htmlString += "<tr>";

        let movieTitle = resultData[i]["movie_name"];
        let quantity = resultData[i]["movie_quantity"];

        totalPrice = totalPrice + (20 * parseInt(quantity));

        htmlString += "<td>" + movieTitle + "</td>";
        htmlString += "<td> $20 </td>";
        htmlString += "<td>" + quantity + "</td>";
        htmlString += "</tr>";
    }
    movieTableElement.append(htmlString);

    let totalPriceTable = jQuery("#totalPrice")
    let totalPriceString = "Total Price: $" + totalPrice.toString();
    totalPriceTable.append(totalPriceString);
}

let url = `api/movie-confirmation`;

jQuery.ajax({
    dataType: "json",
    method: "GET",
    url: url,
    success: (resultData) => populateHTMLWithMovieData(resultData),
    error: (resultData) => console.log(resultData)
});