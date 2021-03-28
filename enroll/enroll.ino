/***************************************************
  This is an example sketch for our optical Fingerprint sensor

  Designed specifically to work with the Adafruit BMP085 Breakout
  ----> http://www.adafruit.com/products/751

  These displays use TTL Serial to communicate, 2 pins are required to
  interface
  Adafruit invests time and resources providing this open source code,
  please support Adafruit and open-source hardware by purchasing
  products from Adafruit!

  Written by Limor Fried/Ladyada for Adafruit Industries.
  BSD license, all text above must be included in any redistribution
 ****************************************************/

#include "src/Adafruit-fingerprint/Adafruit_Fingerprint.h"


#if (defined(__AVR__) || defined(ESP8266)) && !defined(__AVR_ATmega2560__)
// For UNO and others without hardware serial, we must use software serial...
// pin #2 is IN from sensor (GREEN wire)
// pin #3 is OUT from arduino  (WHITE wire)
// Set up the serial port to use softwareserial..
SoftwareSerial mySerial(2, 3);

#else
// On Leonardo/M0/etc, others with hardware serial, use hardware serial!
// #0 is green wire, #1 is white
#define mySerial Serial1

#endif


Adafruit_Fingerprint finger = Adafruit_Fingerprint(&mySerial);

uint8_t id;

void setup()
{
  Serial.begin(9600);
  while (!Serial);  // For Yun/Leo/Micro/Zero/...
  delay(100);
  //Serial.println("\n\nAdafruit Fingerprint sensor enrollment");

  // set the data rate for the sensor serial port
  finger.begin(57600);

  if (finger.verifyPassword()) {
    //Serial.println("Found fingerprint sensor!");
  } else {
    //Serial.println("Did not find fingerprint sensor :(");
    return;
    while (1) { delay(1); }
  }

  //Serial.println(F("Reading sensor parameters"));
  //finger.getParameters();
  //Serial.print(F("Status: 0x")); Serial.println(finger.status_reg, HEX);
  //Serial.print(F("Sys ID: 0x")); Serial.println(finger.system_id, HEX);
  //Serial.print(F("Capacity: ")); Serial.println(finger.capacity);
  //Serial.print(F("Security level: ")); Serial.println(finger.security_level);
  //Serial.print(F("Device address: ")); Serial.println(finger.device_addr, HEX);
  //Serial.print(F("Packet len: ")); Serial.println(finger.packet_len);
  //Serial.print(F("Baud rate: ")); Serial.println(finger.baud_rate);
}

uint8_t readnumber(void) {
  uint8_t num = 0;

  while (num == 0) {
    while (! Serial.available());
    num = Serial.read(); //Serial.parseInt();
  }
  return num;
}

void loop()                     // run over and over again
{
  //Serial.println("Ready to enroll a fingerprint!");
  //Serial.println("Please type in the ID # (from 1 to 127) you want to save this finger as...");
  delay(100);
  id = readnumber();
  if (id == 0) {// ID #0 not allowed, try again!
     return;
  }
  Serial.print("Enrolling ID #");
  Serial.println(id);

  while (!  getFingerprintEnroll() );
}

uint8_t getFingerprintEnroll() {

  int p = -1;
  Serial.print("Waiting for valid finger to enroll as #"); Serial.println(id);
  while (p != FINGERPRINT_OK) {
    p = finger.getImage();
    switch (p) {
    case FINGERPRINT_OK:
      Serial.println("taken");
      break;
    case FINGERPRINT_NOFINGER:
      Serial.println(".");
      break;
    case FINGERPRINT_PACKETRECIEVEERR:
      Serial.println("communication");
      break;
    case FINGERPRINT_IMAGEFAIL:
      Serial.println("imaging");
      break;
    default:
      Serial.println("unknown");
      break;
    }
  }

  // OK success!
  delay(100);

  p = finger.image2Tz(1);
  switch (p) {
    case FINGERPRINT_OK:
      Serial.println("converted");
      break;
    case FINGERPRINT_IMAGEMESS:
      Serial.println("messy");
      return p;
    case FINGERPRINT_PACKETRECIEVEERR:
      Serial.println("communication");
      return p;
    case FINGERPRINT_FEATUREFAIL:
      Serial.println("features");
      return p;
    case FINGERPRINT_INVALIDIMAGE:
      Serial.println("features");
      return p;
    default:
      Serial.println("Unknown");
      return p;
  }

  delay(100);

  delay(2000);
  p = 0;
  while (p != FINGERPRINT_NOFINGER) {
    p = finger.getImage();
    Serial.println("finger");
  }
  Serial.println("noFinger");
  //Serial.print("ID "); Serial.println(id);
  p = -1;
  //Serial.println("Place same finger again");

  delay(100);
  
  while (p != FINGERPRINT_OK) {
    p = finger.getImage();
    switch (p) {
    case FINGERPRINT_OK:
      Serial.println("taken");
      break;
    case FINGERPRINT_NOFINGER:
      Serial.println(".");
      break;
    case FINGERPRINT_PACKETRECIEVEERR:
      Serial.println("communication");
      break;
    case FINGERPRINT_IMAGEFAIL:
      Serial.println("imaging");
      break;
    default:
      Serial.println("unknown");
      break;
    }
  }

  // OK success!

  delay(100);

  p = finger.image2Tz(2);
  switch (p) {
    case FINGERPRINT_OK:
      Serial.println("converted");
      break;
    case FINGERPRINT_IMAGEMESS:
      Serial.println("messy");
      return p;
    case FINGERPRINT_PACKETRECIEVEERR:
      Serial.println("communication");
      return p;
    case FINGERPRINT_FEATUREFAIL:
      Serial.println("features");
      return p;
    case FINGERPRINT_INVALIDIMAGE:
      Serial.println("features");
      return p;
    default:
      Serial.println("unknown");
      return p;
  }

  delay(100);

  p = finger.createModel();
  if (p == FINGERPRINT_OK) {
    Serial.println("matched");
  } else if (p == FINGERPRINT_PACKETRECIEVEERR) {
    Serial.println("communication");
    return p;
  } else if (p == FINGERPRINT_ENROLLMISMATCH) {
    Serial.println("nomatch");
    return p;
  } else {
    Serial.println("unknown");
    return p;
  }

  delay(100);

  p = finger.storeModel(id);
  if (p == FINGERPRINT_OK) {
    Serial.println("stored");
  } else if (p == FINGERPRINT_PACKETRECIEVEERR) {
    Serial.println("communication");
    return p;
  } else if (p == FINGERPRINT_BADLOCATION) {
    Serial.println("location");
    return p;
  } else if (p == FINGERPRINT_FLASHERR) {
    Serial.println("flash");
    return p;
  } else {
    Serial.println("unknown");
    return p;
  }

  return true;
}
