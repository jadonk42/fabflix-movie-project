# Project 3 
## Team #37 Members
- Yaseen Khan(yaseenk@uci.edu)
- Jadon Kwan(jadonk@uci.edu)

## 1. Demo Video Link: 
https://drive.google.com/file/d/10PsVvP2xNPZlFrkQzgPXGiJz-EXc18gv/view?usp=sharing

## 2. How to deploy application:

### Database Setup:
1. Download the latest version of MySQL if you don't have it
2. Login to mysql as the root user: ```local> mysql -u root -p```
3. Create user CS122B and grant privileges
```
mysql> CREATE USER 'CS122B'@'localhost' IDENTIFIED BY 'FabFlix';
mysql> GRANT ALL PRIVILEGES ON * . * TO 'CS122B'@'localhost';
mysql> quit;
```
4. Create the moviedb database
```
local> mysql -u CS122B -p
mysql> CREATE DATABASE IF NOT EXISTS moviedb;
mysql> USE moviedb;
mysql> quit;
```
5. Create the moviedb tables using the create_table.sql file: ```local> mysql -u CS122B -p < create_table.sql```
6. Populate the database data using the movie-data.sql file: ```local> mysql -u CS122B -p --database=moviedb < PATH/movie-data.sql```

### XML Parsing(Locally)
1. To Run the parser, ensure the XML files are in the same directory as the **pom.xml** file
2. Open Intellij and run src/main/java/MainParser.java 
3. When the parser is finished, you can view the inconsistent reports in the same directory as **pom.xml** file.

### XML Parsing(Command Line)
1. To Run the parser, ensure the XML files are in the same directory as the **pom.xml** file
2. Clean the package using ```mvn clean package```
3. Compile the Parser using this command: ```java -cp mvn exec:java -q -e -Dexec.cleanupDaemonThreads=false -Dexec.mainClass="main.java.MainParser"``` 


### Option 1: Deploy from AWS instance
1. SSH into your AWS instance. EX: ```ssh -i My.pem ubuntu@ec2-myipaddress.us-west-2.compute.amazonaws.com```
2. Clone the github repo using ```git clone https://github.com/uci-jherold2-teaching/cs122b-fall-team-37.git```
3. If you don't have the moviedb database setup, follow the **Database Setup** instructions
4. If using a different database account, change the username and password in ```web/META_INF/context.xml```
5. Run ```mvn package``` to build the war file.
6. Copy the war file: ```cp ./target/*.war /var/lib/tomcat9/webapps/```
7. Refresh your tomcat manager and deploy the app.
8. Head to the web page of your AWS instance.

### Option 2: Local(Intellij)
1. Clone the github repo using ```git clone https://github.com/uci-jherold2-teaching/cs122b-fall-team-37.git```
2. Open the Project in Intellij
3. If you don't have the moviedb database setup, follow the **Database Setup** instructions
3. If using a different database account, change the username and password in ```web/META_INF/context.xml```
4. Build project using maven.
5. Set up tomcat to use your war file (IntelliJ has configuration with Tomcat).
6. Open your localhost to the correct port.

## 3. Prepared Statements Locations:
1. [AddMovieServlet.java](src/main/java/AddMovieServlet.java)
2. [AddStarServlet.java](src/main/java/AddStarServlet.java)
3. [BrowseMoviesServlet.java](src/main/java/BrowseMoviesServlet.java)
4. [BrowseServlet.java](src/main/java/BrowseServlet.java)
5. [DashboardLoginServlet.java](src/main/java/DashboardLoginServlet.java)
6. [DashboardMetadataServlet.java](src/main/java/DashboardMetadataServlet.java)
7. [LoginServlet.java](src/main/java/LoginServlet.java)
8. [MovieOrderConfirmationServlet.java](src/main/java/MovieOrderConfirmationServlet.java)
9. [MoviePaymentServlet.java](src/main/java/MoviePaymentServlet.java)
10. [MoviesServlet.java](src/main/java/MoviesServlet.java)
11. [ParseMovies.java](src/main/java/ParseMovies.java)
12. [ParseStars.java](src/main/java/ParseStars.java)
13. [ParseStarsInMovies.java](src/main/java/ParseStarsInMovies.java)
14. [SearchMovieServlet.java](src/main/java/SearchMovieServlet.java)
15. [SingleMovieServlet.java](src/main/java/SingleMovieServlet.java)
16. [SingleStarServlet.java](src/main/java/SingleStarServlet.java)

## 4. XML Parsing Optimizations
1. Used Batch Insertion to insert new movies, stars, and genres into the database. Compared to the regular insert, batch insert allows us to insert many rows of data at a time saving network resources and time.
2. Utilized Hashmaps and Sets to filter out inconsistent movies, stars, and genres(will be explained later). Hashmaps and Sets are efficient Data Structures that provides O(1) comparison and insertion. This is much faster compared to running a query for every single movie, star, and genre in the XML file in order to filter them out.


## 5. XML Inconsistent Data Reports
To view the inconsistent movies, genres, and stars, refer to the following text files:
1. inconsistent_genres.txt
2. inconsistent_movies.txt
3. inconsistent_stars.txt
4. inconsistent_moviesInStars.txt

These text files were obtained from running the XML Parser using the **XML Parsing** Instructions

- inconsistent_genres.txt contains Missing genres(null value) from movies and duplicate genre values 
- inconsistent_movies.txt contains Missing attributes from movies such as missing name, title, ID, and director. The text file also contains duplicate movies and movies with duplicate ID values
- inconsistent_stars.txt contains missing star names and duplicate stars already found in the database
- inconsistent_moviesInStars.txt contains movies that weren't inserted to the database, missing star name, and missing movie id  


## 6. Member Contributions:
## Yaseen Khan
- Ensured all SQL queries used Prepared Statements instead of Statements.
- Implemented XML parsing to insert movies, stars, and genres into the database.

## Jadon Kwan
- Implemented reCAPTCHA
- Encrypted all passwords and added encryption support.
- Implemented Employee Dashboard and Dashboard operations.
- Created Stored Procedure for inserting movie.

