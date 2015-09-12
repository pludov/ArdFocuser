/*
 * status.h
 *
 *  Created on: 27 f�vr. 2015
 *      Author: utilisateur
 */

#ifndef STATUS_H_
#define STATUS_H_

#include "scheduled.h"

/*
 * Le protocol serie est :
 * Paquet de statut:
 *   [Pxxxxx] xxxxx = position moteur
 *   [My]		y = 0 off / 1 on
 *   [Txxxx]   xxxx = temp�rature scope en hexa de -327.68 � 327.67
 *   [Exxxxyy]   xxxx = temp�rature exterieure en hexa de -100.00 � 555.35
 *                 yyy = humidit� (0 � 100 en ramen� sur 0 - 16383)
 *   [Vxxx]   voltage en hexa, x 100 (1100 = 11V)
 *   [Hxx]	  heater % (hexa)
 *
 *
 * => chaque paquet va faire: ~20 caract�res. le buffer serie peut faire 64 maxi
 * Paquet de query:
 *  P.*#
 * Paquet de reponse:
 *  P.*#
 * Probl�me : on ne sait pas trouver la fin d'un paquet de r�ponse
 *
 */
struct Payload
{
	// Position hexa : de 0 � 262k
	char motor[5];
	// moving ou pas (0 ou 1)
	char motorState[1];
	// Hexa: de -255 � +255
	char scopeTemp[4];
	// Hexa: de 0 (-50) � +1024 (50)
	char extTemp[4];
	// Hexa: de 0 � 4095 (100%)
	char extHum[3];
	// Hexa
	char battery[3];
	// Hexa
	char heater[2];
} ;


class Status : public Scheduled{
	void sendStatus();
public:
	Status();
	virtual ~Status();

	void needUpdate();

	virtual void tick();

	Payload getStatusPayload();
};

extern Status status;



#endif /* STATUS_H_ */
