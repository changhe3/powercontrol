#include <Printers.h>
#include <XBee.h>

XBee xbee = XBee();
Rx64Response rx64 = Rx64Response();
bool isOn = false;
double power = 0;

void setup() {
  // put your setup code here, to run once:
  delay(1000);
  Serial.begin(9600);
  xbee.setSerial(Serial);
  pinMode(13, OUTPUT);
  pinMode(A1, INPUT);
}

void loop() {
  // put your main code here, to run repeatedly:
  
  int in = analogRead(A1);
  power = (5 - 5 * ((double) in / 1023.0))/330 * 5;

  union {double in; unsigned char out[4]; } convert;
  convert.in = power;
  
  xbee.readPacket();
  if (xbee.getResponse().isAvailable()) {
    //Serial.print("getResponse!\n");
    if (xbee.getResponse().getApiId() == RX_64_RESPONSE) {
      xbee.getResponse().getRx64Response(rx64);
      isOn = rx64.getData(0);
      digitalWrite(13, isOn ? HIGH : LOW);

      //delay(1000);
      Tx64Request tx64(rx64.getRemoteAddress64(), &convert.out[0], 4);
      xbee.send(tx64);
    } 
  }else if (xbee.getResponse().isError()) {
      Serial.print("Error!\n");  
  }
}
