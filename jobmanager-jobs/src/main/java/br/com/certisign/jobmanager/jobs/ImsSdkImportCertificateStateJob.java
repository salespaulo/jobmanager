/**
 * jobmanager-jobs
 * 
 * Criada em 17/03/2011 15:47:01
 * 
 * Direito de c�pia reservado � Certisign Certificadora Digital S.A.
 * Todos os direitos s�o reservador em propriedade dda empresa
 * ------------- Certisign Certificadora Digital S.A. --------------
 * O uso deste produto � sujeito aos termos de licen�a
 */
package br.com.certisign.jobmanager.jobs;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Date;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sun.misc.BASE64Decoder;
import br.com.certisign.core.data.model.certificate.CertificateState;
import br.com.certisign.ims.common.DriversOfDB;
import br.com.certisign.ims.data.vo.DTOConfigDB;
import br.com.certisign.ims.util.DigitalCertificateConverter;
import br.com.certisign.ims.util.SystemConfigMng;
import br.com.certisign.jobmanager.jobs.utils.ImsJobUtil;
import br.com.esec.pkix.x509.X509CertificateImpl;

/**
 * TODO (psales 17/03/2011) - javadoc
 * 
 * @author psales
 * @since 17/03/2011
 */
@SuppressWarnings("restriction")
public class ImsSdkImportCertificateStateJob implements Job {

	/** TODO (psales 17/03/2011) - javadoc */
	private static final Logger logger = LoggerFactory.getLogger(ImsSdkImportCertificateStateJob.class);
	
	/** TODO (psales 17/03/2011) - javadoc */
	private final BASE64Decoder decoder = new BASE64Decoder();

	/** TODO (psales 18/03/2011) - javadoc */
	private static boolean isRunning = false;
	
	/* (non-Javadoc)
	 * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
	 */
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		if(isRunning == true) {
			return;
		}
		
		isRunning = true;

		Connection connection = null;
		Statement queryStatement = null;
		ResultSet rs = null;
		final String jobName = context.getJobDetail().getFullName();
		final String imsConfigPath = context.getJobDetail().getJobDataMap().getString("imsConfigPath");
		
		try {
				
			logger.info("____________________________________________________________________");
			logger.info("begin running method execute from class " + this.getClass().getSimpleName() + " " + new Date());
		
			ImsJobUtil.initLicenseProvidersAndAppContexts(imsConfigPath);
			
			DTOConfigDB configDB = (DTOConfigDB) SystemConfigMng.getConfigInfo("configDB");
			
			String driver = DriversOfDB.getDBDriverList().get(configDB.getDbDriverChoose());
			
			Class.forName(driver);
			connection = DriverManager.getConnection(configDB.getUrl(), configDB.getUser(), configDB.getPassword());
			queryStatement = connection.createStatement();
			rs = queryStatement.executeQuery("select c.certificate_id, c.certificate_state, r.encodedBase64 " +
							 				 "from   ims_certificate c, ims_certificate_ref r               " +
											 "where  c.certificate_id = r.certificate_id                    " +
											 "and    (c.certificate_state = 0 or                            " +
											 "        c.certificate_state = 3)                              ");
			int unknownCount = 0;
			int validCount   = 0;
			int expiredCount = 0;
			int revokedCount = 0;
			int totalUpdated = 0;

			while (rs.next()) {
				final long id = rs.getLong("certificate_id");
				final int state = rs.getInt("certificate_state");
				final String encoded = rs.getString("encodedBase64");

				final byte[] decodeBuffer = decoder.decodeBuffer(encoded);
				final X509CertificateImpl x509Certificate = new X509CertificateImpl(decodeBuffer);
				final CertificateState certificateState = DigitalCertificateConverter.checkCertificate(x509Certificate);

				// Se for igual não precisa atualizar, vai para o próximo registro
				if (state == certificateState.number()) {
					continue;
				}
				
				if (logger.isInfoEnabled()) {
					if (state == CertificateState.EXPIRED.number()) {
						expiredCount++;
					} else if (state == CertificateState.REVOKED.number()) {
						revokedCount++;
					} else if (state == CertificateState.UNKNOWN.number()) {
						unknownCount++;
					} else if (state == CertificateState.VALID.number()) {
						validCount++;
					}
				}
				
				final PreparedStatement updateStatement = connection.prepareStatement("update ims_certificate set certificate_state = ? where certificate_id = ?");
				updateStatement.setInt(1, certificateState.number());
				updateStatement.setLong(2, id);

				try {
					logger.debug("Atualizando registro IMS_CERTIFICATE={} para CERTIFICATE_STATE={}", id, certificateState);
					int updated = updateStatement.executeUpdate();
					totalUpdated += updated;

					if (updated <= 0) {
						logger.warn("Registro IMS_CERTIFICATE={} não foi atualizado para CERTIFICATE_STATE={}", id, certificateState);
					}
				} finally {
					updateStatement.close();
				}
			}
			
			logger.info(String.format("%d certificado(s) atualizado(s) com state=[%s]", validCount,   CertificateState.VALID));
			logger.info(String.format("%d certificado(s) atualizado(s) com state=[%s]", expiredCount, CertificateState.EXPIRED));
			logger.info(String.format("%d certificado(s) atualizado(s) com state=[%s]", revokedCount, CertificateState.REVOKED));
			logger.info(String.format("%d certificado(s) atualizado(s) com state=[%s]", unknownCount, CertificateState.UNKNOWN));
			
			logger.info("end running method execute from class ImsStateImportedCertificateJob" + new Date());
			logger.info("____________________________________________________________________");
			
		} catch (Exception e) {
			logger.error("Erro ao executar o Job '" + jobName + "': " + e.getMessage(), e);
			throw new JobExecutionException(e.getLocalizedMessage());
			
		} finally {
			ImsJobUtil.close(connection, queryStatement, rs);
			isRunning = false;
		}
	}

}
