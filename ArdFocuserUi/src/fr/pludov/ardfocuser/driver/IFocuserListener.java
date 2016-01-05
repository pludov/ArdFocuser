package fr.pludov.ardfocuser.driver;

public interface IFocuserListener {
	/** Inoqué lors d'un changement de l'état de connection */
	void statusChanged();
	
	/** Lorsque les paramètres exterieurs changent (pos, temp, ...) */
	void parametersChanged();

	/** Lorsque les filtres sont mis à jour */
	void filterDefinitionChanged();
	
	void broadcastError(String string);
}
