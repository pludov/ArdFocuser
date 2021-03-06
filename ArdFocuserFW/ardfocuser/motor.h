/*
 * motor.h
 *
 *  Created on: 3 f�vr. 2015
 *      Author: utilisateur
 */

#ifndef MOTOR_H_
#define MOTOR_H_

#include "scheduled.h"
#include "Config.h"

class Motor : public Scheduled
{
protected:
	const uint8_t * motorPins;
	unsigned long currentPosition;             // current position
	unsigned long targetPosition;              // target position

	// At full speed, how short a step is ?
	int fastestPerHalfStep;

	// Direction & speed
	int speedLevel;

	uint8_t positionConfigId;

	PositionStorage & positionConfig();

public:
	Motor(const uint8_t * pins, uint8_t positionConfigId = ID_STORAGE_POSITION, int fastestPerHalfStep = 4 * 2200);

	// Load stored position
	void loadPosition(unsigned long currentPosition);

	virtual void loadConfigPosition();

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

	// Call when currentPositoin == targetPosition during move (motor still not off)
	virtual void targetPositionReached();
protected:
	void setOutput(int out);
	void clearOutput();
	long getPulseDuration(int speedLevel);
};

extern Motor motor;

#endif /* MOTOR_H_ */
