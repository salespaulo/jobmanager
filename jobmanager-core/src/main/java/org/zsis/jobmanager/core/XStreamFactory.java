/**
 * jobmanager-core
 * 
 * Criada em 15/03/2011 15:09:45
 * 
 * Direito de cópia reservado à Z Sistemas S.A.
 * Todos os direitos são reservador em propriedade dda empresa
 * ------------- Z Sistemas S.A. --------------
 * O uso deste produto é sujeito aos termos de licença
 */
package org.zsis.jobmanager.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zsis.jobmanager.core.SchedulerContexts.SchedulerContext;


import com.thoughtworks.xstream.XStream;

/**
 * Classe que pode retornar instâncias de {@link XStream} pré-configuradas
 * para leitura de arquivos xml. Por exempo, o método {@link XStreamFactory#getJobManagerInstace()}
 * retorna uma instância pré-configurada para ler o arquivo xml de configurações
 * dos jobs. 
 * 
 * @author psales
 * @since 15/03/2011
 */
class XStreamFactory {

	/**
	 * Classe que implementa as configurações do {@link XStream} para
	 * leitura do arquivo xml de configurações de jobs.
	 * 
	 * @author psales
	 * @since 15/03/2011
	 */
	private static class XStreamJobManager extends XStream {
		/** Manipula operações de log */
		private static final Logger logger = LoggerFactory.getLogger(XStreamFactory.XStreamJobManager.class);
		
		/** Construtor que configura o {@link XStream} para ler o xml de configurações de job */
		private XStreamJobManager() {
			super();
			logger.debug("Configurando XStream para JobManager");
			this.alias(SCHEDULERS_ALIAS, SchedulerContexts.class);
			this.alias(SCHEDULER_ALIAS, SchedulerContext.class);
			this.alias(JOB_ALIAS, SchedulerContexts.JobContext.class);
			this.alias(TRIGGER_ALIAS, SchedulerContexts.TriggerContext.class);
			
			this.useAttributeFor(SchedulerContexts.JobContext.class, "name");
			this.useAttributeFor(SchedulerContexts.JobContext.class, "group");
			this.useAttributeFor(SchedulerContexts.JobContext.class, "type");

			this.useAttributeFor(SchedulerContexts.TriggerContext.class, "name");
			this.useAttributeFor(SchedulerContexts.TriggerContext.class, "group");
			this.useAttributeFor(SchedulerContexts.TriggerContext.class, "expression");
		}
	}
	
	/** Alias usado no arquivo XML */
	private static final String TRIGGER_ALIAS = "trigger";

	/** Alias usado no arquivo XML */
	private static final String JOB_ALIAS = "job";

	/** Alias usado no arquivo XML */
	private static final String SCHEDULER_ALIAS = "scheduler";

	/** Alias usado no arquivo XML */
	private static final String SCHEDULERS_ALIAS = "jobmanager";

	/** Instância do {@link XStream} para ler o xml de configurações de jobs */
	private static final XStream xstreamJobManager = new XStreamJobManager();
	
	/**
	 * Retorna uma instância de {@link XStream} configurada para ler o
	 * arquivo xml de configurações dos jobs.
	 *
	 * @return Uma instância {@link XStream} pré-configurada.
	 */
	public static XStream getJobManagerInstace() {
		return xstreamJobManager;
	}

	/** Construtor privado que não pode ser executado */
	private XStreamFactory() {
		throw new AssertionError();
	}
	
}
