# Project 1 
## Team #37 Members
- Yaseen Khan(yaseenk@uci.edu)
- Jadon Kwan(jadonk@uci.edu)

## 1. Demo Video Link: 

## 2. How to deploy application:

### Database Setup:
1. Download the latest version of MySQL if you don't have it
2. Login to mysql as the root user

### Option 1: Deploy from AWS instance
1. SSH into your AWS instance. EX: ```ssh -i My.pem ubuntu@ec2-myipaddress.us-west-2.compute.amazonaws.com```
2. Clone the github repo using ```git clone https://github.com/uci-jherold2-teaching/cs122b-fall-team-37.git```
3. If you don't have the moviedb database setup, follow the **Database Setup** instructions
4. If using a different database account, change the username and password in ```web/META_INF/context.xml```

### Option 2: Intellij
1. Clone the github repo using ```git clone https://github.com/uci-jherold2-teaching/cs122b-fall-team-37.git```
2. Open the Project in Intellij
3. If using a different database account, change the username and password in ```web/META_INF/context.xml```
4. Setup TomCat configuration
5. Run the server

## 3. Member Contributions:
## Yaseen Khan
- Connected moviedb database to project
- Wrote SQL queries to extract movies and stars information from moviedb database
- Implemented backend servlets of the movies page, single movie page, and single star page
- CSS styling of all pages
- README

## Jadon Kwan
- Project initial setup
- Implemented frontend servlets of the movies page, single movie page, and single star page
- Connected frontend to backend servlets
- Project 1 demo video

