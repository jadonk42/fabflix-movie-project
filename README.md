- # General
    - #### Team#: 37

    - #### Names:
      - Yaseen Khan(yaseenk@uci.edu)
      - Jadon Kwan(jadonk@uci.edu)

    - #### Project 5 Video Demo Link:

    - #### Instruction of deployment:
      - ##### Database Setup:
        - Download the latest version of MySQL if you don't have it 
        - Login to mysql as the root user: ```local> mysql -u root -p```
        - Create user CS122B and grant privileges
          ```
          mysql> CREATE USER 'CS122B'@'localhost' IDENTIFIED BY 'FabFlix';
          mysql> GRANT ALL PRIVILEGES ON * . * TO 'CS122B'@'localhost';
          mysql> quit;
          ```
        - Create the moviedb database
        ```
        local> mysql -u CS122B -p
        mysql> CREATE DATABASE IF NOT EXISTS moviedb;
        mysql> USE moviedb;
        mysql> quit;
        ```
        - Create the moviedb tables using the create_table.sql file: ```local> mysql -u CS122B -p < create_table.sql```
        - Populate the database data using the movie-data.sql file: ```local> mysql -u CS122B -p --database=moviedb < PATH/movie-data.sql```Populate the database data using the movie-data.sql file: ```local> mysql -u CS122B -p --database=moviedb < PATH/movie-data.sql```
      - ##### Option 1: Deploy from AWS
        - SSH into your AWS instance. EX: ```ssh -i My.pem ubuntu@ec2-myipaddress.us-west-2.compute.amazonaws.com```
        - Clone the github repo using ```git clone https://github.com/uci-jherold2-teaching/cs122b-fall-team-37.git```
        - If you don't have the moviedb database setup, follow the **Database Setup** instructions
        - If using a different database account, change the username and password in ```web/META_INF/context.xml```
        - Run ```mvn package``` to build the war file.
        - Copy the war file: ```sudo cp ./target/*.war /var/lib/tomcat9/webapps/```
        - Refresh your tomcat manager and deploy the app.
        - Head to the web page of your AWS instance.
      - ##### Option 2: Local(Intellij)
        - Clone the github repo using ```git clone https://github.com/uci-jherold2-teaching/cs122b-fall-team-37.git```
        - Open the Project in Intellij
        - If you don't have the moviedb database setup, follow the **Database Setup** instructions
        - If using a different database account, change the username and password in ```web/META_INF/context.xml```
        - Build project using maven.
        - Set up tomcat to use your war file (IntelliJ has configuration with Tomcat).
        - Open your localhost to the correct port.

    - #### Collaborations and Work Distribution:
      - ##### Yaseen Khan
      - ##### Jadon Kwan


- # Connection Pooling
    - #### Include the filename/path of all code/configuration files in GitHub of using JDBC Connection Pooling.
      - src/main/java/FullTextSearchServlet.java
      - src/main/java/SearchMovieServlet.java
      - src/main/java/AutocompleteSearchServlet.java
      - WebContent/META-INF/context.xml

    - #### Explain how Connection Pooling is utilized in the Fabflix code.
    - Connection Pooling is utilized in the Search servlets since those require user input. Based on the updated configurations in the context.xml file, the Prepared Statements for each servlet is cached so more than one JDBC connection can utilize these statements. Caching the Prepared Statements is needed since Prepared Statements are usually associated with one Connection.

    - #### Explain how Connection Pooling works with two backend SQL.


- # Master/Slave
    - #### Include the filename/path of all code/configuration files in GitHub of routing queries to Master/Slave SQL.

    - #### How read/write requests were routed to Master/Slave SQL?


- # JMeter TS/TJ Time Logs
    - #### Instructions of how to use the `log_processing.*` script to process the JMeter logs.


- # JMeter TS/TJ Time Measurement Report

| **Single-instance Version Test Plan**          | **Graph Results Screenshot** | **Average Query Time(ms)** | **Average Search Servlet Time(ms)** | **Average JDBC Time(ms)** | **Analysis** |
|------------------------------------------------|------------------------------|----------------------------|-------------------------------------|---------------------------|--------------|
| Case 1: HTTP/1 thread                          | ![](path to image in img/)   | ??                         | ??                                  | ??                        | ??           |
| Case 2: HTTP/10 threads                        | ![](path to image in img/)   | ??                         | ??                                  | ??                        | ??           |
| Case 3: HTTPS/10 threads                       | ![](path to image in img/)   | ??                         | ??                                  | ??                        | ??           |
| Case 4: HTTP/10 threads/No connection pooling  | ![](path to image in img/)   | ??                         | ??                                  | ??                        | ??           |

| **Scaled Version Test Plan**                   | **Graph Results Screenshot** | **Average Query Time(ms)** | **Average Search Servlet Time(ms)** | **Average JDBC Time(ms)** | **Analysis** |
|------------------------------------------------|------------------------------|----------------------------|-------------------------------------|---------------------------|--------------|
| Case 1: HTTP/1 thread                          | ![](path to image in img/)   | ??                         | ??                                  | ??                        | ??           |
| Case 2: HTTP/10 threads                        | ![](path to image in img/)   | ??                         | ??                                  | ??                        | ??           |
| Case 3: HTTP/10 threads/No connection pooling  | ![](path to image in img/)   | ??                         | ??                                  | ??                        | ??           |

