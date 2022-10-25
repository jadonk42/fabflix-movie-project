function populateHTMLWithMovieData(resultData) {
    let movieTableElement = jQuery("#confirmation_table_body");
    let htmlString = "";
    let totalPrice = 0;
    for (let i = 0; i < resultData.length - 1; i++) {
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

    let totalPriceTable = jQuery("#totalPrice");
    let totalPriceString = "Total Price: $" + totalPrice.toString();
    totalPriceTable.append(totalPriceString);

    let saleIdElement = jQuery("#saleId");
    let saleIdString = "SaleId: ";
    saleIdString = saleIdString + populateSales(resultData[resultData.length - 1]["saleId"]);
    saleIdElement.append(saleIdString);
}

function populateSales(salesIdCSV) {
    let sales_list = salesIdCSV.split(",");
    let result = "";

    for (let i = 0; i < sales_list.length; i++) {
        result += sales_list[i];
        if(i !== sales_list.length-1){
            result += ", ";
        }
    }
    return result;
}

let url = `api/movie-confirmation`;

jQuery.ajax({
    dataType: "json",
    method: "GET",
    url: url,
    success: (resultData) => populateHTMLWithMovieData(resultData),
    error: (resultData) => console.log(resultData)
});

jQuery.ajax(
    "api/shopping-cart", {
        method: "POST",
        // Serialize the login form to the data sent by POST request
        data: "action=clear",
        success: window.location.replace("order-confirmation.html"),
        error: (resultData) => console.log(resultData)
    }
);