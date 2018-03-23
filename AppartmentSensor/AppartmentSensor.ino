// include the library code:
#include <LiquidCrystal.h>

// initialize the library by associating any needed LCD interface pin
// with the arduino pin number it is connected to
const int rs = 12, en = 11, d4 = 5, d5 = 4, d6 = 3, d7 = 2;
LiquidCrystal lcd(rs, en, d4, d5, d6, d7);
//TMP36 Pin Variables
const int tmpSensorPin = A5; //the analog pin the TMP36's Vout (sense) pin is connected to
//the resolution is 10 mV / degree centigrade with a
//500 mV offset to allow for negative temperatures
const int micSensor = A0; //Mic is on pin 0
const int knockSensor = A3; // the piezo is connected to analog pin 0

const byte numChars = 16;
char receivedChars[numChars];
char nullBuffer[numChars] = "TEST";
String readIn = "WAITING";
boolean newData = false;
int micRet[2] = {0,0};

int knockReading = 0;
int micReading = 0;
String top = "WAITING";
String bottom = "WAITING";
String tmpMessage = "WAITING"; 
String seisMessage = "WAITING";
String sendInfo = "WAITING";
/*
  setup() - this function runs once when you turn your Arduino on
  We initialize the serial connection with the computer
*/
void setup() {
  lcd.begin(16, 2);
  Serial.begin(9600);  //Start the serial connection with the computer
  //to view the result open the serial monitor
}

void loop() {
  bottom = readIn;
  tmpMessage = String(ReadTempF(tmpSensorPin)) + "F";
  micRet[0] = 0;
  micRet[1] = 0;
  seismographAndAudio(knockSensor, micSensor, 1000, micRet);
  seisMessage = String(micRet[1]) + "Db";
  top = tmpMessage + " " + seisMessage;
  if (Serial.available() > 0) {
    Serial.readBytes(nullBuffer, numChars);
    String str(nullBuffer);
    readIn = str.substring(0,15);
  }
  sendInfo = top + " " + String(micRet[0]) + "Hz";
  Serial.println(sendInfo);
  lcd.clear();
  printLineOne(top);
  printLineTwo(bottom);

}

void seismographAndAudio(int knock, int mic, int delayTime, int *data) {
  for (int i = 0; i <= delayTime; i++) {
    knockReading = analogRead(knock);
    micReading = analogRead(mic); 
    if (knockReading > data[0]) {
      data[0] = knockReading;
    }
    if (micReading > data[1]) {
      data[1] = micReading;
    }
    delay(1);
  }

}

float ReadTempF(int sensor) {
  //getting the voltage reading from the temperature sensor
  int reading = analogRead(sensor);

  // converting that reading to voltage, for 3.3v arduino use 3.3
  float voltage = reading * 5.0;
  voltage /= 1024.0;
  float temperatureC = (voltage - 0.5) * 100; //converting from 10 mv per degree wit 500 mV offset
  //to degrees ((voltage - 500mV) times 100)
  // now convert to Fahrenheit
  float temperatureF = (temperatureC * 9.0 / 5.0) + 32.0;
  return temperatureF;
}
void printLineOne(String line) {
  lcd.setCursor(0, 0);
  // print the number of seconds since reset:
  lcd.print(line);
}
void printLineTwo(String line) {
  lcd.setCursor(0, 1);
  // print the number of seconds since reset:
  lcd.print(line);
}
