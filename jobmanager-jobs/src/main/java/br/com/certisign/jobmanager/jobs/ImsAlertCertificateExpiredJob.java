/**
 * jobmanager-jobs
 * 
 * Criada em 18/03/2011 15:43:33
 * 
 * Direito de cópia reservado à Certisign Certificadora Digital S.A.
 * Todos os direitos são reservador em propriedade dda empresa
 * ------------- Certisign Certificadora Digital S.A. --------------
 * O uso deste produto é sujeito aos termos de licença
 */
package br.com.certisign.jobmanager.jobs;

import java.io.FileInputStream;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;

import javax.persistence.EntityManager;

import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import br.com.certisign.core.data.model.certificate.CertificateFamily;
import br.com.certisign.core.persistence.PersistenceServiceFactory;
import br.com.certisign.core.util.FileUtils;
import br.com.certisign.ims.data.model.certificate.ImsDigitalCertificate;
import br.com.certisign.ims.data.vo.DTOConfigEMail;
import br.com.certisign.ims.service.certificate.ImsDigitalCertificateService;
import br.com.certisign.ims.service.certificate.ImsDigitalCertificateServiceImpl;
import br.com.certisign.ims.service.email.EmailServiceImpl;
import br.com.certisign.ims.util.DigitalCertificateConverter;
import br.com.certisign.ims.util.SystemConfigMng;
import br.com.certisign.jobmanager.jobs.utils.ImsJobUtil;

/**
 * TODO (psales 18/03/2011) - javadoc
 * 
 * @author psales
 * @since 18/03/2011
 */
public class ImsAlertCertificateExpiredJob implements Job {
	
	/** TODO (psales 18/03/2011) - javadoc */
	private EntityManager dbmanager = null;
	
	/** TODO (psales 18/03/2011) - javadoc */
	private final static Logger logger = Logger.getLogger(ImsAlertCertificateExpiredJob.class);
	
	public void execute(JobExecutionContext context) throws JobExecutionException {

		logger.info("____________________________________________________________________");
		logger.info("begin running method execute from class "+this.getClass().getSimpleName()+" " + new Date());
		
		final String jobName = context.getJobDetail().getFullName();
		final String imsConfigPath = context.getJobDetail().getJobDataMap().getString("imsConfigPath");
		
		try {
			
			ImsJobUtil.initLicenseProvidersAndAppContexts(imsConfigPath);
			
			dbmanager = PersistenceServiceFactory.getInstance().getManager();
			
			@SuppressWarnings("deprecation")
			ImsDigitalCertificateService certService = new ImsDigitalCertificateServiceImpl(dbmanager);
			
			// Busca a lista de 30 dias
			Collection<ImsDigitalCertificate> listOf30Days = certService.getCertificatesByRemainingDays(30);
			
			// Busca a lista de 10 dias
			Collection<ImsDigitalCertificate> listOf10Days = certService.getCertificatesByRemainingDays(10);
			
			// Busca a lista de 0 dias
			Collection<ImsDigitalCertificate> listOf0Days = certService.getCertificatesByRemainingDays(0);
			
			// Pega o path do html
			JobDataMap databMap = context.getJobDetail().getJobDataMap();
			String locationHTML = databMap.getString("location");
			
			// Lê os templates
			String template30Days = FileUtils.textFileReader(new FileInputStream(locationHTML + "//30_days_warning.html"));
			String template10Days = FileUtils.textFileReader(new FileInputStream(locationHTML + "//10_days_warning.html"));
			String template0Days = FileUtils.textFileReader(new FileInputStream(locationHTML + "//0_days_warning.html"));

			// Prepara o serviço de email
			DTOConfigEMail dtoConfigEMail = (DTOConfigEMail) SystemConfigMng.getConfigInfo("configEMail");
			EmailServiceImpl emailSender = new EmailServiceImpl(dtoConfigEMail);			
			
			// Envia os emails de 30 dias
			for (ImsDigitalCertificate imsDigitalCertificate : listOf30Days) {
				
				String emailTo = DigitalCertificateConverter.getEmailFromCertificateEntity(imsDigitalCertificate);
				
				String emailMessage = applyTemplateToMail(template30Days, imsDigitalCertificate);
				emailSender.sendEmail(emailMessage, emailTo, "IMS::Aviso de expira\u00E7\u00E3o de certificado", locationHTML);				
			}

			// Envia os emails de 10 dias
			for (ImsDigitalCertificate imsDigitalCertificate : listOf10Days) {
				
				String emailTo = DigitalCertificateConverter.getEmailFromCertificateEntity(imsDigitalCertificate);
				
				String emailMessage = applyTemplateToMail(template10Days, imsDigitalCertificate);
				emailSender.sendEmail(emailMessage, emailTo, "IMS::Aviso de expira\u00E7\u00E3o de certificado", locationHTML);				
			}

			// Envia os emails de 0 dias
			for (ImsDigitalCertificate imsDigitalCertificate : listOf0Days) {
				
				String emailTo = DigitalCertificateConverter.getEmailFromCertificateEntity(imsDigitalCertificate);
				
				String emailMessage = applyTemplateToMail(template0Days, imsDigitalCertificate);
				emailSender.sendEmail(emailMessage, emailTo, "IMS::Aviso de expira\u00E7\u00E3o de certificado", locationHTML);				
			}
			
		} catch (Exception e) {
			logger.warn("error running job '"+jobName+"': " + e.getMessage());
		}	
		
		logger.info("end running method execute the class " + this.getClass().getSimpleName());
		logger.info("____________________________________________________________________");
	}
	
	private String applyTemplateToMail(String template, ImsDigitalCertificate cert) {

		String texto = "";
		try {
		
			texto = template.replace("{subject}", cert.getCertificateDn().getCn());
			texto = texto.replace("{cert_type}", CertificateFamily.getFamily(DigitalCertificateConverter.getCertificateFamilyFromEncoding(cert.getEncodedBase64()).number(), new Locale("pt", "BR")));
			texto = texto.replace("{serial_number}", cert.getSerialNumber());
			texto = texto.replace("{issuer}", DigitalCertificateConverter.getCAIssuerFromEncoding(cert.getEncodedBase64()).getCn());
			texto = texto.replace("{common_name}", cert.getCertificateDn().getCn());
			texto = texto.replace("{not_after}", cert.getNotAfter().toString());
			
		} catch(Exception e) {
			logger.error("error running method parseEmailTemplate", e);
			return template;
		}
		
		return texto;
	}

}
