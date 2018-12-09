#define STATE_WAIT 0
#define STATE_TEMP 1
#define STATE_PH 2
#define STATE_STIR 3
#include <math.h>

int sensorPin = 0;
int ledPin = 9;

int pstate;
int targetTemp;
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
  if(state == STATE_TEMP) {
    int x = (int) v;
    if(targetTemp != x) {
      targetTemp = x;
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
 

void setup(){
  pstate = STATE_WAIT;
  targetTemp = -1;
  Serial.begin(9600);
}
  
 
void loop()     
{

   if(send_confirm) {
    
      Serial.print("TC");
      Serial.println(targetTemp); 
      send_confirm = false;
    }
    
  if(targetTemp == -1) {
    return;
  }
  
   //getting the voltage reading from the temperature sensor
   int reading = analogRead(sensorPin);
   
   float voltage = reading * 5.0;
   voltage /= 1024.0; 
     
   
   //float temperatureC = ((voltage - 0.5) * 100) +75 ; 
   //Serial.print(temperatureC); Serial.println(" degrees C");
   
   
   float resistance = (10000 * voltage) / (5- voltage);
   float temperatureC = ((log(resistance / 10000) / -0.04814) + 25);
   float tempz = temperatureC - ((35-temperatureC)*0.05);
   float comptemp;
   comptemp = (45-tempz)*3;


   //Serial.print("tempz: ");
   //Serial.print(tempz);
   //Serial.print(" comptemp: ");
   //Serial.print(comptemp);
   //Serial.print(" target: ");
   //Serial.println(targetTemp);
   if(tempz<(targetTemp-comptemp))
   {
    digitalWrite(ledPin, HIGH);
    delay(200);
   }
   else
   {
    digitalWrite(ledPin, LOW);
    delay(200);
   }

   Serial.print("TD");
   Serial.print(tempz);
   Serial.print(" ");
   Serial.println(millis());
}
