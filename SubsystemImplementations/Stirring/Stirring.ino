#define STATE_WAIT 0
#define STATE_TEMP 1
#define STATE_PH 2
#define STATE_STIR 3


int pstate;
int targetSpeed;
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
  if(state == STATE_STIR) {
    int x = (int) v;
    if(targetSpeed != x) {
      targetSpeed = x;
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

//import pH,Temp

//int Timer //PWM
const int pinA = A0; //connected to OSD, analog pin
const int pinX = 3; //connected to motor, PWM digital pin
int counter = 0;
//SerialPort
int receiveSpeed, sensLow, sensHigh, sensVal, sensMid, startTime, stopTime, voltageToMotor;
bool pHChangeVal = LOW;
bool TempChangeVal = LOW;
bool checked = HIGH;
bool Init;

void setup()
{
  pstate = STATE_WAIT;
  targetSpeed = -1;
  
  Init = HIGH;
  //analogWrite(pinX,20);
  pinMode(pinA, INPUT);
  pinMode(pinX, OUTPUT);
  //get (SerialPort)
  //get PWM(Timer)
  sensLow = 1023;
  sensHigh = 0;
  Serial.begin(9600);
   startTime = millis();
   stopTime = millis();
   while (stopTime - startTime<5000) {
      Serial.print("calibrating: ");
      Serial.println((stopTime-startTime));
    //calibrates for 5 seconds
      stopTime = millis();
      analogWrite(pinX,255);
      sensVal = analogRead(pinA);
      //records max sens val
      if (sensVal<sensLow)
      {
        sensLow = sensVal;
      }
     //records min sens val
      if (sensVal>sensHigh)
      {
        sensHigh = sensVal;
      }
   }
   sensMid = (sensHigh + sensLow)/2;
}
/*
void loop(){
  analogWrite(pinX,255);
  }
*/
void loop()
{
 if(send_confirm) {
    Serial.print("SC");
    Serial.println(targetSpeed); 
    send_confirm = false;
  }

  if(targetSpeed < 500 || targetSpeed > 1500) {
    return;  
  }
  
  startTime = millis();
  stopTime = millis();
  counter = 0;
  while (stopTime - startTime<5000)
  { //searches for 5 seconds
    stopTime = millis();
    sensVal = analogRead(pinA);//OSD gets 2 pulses per rev
    if (sensVal >= sensMid && sensVal <= sensHigh and checked == HIGH)
    {
      counter += 1;
      checked = LOW;
     }
     if (sensVal < sensMid && sensVal >= sensLow)
     {
      checked = HIGH;
     }
    }
  //import bool pH.pHChangeVal and Temp.tempChangeVal
  receiveSpeed = counter*6; //12 times 5 is 60 seconds, but it detects 2 times  per rev: multiplying by 12/2 = 6 puts receiveSpeed in RPM
  //Serial.print("speed received: ");
  //Serial.println(receiveSpeed);
  if ((pHChangeVal == HIGH) or (TempChangeVal == HIGH) or (Init == HIGH))
  {
    //map(x,fromLow,fromHigh,toLow,toHigh) and according to the datasheet the fastest speed of the motor is 11500rpm, notice that some users have actually counted this speed as 7500rpm
    voltageToMotor = 255 - map(map(targetSpeed*4,0,115000*4,0,3),0,3,0,255);
    Init = LOW;
    pHChangeVal = LOW;
    TempChangeVal = LOW;
   }
   if (receiveSpeed > targetSpeed + 20)
   {
    voltageToMotor += 1;
   }
   if (receiveSpeed < targetSpeed-20)
   {
    voltageToMotor -= 1;
   }
   analogWrite(pinX,voltageToMotor);

   Serial.print("SD");
   Serial.print(receiveSpeed);
   Serial.print(" ");
   Serial.println(millis());
   
//   Serial.print("motor speed sent in RPM: ");
//   Serial.println(targetSpeed);
//   Serial.print("Voltage send in pwm: ");
//   Serial.println(voltageToMotor);
 }
