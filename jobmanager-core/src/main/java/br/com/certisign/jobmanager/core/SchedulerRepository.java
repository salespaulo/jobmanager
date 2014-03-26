/**
 * jobmanager-core
 * 
 * Criada em 11/03/2011 19:04:37
 * 
 * Direito de copia reservado a Certisign Certificadora Digital S.A.
 * Todos os direitos sao reservador em propriedade da empresa
 * ------------- Certisign Certificadora Digital S.A. --------------
 * O uso deste produto esta sujeito aos termos de licenca
 */
package br.com.certisign.jobmanager.core;


/**
 * TODO (psales 11/03/2011) - javadoc
 * 
 * @author psales
 * @since 11/03/2011
 */
interface SchedulerRepository {

	/**
	 * TODO (psales 11/03/2011) - javadoc
	 *
	 * @param context
	 * @return
	 */
	SchedulerContexts get();
	
	/**
	 * TODO (psales 14/03/2011) - javadoc
	 *
	 */
	void clear();
	
	/**
	 * TODO (psales 14/03/2011) - javadoc
	 *
	 */
	void refresh();
	
	/**
	 * TODO (psales 15/03/2011) - javadoc
	 *
	 * @param schedulers
	 */
	void save(SchedulerContexts schedulers);
	
}
