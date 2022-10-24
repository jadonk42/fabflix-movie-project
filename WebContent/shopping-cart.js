
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', ready);
} else {
    ready();
}

function ready() {
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

    addToCartClicked();
}

function addToCartClicked() {
    var title = "Test Movie";
    var price = "20";
    addMovieToCart(title, price);
    updateCartTotal();

    var newMovie = "Get Movies";
    addMovieToCart(newMovie, price);
    updateCartTotal();

    addMovieToCart(title, price);
    updateCartTotal();
}

function removeMovieFromCart(event) {
    let removeMovie = event.target;
    removeMovie.parentElement.parentElement.remove();
    updateCartTotal();
}

function changeMovieQuantity(event) {
    let quantity = event.target;
    if (isNaN(quantity.value) || quantity.value <= 0) {
        quantity.value = 1;
    }
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

function addMovieToCart(title, price) {
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
            <input class="movie-quantity-input" type="number" value="1">
            <button class="btn remove-movie" type="button">REMOVE</button>
        </div>`
    movieRow.innerHTML = movieRowContents
    movieItems.append(movieRow)
    movieRow.getElementsByClassName('remove-movie')[0].addEventListener('click', removeMovieFromCart)
    movieRow.getElementsByClassName('movie-quantity-input')[0].addEventListener('change', changeMovieQuantity)
}