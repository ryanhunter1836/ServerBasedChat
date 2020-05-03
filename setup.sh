#!/bin/bash

# Update the system (Issues with old SSL versions)
sudo apt-get update

# Add repositories and update package list
wget -qO - https://www.mongodb.org/static/pgp/server-4.0.asc | sudo apt-key add -
echo "deb [ arch=amd64 ] https://repo.mongodb.org/apt/ubuntu trusty/mongodb-org/4.0 multiverse" | sudo tee /etc/apt/sources.list.d/mongodb-org-4.0.list
sudo apt-get install -y software-properties-common
sudo add-apt-repository -y ppa:openjdk-r/ppa
sudo add-apt-repository -y ppa:cwchien/gradle
sudo apt-get update

# Install dependencies
sudo dpkg --purge --force-depends ca-certificates-java
sudo apt-get install -y openjdk-11-jdk ca-certificates-java gradle mongodb-org=4.0.18 mongodb-org-server=4.0.18 mongodb-org-shell=4.0.18 mongodb-org-mongos=4.0.18 mongodb-org-tools=4.0.18
sudo update-ca-certificates -f

# Build the project
gradle buildJar
gradle buildJarDatabaseInit
mv build/libs/* ./