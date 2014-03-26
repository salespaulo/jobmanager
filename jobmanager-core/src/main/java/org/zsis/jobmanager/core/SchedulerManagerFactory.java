/**
 * jobmanager-core
 * 
 * Criada em 15/03/2011 12:04:22
 * 
 * Direito de cópia reservado à Z Sistemas S.A.
 * Todos os direitos são reservador em propriedade dda empresa
 * ------------- Z Sistemas S.A. --------------
 * O uso deste produto é sujeito aos termos de licença
 */
package org.zsis.jobmanager.core;

/**
 * Classe que retorna instâncias específicas que implementam a 
 * interface {@link SchedulerManager}.
 * 
 * @author psales
 * @since 15/03/2011
 * @see SchedulerManager
 */
public class SchedulerManagerFactory {

	/**
	 * Retorna a instância que implementa {@link SchedulerManager} utilizando
	 * a leitura do arquivo xml de configuração padrão.
	 *
	 * @return Uma instância que implementa {@link SchedulerManager} utilizando
	 * a leitura do arquivo xml de configuração padrão.
	 */
	public static SchedulerManager getXmlSchedulerManager() {
		return XmlSchedulerManager.getInstance();
	}
	
	/**
	 * Retorna a instância que implementa {@link SchedulerManager} utilizando
	 * a leitura do arquivo xml de configuração passado como parâmetro.
	 *
	 * @param file
	 * @return Uma instância que implementa {@link SchedulerManager} utilizando
	 * a leitura do arquivo xml de configuração passado como parâmetro.
	 */
	public static SchedulerManager getXmlSchedulerManager(String file) {
		return XmlSchedulerManager.getInstance();
	}
	
	/** Construtor privado que não pode ser executado */
	private SchedulerManagerFactory() {
		throw new AssertionError();
	}
	
}
