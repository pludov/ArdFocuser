/*
 * pwmresistor.h
 *
 *  Created on: 5 févr. 2015
 *      Author: utilisateur
 */

#ifndef PWMRESISTOR_H_
#define PWMRESISTOR_H_

#include "scheduled.h"

class PWMResistor {
	uint8_t pin;


public:
	uint8_t pct;

	PWMResistor(uint8_t pin);
	~PWMResistor();

	void setLevel(uint8_t level);
};

#endif /* PWMRESISTOR_H_ */
