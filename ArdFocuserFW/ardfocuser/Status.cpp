/*
 * Status.cpp
 *
 *  Created on: 27 févr. 2015
 *      Author: utilisateur
 */
#include <Arduino.h>
#include "Status.h"
#include "MainLogic.h"
#include "meteoTemp.h"
#include "motor.h"
#include "voltmeter.h"
#include "scopetemp.h"
#include "pwmresistor.h"
#include "debug.h"
#include "SerialIO.h"

extern PWMResistor resistor;

// a 115200, on transmet un caractère (9 bits), en: 1000000 / (115200 / 9) us
#define CHAR_XMIT_DELAY 79




static int pendingWrite()
{
	return (unsigned int)(SERIAL_BUFFER_SIZE + Serial._tx_buffer_head - Serial._tx_buffer_tail) % SERIAL_BUFFER_SIZE;
}

Status::Status()
{
	this->nextTick = UTime::now();
	this->priority = 2;
	this->tickExpectedDuration = MS(1);
}

Status::~Status()
{}


void Status::needUpdate()
{
	UTime n = UTime::now();
	if (this->nextTick > n) {
		this->nextTick = n;
	}
}

void Status::tick()
{
	// Si on a de la place sur le buffer de sortie, alors on accepte
	int toWait = pendingWrite();
	if (toWait >= sizeof(struct Payload)) {
		// Il faut attendre pendant au moins ce temps
		this->nextTick = UTime::now() + (unsigned long)toWait * CHAR_XMIT_DELAY;
		return;
	}
	sendStatus();
	this->nextTick += LongDuration::seconds(10);
}


static void writeHex(char * buff, int length, unsigned long value)
{
	while(length > 0) {
		length--;
		int i = value & 15;
		if (i >= 10) {
			buff[length] = 'A' + i - 10;
		} else {
			buff[length] = '0' + i;
		}
		value = value >> 4;
	}
}

static void writeBlank(char * buffer, int length)
{
	for(int i = 0;i < length; ++i)
	{
		buffer[i] = ' ';
	}
}

static void writeTemp(char * buffer, float temp)
{
	if (temp == NAN) {
		writeBlank(buffer, 4);
		return;
	}
	unsigned long scaledValue = round(temp * 100 + 32768);
	writeHex(buffer, 4, scaledValue);
}

static void writeHum(char * buffer, float hum)
{
	if (hum == NAN) {
		writeBlank(buffer, 3);
	}
	unsigned long scaledValue = round(hum * 40.95);
	writeHex(buffer, 3, scaledValue);
}

static void writeVolt(char * buffer, float hum)
{
	if (hum == NAN) {
		writeBlank(buffer, 3);
	}
	unsigned long scaledValue = round(hum * 100.0);
	writeHex(buffer, 3, scaledValue);
}

Payload Status::getStatusPayload()
{
	Payload s;
	writeHex(s.motor, 5, motor.getCurrentPosition());
	writeHex(s.motorState, 1, motor.isMoving() ? 1 : 0);

	writeTemp(s.scopeTemp, scopeTemp.lastValue);
	writeTemp(s.extTemp, meteoTemp.lastTemperature());
	writeHum(s.extHum, meteoTemp.lastHumidity());

	writeVolt(s.battery, voltmeter.lastValue());
	writeHex(s.heater, 2, resistor.pct);
	return s;
}

void Status::sendStatus()
{
#ifdef DEBUG
	Serial.println(F("MMMMMS°sco°ext%%%VVVHH"));
#endif


#ifndef NO_STATUS
	Payload s = getStatusPayload();
	serialIO.sendPacket('S', (uint8_t*)&s, sizeof(s));
#endif

#ifdef DEBUG
	Serial.println("");
#endif
}



