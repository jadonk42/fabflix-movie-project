function convertGenreToHyperlink(genres) {
    return "<a href=\"browse-movies.html?genre=" + genres + "character&=null" +  "\">" + "</a>"
}

function convertCharacterToHyperlink(character) {
    return "<a href=\"browse-movies.html?genre=null&character=" + character + "\">" + "</a>"
}

function populateHTMLWithGenreData(resultData) {
    console.log(resultData);
    let genreList = jQuery("#genre-list");
    let htmlString = "";
    htmlString += "<h3>Movie Genres: </h3>";
    for (let i = 0; i < resultData.length; ++i) {
        htmlString += "<p>" + convertGenreToHyperlink(resultData[i]["movie_genres"]) + "</p>";
    }
    genreList.append(htmlString);
}

function populateHTMLWithCharacterData(resultData) {
    console.log(resultData);
    let characterList = jQuery("#character-list");
    let htmlString = "";
    htmlString += "<h3>First Characters: </h3>";
    for (let i = 0; i < resultData.length; ++i) {
        htmlString += "<p>" + convertCharacterToHyperlink(resultData[i]["movie_characters"]) + "</p>";
    }
    characterList.append(htmlString);
}

jQuery.ajax({
    dataType: "json",
    method: "GET",
    url: "api/browse?method=getMovieGenres",
    success: (resultData) => populateHTMLWithGenreData(resultData),
    error: (resultData) => console.log(resultData)
});

jQuery.ajax({
    dataType: "json",
    method: "GET",
    url: "api/browse?method=getMovieCharacters",
    success: (resultData) => populateHTMLWithCharacterData(resultData),
    error: (resultData) => console.log(resultData)
});