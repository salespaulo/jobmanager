/**
 * jobmanager-core
 * 
 * Criada em 15/03/2011 12:01:16
 * 
 * Direito de cópia reservado à Z Sistemas S.A.
 * Todos os direitos são reservador em propriedade dda empresa
 * ------------- Z Sistemas S.A. --------------
 * O uso deste produto é sujeito aos termos de licença
 */
package org.zsis.jobmanager.core;

/**
 * TODO (psales 15/03/2011) - javadoc
 * 
 * @author psales
 * @since 15/03/2011
 */
public class QuartzSchedulerException extends RuntimeException {

	/** TODO (psales 15/03/2011) - javadoc */
	private static final long serialVersionUID = 3347466928251299945L;

	/**
	 * TODO (psales 15/03/2011) - javadoc
	 *
	 */
	public QuartzSchedulerException(String msg, Throwable cause) {
		super(msg, cause);
	}
	
}
