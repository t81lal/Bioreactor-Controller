#define STATE_WAIT 0
#define STATE_TEMP 1
#define STATE_PH 2
#define STATE_STIR 3
#include <ctype.h>

const int phSensorPin = A1; //TEAM 25's PH probe
const int MotorPin1 = 10;
const int MotorPin2 = 5; 
const int controlPinIn = 2;
const int controlPinOut = 4;
const float tolerance = 0.5;

bool pumpOn = false;

int pstate;
float targetPH;
boolean send_confirm = false;

int state_for_char(int c) {
  switch(c) {
    case 'T':
      return STATE_TEMP;
    case 'S':
      return STATE_STIR;
    case 'P':
      return STATE_PH;
    default:
      return STATE_WAIT;  
  }  
}

void consume(int state, float v) {
  if(state == STATE_PH) {
    int x = (int) v;
    if(targetPH != x) {
      targetPH = x;
      send_confirm = true; 
    }
  }
}

void serialEvent() {
  while(Serial.available()) {
    switch(pstate) {
      case STATE_WAIT:
        pstate = state_for_char(Serial.read());
        break;
      case STATE_TEMP:
      case STATE_PH:
      case STATE_STIR:
        float f = Serial.parseFloat();
        consume(pstate, f);
        pstate = STATE_WAIT;  
        break;
    }
  }
}

void setup() {
  // put your setup code here, to run once:
  Serial.begin(9600);
  pinMode(phSensorPin,INPUT);
  targetPH = -1;

}

void loop() {

   if(send_confirm) {
    
      Serial.print("PC");
      Serial.println(targetPH); 
      send_confirm = false;
    }
    
  if(targetPH == -1) {
    return;
  }
  
  // put your main code here, to run repeatedly:
  double phRawValue = analogRead(phSensorPin) * (5000.00/1023.00);
  double ph = getpH(300, phRawValue);
  //double targetPh = setPH();

  
   Serial.print("PD");
   Serial.print((float)ph);
   Serial.print(" ");
   Serial.println(millis());
  
  /**
  //test output of the Ph that is recorded
  Serial.print("Raw Voltage: ");
  Serial.print(phRawValue);
  Serial.print(" ");
  Serial.print(analogRead(A4) * (5000.00/1023.00));
  delay(100);
  Serial.print(" ");
  Serial.print(analogRead(A3) * (5000.00/1023.00));
  delay(100);
  Serial.print(" ");
  Serial.print(analogRead(A2) * (5000.00/1023.00));
  delay(100);
  Serial.print(" ");
  Serial.print(analogRead(A1) * (5000.00/1023.00));
  delay(100);
  Serial.print(" ");
  Serial.print(analogRead(A0) * (5000.00/1023.00));
  delay(100);
 
  Serial.print("\t Calculated PH: ");
  Serial.print(ph);
  Serial.print("\t Target PH:");
  Serial.println(targetPh);
  **/

  if(ph + tolerance > targetPH){
    digitalWrite(MotorPin1, HIGH);
    pumpOn = true;
  }
  else if(ph < targetPH){
    digitalWrite(MotorPin2, HIGH);
    pumpOn = true;
  }
  else if(pumpOn){
    digitalWrite(MotorPin1, LOW);
    digitalWrite(MotorPin2, LOW);
    pumpOn = false;
  }
  delay(500);
}

double getpH(int temp, double Ex){
  double R = 8.314510;
  double F = 96485.309;
  double Es = 1.748;
  double VperPH;
  double ph;
  VperPH = R * temp * log(10) / F;
  ph = 7 + (Es - Ex/1000)/VperPH;
  return ph;
}

double getPH(int pH){
  String outputString = "//#" + pH;
  //Serial.println(outputString);
}


double setPH(){
  if(Serial.available() > 0){
    char inputString = Serial.read();
    if(inputString == '#'){
      String input = "";
      char externalinput;
      do{
        externalinput = Serial.read();
        if(isDigit(externalinput) || externalinput == '.'){
          input += externalinput;
        }
      }
      while(externalinput != '\n');
      double target = input.toDouble();
      if(target < 0 || target > 14){
        Serial.println("ERROR: Target PH exceeds boundaries (0 to 14)");
      } else {
        return target;
      }
    }
  }
} 
