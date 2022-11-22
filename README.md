# Project 4 
## Team #37 Members
- Yaseen Khan(yaseenk@uci.edu)
- Jadon Kwan(jadonk@uci.edu)

## 1. Demo Video Link: 
https://youtu.be/UQZ_XNIDiSg

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


### Running Android App
1. To run the backend to the Tomcat server, follow the ```Option 1: Deploy from AWS instance``` instructions below.
2. Clone the github repo locally using ```git clone https://github.com/uci-jherold2-teaching/cs122b-fall-team-37.git```
3. Open the android directory of the repo using Android Studio or Intellij.
4. Change the host to your AWS public IP address in the BackendServer class constructor(```android/app/src/main/java/edu/uci/ics/fabflixmobile/BackendServer.java```).
5. Setup an Android Emulator if you do not have one. You can also connect your Android phone to run the app.
6. Turn on the Emulator.
7. Run the Application.

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




## 3. Member Contributions:
## Yaseen Khan
- Implemented Android Login, Search, Movie List Page, and Single Movie Page 

## Jadon Kwan
- Implemented Full-text Search for Movie Title field
- Implemented Autocomplete features and search

