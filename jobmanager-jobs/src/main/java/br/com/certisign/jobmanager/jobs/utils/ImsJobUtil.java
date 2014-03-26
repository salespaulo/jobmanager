/**
 * jobmanager-jobs
 * 
 * Criada em 18/03/2011 15:11:09
 * 
 * Direito de cópia reservado à Certisign Certificadora Digital S.A.
 * Todos os direitos são reservador em propriedade dda empresa
 * ------------- Certisign Certificadora Digital S.A. --------------
 * O uso deste produto é sujeito aos termos de licença
 */
package br.com.certisign.jobmanager.jobs.utils;

import java.net.URL;
import java.security.Security;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.certisign.ims.service.app.AppCtxInstances;
import br.com.certisign.ims.util.SdkLicenseMng;
import br.com.certisign.jobmanager.jobs.ImsSdkImportCertificateStateJob;

/**
 * TODO (psales 18/03/2011) - javadoc
 * 
 * @author psales
 * @since 18/03/2011
 */
public class ImsJobUtil {

	/** TODO (psales 18/03/2011) - javadoc */
	private static final Logger logger = LoggerFactory.getLogger(ImsJobUtil.class);
	
	/** TODO (psales 17/03/2011) - javadoc */
	private static final Map<?, ?> appContext = new HashMap<Object, Object>();

	/** TODO (psales 18/03/2011) - javadoc */
	private static boolean initialized = false;
	
	/**
	 * TODO (psales 18/03/2011) - javadoc
	 *
	 * @param imsConfigPath
	 * @throws Exception
	 */
	public synchronized static void initLicenseProvidersAndAppContexts(String imsConfigPath) throws Exception {
		if (!initialized) {
			initLicenseAndProviders();
			initAppContexts(imsConfigPath);
			initialized = true;
		}
	}
	
	/**
	 * TODO (psales 18/03/2011) - javadoc
	 *
	 * @param resource
	 * @return
	 */
	public static URL getResource(String resource) {
		final ClassLoader classLoader = ImsSdkImportCertificateStateJob.class.getClassLoader();;
		URL url = null;
		
		if (classLoader != null) {
			logger.debug("Encontrando '{}' usando '{}' class loader.", resource, classLoader);
			url = classLoader.getResource(resource);
			
			if (url != null) {
				return url;
			}
		}

		return ClassLoader.getSystemResource(resource);
	}

	/**
	 * TODO (psales 18/03/2011) - javadoc
	 *
	 * @param connection
	 * @param statement
	 * @param rs
	 */
	public static void close(Connection connection, Statement statement, ResultSet rs) {
		try {
			if (rs != null) rs.close();
			if (statement != null) statement.close();
			if (connection != null) connection.close();
		} catch (SQLException eSQL) {
			logger.warn("Não foi possível fechar a conexão corretamente: " + eSQL.getMessage(), eSQL);
		}
	}
	
	/** TODO (psales 18/03/2011) - javadoc */
	private ImsJobUtil() {
		throw new AssertionError();
	}
	
	/**
	 * TODO (psales 18/03/2011) - javadoc
	 *
	 */
	private static void initLicenseAndProviders() {
		// adiciona a licença e o provider J128
		new SdkLicenseMng();
		// Provider do SDK, usado em outros pontos do código
		Security.addProvider(new BouncyCastleProvider());			
	}
	
	/**
	 * TODO (psales 18/03/2011) - javadoc
	 *
	 * @param imsConfigPath
	 * @throws Exception
	 */
	private static void initAppContexts(String imsConfigPath) throws Exception {
		AppCtxInstances.setupCertManagerToAppCtx(appContext, imsConfigPath);
		AppCtxInstances.setupCrlManagerToAppCtx(appContext, imsConfigPath);
		// o diretório init é onde está o arquivo sequence.properties (TODO psales: analisar uma maneira melhor de impl)
		AppCtxInstances.setupConfigMngToAppCtx(appContext, imsConfigPath, getResource("init").getPath());
	}
	
}
