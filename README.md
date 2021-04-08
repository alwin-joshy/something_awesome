## Getting Started

Welcome to the Awesome Password Manager. This application allows you to securely store all of your credentials across multiple services in one place. Make an account to get started 

## Dependencies

All the required libraries are included in the lib directory. 
Arduino-cli is also required for any arduino-based authentication in this program to work. 
This program was developed for Linux and will not work on Windows or OSX
Only Arduino UNO R3 has been tested for compatability, but other chips which have a serial number should work. 
The fingerprint sensor used is the is the Adafruit fingerprint sensor. 

## Code attribution

I did not write the Arduino code or C++ for the enroll and fingerprint sketches. The arduino sketches are modified versions of the sample code found in the Adafruit fingerprint library (https://github.com/adafruit/Adafruit-Fingerprint-Sensor-Library) to make them work more easily with Java.
