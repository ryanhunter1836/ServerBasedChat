# ServerBasedChat

## DOWNLOADING/CLONING  
You can either download the .zip file from GitHub or you can clone it using the command:  
`git clone https://github.com/ryanhunter1836/ServerBasedChat.git`  

## SETUP AND CONFIGURATION
### THIS CAN ALL BE PERFORMED WITH ONE COMMAND  
The code below is in case this command does not work. The file `setup.sh` in the folder should be able to execute all of these commands below:  
 `bash setup.sh`  

### THIS CAN ALL BE PERFORMED WITH ONE COMMAND  
The code below is in case this command does not work. The file `setup.sh` in the folder should be able to execute all of these commands below:  
 `bash setup.sh`  

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
 `gradle buildJar`
 `gradle buildJarDatabaseInit`  
  
## TO RUN:  
To run in server mode:  
pass in arguments `server <port number>` when running `Main.java`  
`java -jar <jarfile> server <port number>`  
  
To run in client mode:  
pass in arguments `client <port number>` and `<key path>` when running  `Main.java`. `<key path>` is the path where the keys of the users are held (for example, the path can be `./database/key1.txt`).  
`java -jar <jarfile> client <port number> <key path>`  
