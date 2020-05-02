# ServerBasedChat

## SETUP AND CONFIGURATION

### Configuring database  
MongoDB was chosen in order to be a compromise since there was a desire to have both flat files and a database management system. The most recent version that can run on mininet is 4.0.18. 

Installation of MongoDB server onto mininet, taken from https://docs.mongodb.com/v4.0/tutorial/install-mongodb-on-ubuntu/: 

  `wget -qO - https://www.mongodb.org/static/pgp/server-4.0.asc | sudo apt-key add - `  
  
  `echo "deb [ arch=amd64 ] https://repo.mongodb.org/apt/ubuntu trusty/mongodb-org/4.0 multiverse" | sudo tee /etc/apt/sources.list.d/mongodb-org-4.0.list`  
  
 ` sudo apt-get update`  
 
  `sudo apt-get install -y mongodb-org=4.0.18 mongodb-org-server=4.0.18 mongodb-org-shell=4.0.18 mongodb-org-mongos=4.0.18 mongodb-org-tools=4.0.18`

### Java  
For this project, we settled on Java 11 since it’s the current LTS. 
To get this running on mininet: 

  `sudo apt-get install software-properties-common`  
  `sudo add-apt-repository ppa:openjdk-r/ppa`  
  `sudo apt update`  
  `sudo apt install openjdk-11-jdk`  

### Gradle  
For the management of our dependencies, we’re using gradle. Since we’re using Java 11, gradle version 5 or greater is required.  
To get this running on mininet: 

  `sudo add-apt-repository ppa:cwchien/gradle `  
  `sudo apt-get update `  
  `sudo apt upgrade gradle `  
  
 
 ## TO PREPARE SYSTEMS:
 If installed correcctly, the daemon for MongoDB should be up and running once installed. To check, type:  
 `sudo systemctl status mongodbd`  
 As for Gradle, we need to build the jar file for the dependencies to be handled. To build the jar file, type:  
 `gradle build`
 `gradle buildDependencies`  
  
## TO RUN:  
To run in server mode:  
pass in arguments `server [port number]` when running `Main.java`  
`java Main.java server <port number>`  
  
To run in client mode:  
pass in arguments `client [port number]` when running  `Main.java`  
`java Main.java client <port number>`
