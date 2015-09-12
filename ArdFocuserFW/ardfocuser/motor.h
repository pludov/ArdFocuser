/*
 * motor.h
 *
 *  Created on: 3 févr. 2015
 *      Author: utilisateur
 */

#ifndef MOTOR_H_
#define MOTOR_H_

#include "scheduled.h"

class Motor : public Scheduled
{
private:
	const uint8_t * motorPins;
	unsigned long currentPosition;             // current position
	unsigned long targetPosition;              // target position

	// Direction & speed
	int speedLevel;
public:
	Motor(const uint8_t * pins);

	// Load stored position
	void loadPosition(unsigned long currentPosition);

	// Start moving to the given position
	void setTargetPosition(unsigned long newPosition);

	unsigned long getTargetPosition();

	unsigned long getCurrentPosition();

	// Is there a signal currently delivered ?
	bool isActive();

	// Is there an ongoing move ?
	bool isMoving();

	// Call when micros >= nextTick
	virtual void tick();
private:
	void setOutput(int out);
	void clearOutput();
	long getPulseDuration(int speedLevel);
};

extern Motor motor;

#endif /* MOTOR_H_ */
