/**
 * jobmanager-core
 * 
 * Criada em 04/03/2011 19:49:21
 * 
 * Direito de copia reservado a Z Sistemas S.A.
 * Todos os direitos sao reservador em propriedade da empresa
 * ------------- Z Sistemas S.A. --------------
 * O uso deste produto esta sujeito aos termos de licenca
 */
package org.zsis.jobmanager.core;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO (psales 04/03/2011) - javadoc
 * 
 * @author psales
 * @since 04/03/2011
 */
class Schedulers {

	/** TODO (psales 04/03/2011) - javadoc */
	private static final Logger logger = LoggerFactory.getLogger(Schedulers.class);
	
	/** TODO (psales 04/03/2011) - javadoc */
	private static final SchedulerFactory factory;
	
	static {
		factory = new StdSchedulerFactory();
		logger.trace("SchedulerFactory padrão foi criado: {}", factory.toString());
	}
	
	/**
	 * TODO (psales 04/03/2011) - javadoc
	 *
	 * @return
	 */
	public static SchedulerFactory getSchedulerFactory() {
		return factory;
	}
	
	/**
	 * TODO (psales 04/03/2011) - javadoc
	 *
	 * @return
	 */
	public static Scheduler getScheduler() {
		try {
			final Scheduler scheduler = factory.getScheduler();
			logger.trace("Retornando Scheduler padrão: {}", scheduler);
			return scheduler;
		} catch (SchedulerException e) {
			logger.error(e.getMessage(), e);
			throw new QuartzSchedulerException(e.getMessage(), e);
		}
	}
	
	/** TODO (psales 04/03/2011) - javadoc */
	private Schedulers() {
		throw new AssertionError();
	}
	
}
