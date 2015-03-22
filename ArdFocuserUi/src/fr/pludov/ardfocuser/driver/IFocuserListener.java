package fr.pludov.ardfocuser.driver;

public interface IFocuserListener {
	/** Inoqu� lors d'un changement de l'�tat de connection */
	void statusChanged();
	
	/** Lorsque les param�tres exterieurs changent (pos, temp, ...) */
	void parametersChanged();

	void broadcastError(String string);
}
