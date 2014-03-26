/**
 * jobmanager-core
 * 
 * Criada em 11/03/2011 19:28:40
 * 
 * Direito de copia reservado a Z Sistemas S.A.
 * Todos os direitos sao reservador em propriedade da empresa
 * ------------- Z Sistemas S.A. --------------
 * O uso deste produto esta sujeito aos termos de licenca
 */
package org.zsis.jobmanager.core;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO (psales 11/03/2011) - javadoc
 * 
 * @author psales
 * @since 11/03/2011
 */
class XmlSchedulerRepository implements SchedulerRepository {

	/** TODO (psales 14/03/2011) - javadoc */
	private static final XmlSchedulerRepository INSTANCE = new XmlSchedulerRepository();
	
	/** Nome do arquivo de configuração */
	private static String fileConfig = null;

	/**
	 * TODO (psales 14/03/2011) - javadoc
	 *
	 * @return
	 */
	public static XmlSchedulerRepository getInstance(String fileName) {
		fileConfig = fileName;
		INSTANCE.loadFromXml();
		return INSTANCE;
	}

	/** TODO (psales 14/03/2011) - javadoc */
	private final Logger logger = LoggerFactory.getLogger(XmlSchedulerRepository.class);
	
	/** TODO (psales 14/03/2011) - javadoc */
	private XmlSchedulerRepository() {
		this.schedulers = SchedulerContexts.getNullInstance();
	}
	
	/** TODO (psales 14/03/2011) - javadoc */
	private SchedulerContexts schedulers;

	/* (non-Javadoc)
	 * @see org.zsis.jobmanager.core.SchedulerRepository#get()
	 */
	public SchedulerContexts get() {
		logger.debug("Retornando SchedulerContexts do repositório: {}", schedulers);
		synchronized(this) {
			return schedulers;
		}
	}

	/* (non-Javadoc)
	 * @see org.zsis.jobmanager.core.SchedulerRepository#clear()
	 */
	@Override
	public void clear() {
		logger.debug("Limpando SchedulerContexts do repositório: {}", schedulers);
		synchronized(this) {
			this.schedulers = SchedulerContexts.getNullInstance();
		}
	}
	
	/* (non-Javadoc)
	 * @see org.zsis.jobmanager.core.SchedulerRepository#refresh()
	 */
	@Override
	public void refresh() {
		this.loadFromXml();
	}
	
	/* (non-Javadoc)
	 * @see org.zsis.jobmanager.core.SchedulerRepository#add(org.zsis.jobmanager.core.SchedulerContexts)
	 */
	@Override
	public void save(SchedulerContexts schedulers) {
		this.saveToXml(schedulers);
	}
	
	/** TODO (psales 15/03/2011) - javadoc */
	protected void setSchedulers(SchedulerContexts schedulers) {
		logger.debug("Setando SchedulerContexts no repositório: {}", schedulers);
		synchronized(this.schedulers) {
			this.schedulers = schedulers;
		}
	}
	
	/** Salva as informações do arquivo xml e grava no repositorio */
	private void saveToXml(SchedulerContexts schedulers) {
		try {
			XStreamFactory.getJobManagerInstace().toXML(schedulers);
		} catch (Exception e) {
			logger.error("Erro salvando em " + fileConfig + ": " + e.getMessage(), e);
			throw new SchedulerRepositoryException("Erro salvando em " + fileConfig + ": " + e.getMessage(), e);
		}
	}

	/** Carrega as informações do arquivo xml e grava no repositorio */
	private void loadFromXml() {
		logger.debug("Carregando configurações do arquivo {}", fileConfig);
		try {
			final URL url = Loader.getResource(fileConfig);
			final InputStream input = url.openStream();
			final Object o = XStreamFactory.getJobManagerInstace().fromXML(input);

			input.close();
			
			if (o != null) {
				final SchedulerContexts contexts = (SchedulerContexts) o;
				this.setSchedulers(contexts);
			}
		} catch (IOException eIO) {
			logger.error("Erro lendo " + fileConfig + ": " + eIO.getMessage(), eIO);
			throw new SchedulerRepositoryException("Erro lendo " + fileConfig + ": " + eIO.getMessage(), eIO);
			
	    } catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new SchedulerRepositoryException(e.getMessage(), e);
		}
	}

}
