/**
 * jobmanager-core
 * 
 * Criada em 14/03/2011 19:52:13
 * 
 * Direito de cópia reservado à Z Sistemas S.A.
 * Todos os direitos são reservador em propriedade dda empresa
 * ------------- Z Sistemas S.A. --------------
 * O uso deste produto é sujeito aos termos de licença
 */
package org.zsis.jobmanager.core;

/**
 * Interface que possui operações para manipular os agendamentos
 * de jobs. 
 * 
 * @author psales
 * @since 14/03/2011
 */
public interface SchedulerManager {

	/**
	 * Realiza o agendamento de todos os jobs configurados
	 * e inicializa o processo que gerencia e executa cada 
	 * job seguindo suas configurações.
	 */
	void doSchedulers();

	/**
	 * Retira todos os jobs agendados e finaliza o processo de
	 * gerenciamento e execução dos jobs.
	 */
	void finalizeSchedulers();
	
	/**
	 * Atualiza as configurações de agendamento dos jobs que
	 * estão sendo gerenciados e executados.
	 */
	void refreshSchedulers();
	
	/**
	 * Limpa todos os jobs agendados do processo de gerenciamento
	 * e execução de jobs. 
	 */
	void clearSchedulers();
	
	/**
	 * Armazena e atualiza as configurações de agendamento dos jobs
	 * que estão sendo gerenciadose executados.
	 * 
	 * @param schedulers A configuraçõa dos agendamentos dos jobs.
	 */
	void storeSchedulers(SchedulerContexts schedulers);
	
	/**
	 * Retorna as configurações de agendamento dos jobs que
	 * estão sendo gerenciados e executados.
	 *
	 * @return As configurações de agendamentos dos jobs.
	 */
	SchedulerContexts getSchedulers();
	
}
