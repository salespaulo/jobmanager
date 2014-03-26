/**
 * jobmanager-core
 * 
 * Criada em 14/03/2011 19:54:26
 * 
 * Direito de cópia reservado à Z Sistemas S.A.
 * Todos os direitos são reservador em propriedade dda empresa
 * ------------- Z Sistemas S.A. --------------
 * O uso deste produto é sujeito aos termos de licença
 */
package org.zsis.jobmanager.core;

import java.text.ParseException;
import java.util.Date;

import org.quartz.CronTrigger;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zsis.jobmanager.core.SchedulerContexts.SchedulerContext;


/**
 * TODO (psales 14/03/2011) - javadoc
 * 
 * @author psales
 * @since 14/03/2011
 */
class XmlSchedulerManager implements SchedulerManager {

	/** TODO (psales 14/03/2011) - javadoc */
	private static final XmlSchedulerManager INSTANCE = new XmlSchedulerManager();

	/** TODO (psales 14/03/2011) - javadoc */
	private static SchedulerConfigurator configurator = null;
	
	/**
	 * TODO (psales 14/03/2011) - javadoc
	 *
	 * @return
	 */
	public static XmlSchedulerManager getInstance() {
		configurator = XmlSchedulerConfigurator.getInstance();
		return INSTANCE;
	}
	
	/**
	 * TODO (psales 15/03/2011) - javadoc
	 *
	 * @param file
	 * @return
	 */
	public static XmlSchedulerManager getInstance(String file) {
		configurator = XmlSchedulerConfigurator.getInstance(file);
		return INSTANCE; 
	}

	/** TODO (psales 14/03/2011) - javadoc */
	private final Logger logger = LoggerFactory.getLogger(XmlSchedulerManager.class);
	
	/** TODO (psales 14/03/2011) - javadoc */
	private XmlSchedulerManager() {
		super();
	}
	
	/* (non-Javadoc)
	 * @see org.zsis.jobmanager.core.SchedulerManager#doSchedulers()
	 */
	@Override
	public void doSchedulers() {
		this.start(true);
	}

	/* (non-Javadoc)
	 * @see org.zsis.jobmanager.core.SchedulerManager#finalizeSchedulers()
	 */
	@Override
	public void finalizeSchedulers() {
		try {
			logger.debug("Finalizando schedulers");
			configurator.getSchedulerRepository().clear();
			Schedulers.getScheduler().shutdown();
		} catch (Exception e) {
			logger.error("Erro ao finalizar schedulers: " + e.getMessage(), e);
			throw new SchedulerManagerException("Erro ao finalizar schedulers: " + e.getMessage(), e);
		}
	}

	/* (non-Javadoc)
	 * @see org.zsis.jobmanager.core.SchedulerManager#refresh()
	 */
	@Override
	public void refreshSchedulers() {
		logger.debug("Re-carregando schedulers");
		this.clearSchedulers();
		this.start(false);
	}

	/* (non-Javadoc)
	 * @see org.zsis.jobmanager.core.SchedulerManager#clear()
	 */
	@Override
	public void clearSchedulers() {
		logger.debug("Limpando schedulers");
		final SchedulerContexts contexts = configurator.getSchedulerRepository().get();
		
		for (SchedulerContext scheduler: contexts.getSchedulers()) {
			try {
				Schedulers.getScheduler().unscheduleJob(scheduler.getJob().getName(), scheduler.getJob().getGroup());
			} catch (SchedulerException e) {
				logger.error("Erro ao limpar schedulers: " + e.getMessage(), e);
				throw new SchedulerManagerException("Erro ao limpar schedulers: " + e.getMessage(), e);
			}
		}
		
		configurator.getSchedulerRepository().clear();
	}
	
	/* (non-Javadoc)
	 * @see org.zsis.jobmanager.core.SchedulerManager#storeSchedulers(org.zsis.jobmanager.core.SchedulerContexts)
	 */
	@Override
	public void storeSchedulers(SchedulerContexts schedulers) {
		configurator.getSchedulerRepository().save(schedulers);
	}
	
	/* (non-Javadoc)
	 * @see org.zsis.jobmanager.core.SchedulerManager#getSchedulers()
	 */
	@Override
	public SchedulerContexts getSchedulers() {
		return configurator.getSchedulerRepository().get();
	}

	/** TODO (psales 16/03/2011) - javadoc */
	private void start(boolean start) {
		configurator.getSchedulerRepository().refresh();

		final SchedulerContexts contexts = configurator.getSchedulerRepository().get();
	    
		for (SchedulerContext scheduler: contexts.getSchedulers()) {
			addScheduler(scheduler);
	    }
		
		try {
			logger.debug("Inicializando schedulers");
			
			if (start) {
				Schedulers.getScheduler().start();
			}
		} catch (SchedulerException e) {
			logger.error("Erro ao inicializar schedulers: " + e.getMessage(), e);
			throw new SchedulerManagerException("Erro ao inicializar schedulers: " + e.getMessage(), e);
		}
	}

	/** Adiciona o agendamento para ser executado pelo Quartz */
	private void addScheduler(SchedulerContext scheduler) {
		try {
			final String name = scheduler.getJob().getName();
			final String group = scheduler.getJob().getGroup();
			final Class<?> type = scheduler.getJob().getType();
			final String triggerName = scheduler.getTrigger().getName();
			final String triggerGroup = scheduler.getTrigger().getGroup();
			final String triggerExpression = scheduler.getTrigger().getExpression();
			
			logger.debug("Agendando JobDetail [name={}; group={}; jobClass={}; expression={}]",
					new Object[] { name, group, type, triggerExpression });
			
			final CronTrigger trigger = new CronTrigger(triggerName,
					triggerGroup, name, group, triggerExpression);

			Date schedulerDate = null;
			JobDetail job = Schedulers.getScheduler().getJobDetail(name, group);
			
			if (job != null) {
				schedulerDate = Schedulers.getScheduler().rescheduleJob(name, group, trigger);
				
			} else {
				job = new JobDetail(name, group, type);

				if (scheduler.getJob().getDataMap() != null) {
					final JobDataMap jobDataMap = new JobDataMap(scheduler.getJob().getDataMap());
	
					job.setJobDataMap(jobDataMap);
				}
				
				schedulerDate = Schedulers.getScheduler().scheduleJob(job, trigger);
			}
			
			logger.info("{} foi agendado para rodar às: {} e repetir baseado na expressção: {}",
					new Object[] { job.getFullName(), schedulerDate, trigger.getCronExpression() });
		} catch (ParseException eParse) {
			logger.error("Erro ao fazer o parse da expressão: " + eParse.getMessage(), eParse);
			throw new SchedulerManagerException("Erro ao fazer o parse da expressão: " + eParse.getMessage(), eParse);
			
		} catch (SchedulerException eScheduler) {
			logger.error("Erro ao agendar o job: " + eScheduler.getMessage(), eScheduler);
			throw new SchedulerManagerException("Erro ao agendar o job: " + eScheduler.getMessage(), eScheduler);
		}
	}

}
