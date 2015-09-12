/*
 * Scheduled.cpp
 *
 *  Created on: 3 févr. 2015
 *      Author: utilisateur
 */

#include <Arduino.h>
#include "scheduled.h"

Scheduled::Scheduled()
{
	this->nextTick = UTime::never();
	this->priority = 0;
	this->tickExpectedDuration = US(0);
}

Scheduled::~Scheduled()
{
}

