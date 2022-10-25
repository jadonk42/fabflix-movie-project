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

function handleBackToMovies() {
    jQuery.ajax({
        dataType: "json",
        method: "GET",
        url: "api/backToMovies",
        success: (lastURL) => window.location.replace("movies.html?" + lastURL["lastQueryString"])
    });
}

let total = 0;
function populateShoppingCartPage(resultData) {
    console.log("received " + resultData.length + " items");
    let cartItems = jQuery("#movie-items");
    let htmlString = "";
    if(resultData.length == 0) {
        htmlString += "<tr>"
        htmlString += "<td class =\"movie-item\">No Movies in Cart</td>";
        htmlString += "</tr>"
        cartItems.append(htmlString);
        document.getElementsByClassName('movie-total-price')[0].innerText = '$' + total;
        return;
    }


    for(let i =0; i <resultData.length; ++i){
        let updateButtonID = "updateForm" + i;
        htmlString += "<tr>"
        htmlString += "<td class =\"movie-item\">" + resultData[i]['movie_name'] + "</td>";
        htmlString += "<td class =\"movie-price\">" + "20" + "</td>";
        htmlString += `<td><form id=\"${updateButtonID}\"><input className="movie-quantity-input" type="number" name="quantity" value=\"${resultData[i]['movie_quantity']}\"></form>`;
        htmlString += `<button onclick="updateQuantities(\'${ resultData[i]['movie_name']}\', \'${updateButtonID}\')"` + ') className="remove-movie" type="button">Update</button>';
        htmlString += `<button className="remove-movie" onclick="handleRemoveMovieFromCart('` + resultData[i]['movie_name']+ `')" type="button">Remove</button>` +"</td>";
        htmlString += "</tr>"
        total += resultData[i]['movie_quantity'] * 20;
    }

    cartItems.append(htmlString);
    document.getElementsByClassName('movie-total-price')[0].innerText = '$' + total;

}

function updateQuantities(movie, updateButtonID) {
    let queryString = jQuery("#" + updateButtonID).serialize();
    queryString += `&action=modifyQuantity&movie=${movie}`
    jQuery.ajax(
        "api/shopping-cart", {
            method: "POST",
            // Serialize the login form to the data sent by POST request
            data: queryString,
            success: window.location.replace("shopping-cart.html"),
            error: (resultData) => console.log(resultData)
        }
    );
}


function handleRemoveMovieFromCart(movieToRemove) {
    jQuery.ajax(
        "api/shopping-cart", {
            method: "POST",
            // Serialize the login form to the data sent by POST request
            data: `action=removeFromCart&movie=${movieToRemove}`,
            success: window.location.replace("shopping-cart.html"),
            error: (resultData) => console.log(resultData)
        }
    );
}


let url = "api/shopping-cart";
jQuery.ajax({
    dataType: "json",
    method: "GET",
    url: url,
    success: (resultData) => populateShoppingCartPage(resultData),
    error: (resultData) => console.log(resultData)
});