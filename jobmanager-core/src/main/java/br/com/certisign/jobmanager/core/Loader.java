/**
 * jobmanager-core
 * 
 * Criada em 11/03/2011 21:03:28
 * 
 * Direito de copia reservado a Certisign Certificadora Digital S.A.
 * Todos os direitos sao reservador em propriedade da empresa
 * ------------- Certisign Certificadora Digital S.A. --------------
 * O uso deste produto esta sujeito aos termos de licenca
 */
package br.com.certisign.jobmanager.core;

import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO (psales 11/03/2011) - javadoc
 * 
 * @author psales
 * @since 11/03/2011
 */
public class Loader {

	/** TODO (psales 11/03/2011) - javadoc */
	public static final Logger logger = LoggerFactory.getLogger(Loader.class);
	
	/**
	 * TODO (psales 11/03/2011) - javadoc
	 *
	 * @param resource
	 * @return
	 */
	public static URL getResource(String resource) {
		final ClassLoader classLoader = Loader.class.getClassLoader();;
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
	
	/** TODO (psales 11/03/2011) - javadoc */
	private Loader() {
		throw new AssertionError();
	}
	
}
