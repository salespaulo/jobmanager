/**
 * jobmanager-jobs
 * 
 * Criada em 18/03/2011 15:07:01
 * 
 * Direito de cópia reservado à Certisign Certificadora Digital S.A.
 * Todos os direitos são reservador em propriedade dda empresa
 * ------------- Certisign Certificadora Digital S.A. --------------
 * O uso deste produto é sujeito aos termos de licença
 */
package br.com.certisign.jobmanager.jobs;

import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;

import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import br.com.certisign.core.persistence.PersistenceServiceFactory;
import br.com.certisign.ims.data.model.group.GroupImpl;
import br.com.certisign.ims.data.model.ra.ImsCertificateProviderConfig;
import br.com.certisign.ims.data.persistence.ImsGroupImplDao;
import br.com.certisign.ims.data.vo.DTOConfigCertProvider;
import br.com.certisign.ims.data.vo.DTOConfigProxy;
import br.com.certisign.ims.service.ra.ImsCertificateRequestServiceImpl;
import br.com.certisign.ims.util.SystemConfigMng;
import br.com.certisign.jobmanager.jobs.utils.ImsJobUtil;
import br.com.esec.net.ProxyConfiguration;

/**
 * TODO (psales 18/03/2011) - javadoc
 * 
 * @author psales
 * @since 18/03/2011
 */
public class ImsGARImportRequestCertificateJob implements Job {

	/** TODO (psales 18/03/2011) - javadoc */
	private final static Logger logger = Logger.getLogger(ImsGARImportRequestCertificateJob.class);

	/** TODO (psales 18/03/2011) - javadoc */
	private static boolean isRunning = false;
	
	/** TODO (psales 18/03/2011) - javadoc */
	private EntityManager dbmanager = null;
	
	public void execute(JobExecutionContext context) throws JobExecutionException {

		if(isRunning == true) {
			return;
		}
		
		isRunning = true;
		
		final String jobName = context.getJobDetail().getFullName();
		final String imsConfigPath = context.getJobDetail().getJobDataMap().getString("imsConfigPath");

		try {
		
			logger.info("____________________________________________________________________");
			logger.info("begin running method execute from class "+this.getClass().getSimpleName()+" " + new Date());

			ImsJobUtil.initLicenseProvidersAndAppContexts(imsConfigPath);
			
			// Configurações de proxy
			DTOConfigProxy configProxy = (DTOConfigProxy) SystemConfigMng.getConfigInfo("configProxy");
			if(configProxy != null && configProxy.isEnabled()) {
				logger.debug("Configurando proxy");
				ProxyConfiguration proxy = ProxyConfiguration.getInstance();
				proxy.setProxyConfiguration(configProxy.getServer(), configProxy.getPort(), configProxy.getUserName(), configProxy.getPassword());
			}
		
			dbmanager = PersistenceServiceFactory.getInstance().getManager();

			@SuppressWarnings("deprecation")
			ImsCertificateRequestServiceImpl reqService = new ImsCertificateRequestServiceImpl(dbmanager);
			ImsGroupImplDao groupDao = new ImsGroupImplDao(dbmanager);
			List<GroupImpl> groups = groupDao.getListRoot();
			
			for (GroupImpl groupImpl : groups) {
				
				Set<ImsCertificateProviderConfig> providers = groupImpl.getCertProviderConfigs();
				for (ImsCertificateProviderConfig imsCertificateProviderConfig : providers) {
					
					DTOConfigCertProvider providerDto = new DTOConfigCertProvider();
					providerDto.setAccount(imsCertificateProviderConfig.getAccount());
					providerDto.setId(imsCertificateProviderConfig.getId());
					providerDto.setLicense(imsCertificateProviderConfig.getLicense());
					providerDto.setName(imsCertificateProviderConfig.getProviderName());
					providerDto.setPassword(imsCertificateProviderConfig.getPassword());
					providerDto.setProviderType(imsCertificateProviderConfig.getProviderType().number());
					providerDto.setUrlextern(imsCertificateProviderConfig.getUrl());
					
					reqService.importExternalRequests(providerDto, groupImpl.getId());
				}
			}
			
			logger.info("end running method execute from class "+this.getClass().getSimpleName()+" " + new Date());
			logger.info("____________________________________________________________________");
			
		} catch (Exception e) {
			logger.error("error running job '"+jobName+"': "+e.getMessage(), e);
			isRunning = false;
			throw new JobExecutionException(e.getLocalizedMessage());
		} finally {
			isRunning = false;
		}		
	}
}
