function convertGenreToHyperlink(genre) {
    return "<a href=\"movies.html?method=browse&genre=" + genre + "&character=null&limit=10&page=1&sortBy=ratingDesc" +  "\">" + genre + "</a>";
}

function convertCharacterToHyperlink(character) {
    return "<a href=\"movies.html?method=browse&genre=null&character=" + character + "&limit=10&page=1&sortBy=ratingDesc\">" + character + "</a>";
}

function populateHTMLWithGenreData(resultData) {
    console.log(resultData);
    let genreList = jQuery("#genre-list");
    let htmlString = "";
    for (let i = 0; i < resultData.length; ++i) {
        htmlString += "<p>" + convertGenreToHyperlink(resultData[i]["movie_genres"]) + "</p>";
    }
    genreList.append(htmlString);
}

function populateHTMLWithCharacterData(resultData) {
    console.log(resultData);
    let characterList = jQuery("#character-list");
    let htmlString = "";
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