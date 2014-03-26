/**
 * jobmanager-core
 * 
 * Criada em 04/03/2011 20:39:43
 * 
 * Direito de copia reservado a Certisign Certificadora Digital S.A.
 * Todos os direitos sao reservador em propriedade da empresa
 * ------------- Certisign Certificadora Digital S.A. --------------
 * O uso deste produto esta sujeito aos termos de licenca
 */
package br.com.certisign.jobmanager.core;

/**
 * Classe que implementa {@link SchedulerConfigurator} através da leitura 
 * e escrita de um arquivo xml. O arquivo xml pode ser passado por parâmetro
 * ou pode ser usado o arquivo xml padrão, na raiz do projeto.
 * 
 * @author psales
 * @since 04/03/2011
 */
public class XmlSchedulerConfigurator implements SchedulerConfigurator {

	/** TODO (psales 15/03/2011) - javadoc */
	private static final String DEFAULT_FILENAME = "jobmanager.xml";

	/** Instância singleton desta classe */
	private static final XmlSchedulerConfigurator INSTANCE = new XmlSchedulerConfigurator(); 

	/** Nome do arquivo de configuração */
	private static SchedulerRepository repository = null;

	/**
	 * Retorna uma instância que implementa {@link SchedulerConfigurator} 
	 * usando o arquivo xml padrão. 
	 *
	 * @return Uma instância que implementa {@link SchedulerConfigurator}.
	 */
	public static XmlSchedulerConfigurator getInstance() {
		repository = XmlSchedulerRepository.getInstance(DEFAULT_FILENAME);
		return INSTANCE;
	}
	
	/**
	 * Retorna uma instância que implementa {@link SchedulerConfigurator} passado
	 * o arquivo xml para ser lido. 
	 *
	 * @param fileName 
	 * @return Uma instância que implementa {@link SchedulerConfigurator}.
	 */
	public static SchedulerConfigurator getInstance(String fileName) {
		repository = XmlSchedulerRepository.getInstance(fileName);
		return INSTANCE;
	}

	/** Construtor privado que carrega as configurações */
	private XmlSchedulerConfigurator() {
		super();
	}

	/* (non-Javadoc)
	 * @see br.com.certisign.jobmanager.core.SchedulerConfigurator#getSchedulerRepository()
	 */
	@Override
	public SchedulerRepository getSchedulerRepository() {
		return repository;
	}

}
