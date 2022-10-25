

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

function ready(resultData) {
    var removeCartItemButtons = document.getElementsByClassName('remove-movie')
    for (let i = 0; i < removeCartItemButtons.length; i++) {
        var button = removeCartItemButtons[i];
        button.addEventListener('click', removeMovieFromCart);
    }

    var quantityInputs = document.getElementsByClassName('movie-quantity-input')
    for (var i = 0; i < quantityInputs.length; i++) {
        var input = quantityInputs[i];
        input.addEventListener('change', changeMovieQuantity);
    }

    addToCartClicked(resultData);
}

let title = getParameterByName("movieToBuy");
function addToCartClicked(resultData) {
    var price = "$20";

    for (let i = 0; i < resultData.length; i++) {
        let movieTitle = resultData[i]["movie_name"];
        let quantity = resultData[i]["movie_quantity"];

        if (movieTitle !== null) {
            addMovieToCart(movieTitle, price, quantity);
            updateCartTotal();
        }
    }
}

function removeMovieFromCart(event) {
    let removeMovie = event.target;
    // jQuery.ajax(
    //     "api/shopping-cart?movieToBuy=" + title, {
    //         method: "POST",
    //         // Serialize the login form to the data sent by POST request
    //         data: event.serialize(),
    //         success: (resultData) => ready(resultData),
    //         error: (resultData) => console.log(resultData)
    //     }
    // );
    removeMovie.parentElement.parentElement.remove();
    updateCartTotal();
}

function changeMovieQuantity(event) {
    let quantity = event.target;
    if (isNaN(quantity.value) || quantity.value <= 0) {
        quantity.value = 1;
    }

    // jQuery.ajax(
    //     "api/shopping-cart?movieToBuy=" + title + "&quantity=" + quantity, {
    //         method: "POST",
    //         // Serialize the login form to the data sent by POST request
    //         data: event.serialize(),
    //         success: (resultData) => ready(resultData),
    //         error: (resultData) => console.log(resultData)
    //     }
    // );
    updateCartTotal();
}

function updateCartTotal() {
    let movieContainer = document.getElementsByClassName('movie-items')[0];
    let movieRows = movieContainer.getElementsByClassName('movies-row');
    var movieTotal = 0;
    for (let i = 0; i < movieRows.length; i++) {
        let movieRow = movieRows[i];
        let moviePrice = movieRow.getElementsByClassName('movie-price')[0];
        let movieQuantity = movieRow.getElementsByClassName('movie-quantity-input')[0];
        let currPrice = parseInt(moviePrice.innerText.replace("$", ''));
        let currQuantity = movieQuantity.value;
        movieTotal = movieTotal + (currPrice * currQuantity);
    }
    document.getElementsByClassName('movie-total-price')[0].innerText = '$' + movieTotal;
}

function addMovieToCart(title, price, quantity) {
    let movieRow = document.createElement("div");
    movieRow.classList.add('movies-row')
    var movieItems = document.getElementsByClassName('movie-items')[0]
    var movieItemNames = movieItems.getElementsByClassName('movie-item-title')
    var movieRowContents = `
        <div class="movie-item movie-column">
            <span class="movie-item-title">${title}</span>
        </div>
        <span class="movie-price movie-column">${price}</span>
        <div class="movie-quantity movie-column">
            <input class="movie-quantity-input" type="number" value=${quantity}>
            <button class="btn remove-movie" type="button">REMOVE</button>
        </div>`
    movieRow.innerHTML = movieRowContents
    movieItems.append(movieRow)
    movieRow.getElementsByClassName('remove-movie')[0].addEventListener('click', removeMovieFromCart)
    movieRow.getElementsByClassName('movie-quantity-input')[0].addEventListener('change', changeMovieQuantity)
}

let url = "api/shopping-cart?movieToBuy=" + title;
jQuery.ajax({
    dataType: "json",
    method: "GET",
    url: url,
    success: (resultData) => ready(resultData),
    error: (resultData) => console.log(resultData)
});