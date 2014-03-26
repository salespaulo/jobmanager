/**
 * jobmanager-jobs
 * 
 * Criada em 18/03/2011 15:25:04
 * 
 * Direito de cópia reservado à Certisign Certificadora Digital S.A.
 * Todos os direitos são reservador em propriedade dda empresa
 * ------------- Certisign Certificadora Digital S.A. --------------
 * O uso deste produto é sujeito aos termos de licença
 */
package br.com.certisign.jobmanager.jobs;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

import javax.persistence.EntityManager;

import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import br.com.certisign.core.common.StateType;
import br.com.certisign.core.data.model.request.template.TemplateItemType;
import br.com.certisign.core.data.vo.DTOStateChange;
import br.com.certisign.core.data.vo.ws.RequestTemplateItemVO;
import br.com.certisign.core.data.vo.ws.RequestTemplateVO;
import br.com.certisign.core.exception.CertificateProviderException;
import br.com.certisign.core.persistence.PersistenceServiceFactory;
import br.com.certisign.ims.data.model.ra.ImsCertificateRequestImpl;
import br.com.certisign.ims.data.persistence.ImsCertificateRequestImplDao;
import br.com.certisign.ims.data.vo.DTOConfigCertProvider;
import br.com.certisign.ims.data.vo.DTOConfigEMail;
import br.com.certisign.ims.data.vo.DTOConfigProxy;
import br.com.certisign.ims.data.vo.DTOImsCertificate;
import br.com.certisign.ims.data.vo.DTOImsCertificateRequest;
import br.com.certisign.ims.exception.ImsServiceException;
import br.com.certisign.ims.service.certificate.ImsDigitalCertificateServiceImpl;
import br.com.certisign.ims.service.device.DeviceServiceImpl;
import br.com.certisign.ims.service.email.EmailServiceImpl;
import br.com.certisign.ims.service.ra.CertisignCertificateProvider;
import br.com.certisign.ims.service.ra.ImsCertificateRequestService;
import br.com.certisign.ims.service.ra.ImsCertificateRequestServiceImpl;
import br.com.certisign.ims.service.template.RequestTemplateService;
import br.com.certisign.ims.service.template.RequestTemplateServiceImpl;
import br.com.certisign.ims.util.CertificateRequestConverter;
import br.com.certisign.ims.util.SystemConfigMng;
import br.com.certisign.jobmanager.jobs.utils.ImsJobUtil;
import br.com.certisign.ra.data.model.request.CertificateRequestState;
import br.com.certisign.ra.data.vo.DTOCertificateRequest;
import br.com.certisign.ra.service.CertificateProviderService;
import br.com.esec.net.ProxyConfiguration;

/**
 * TODO (psales 18/03/2011) - javadoc
 * 
 * @author psales
 * @since 18/03/2011
 */
public class ImsExportPendentRequestCertificateToGARJob implements Job {
	
	/** TODO (psales 18/03/2011) - javadoc */
	private final static Logger logger = Logger.getLogger(ImsExportPendentRequestCertificateToGARJob.class);
	
	/** TODO (psales 18/03/2011) - javadoc */
	private EntityManager dbmanager = null;
	
	/** TODO (psales 18/03/2011) - javadoc */
	private String locationHTML = null;
	
	/** TODO (psales 18/03/2011) - javadoc */
	private String ret =null;
	
	/** TODO (psales 18/03/2011) - javadoc */
	private CertificateRequestState caRequestState = CertificateRequestState.UNDER_SUBMISSION_TO_RA;
	
	public void execute(JobExecutionContext context) throws JobExecutionException {

		final String jobName = context.getJobDetail().getFullName();
		final String imsConfigPath = context.getJobDetail().getJobDataMap().getString("imsConfigPath");
		
		try {
			logger.info("____________________________________________________________________");
			logger.info("begin running method execute from class "+this.getClass().getSimpleName()+" "+new Date());

			ImsJobUtil.initLicenseProvidersAndAppContexts(imsConfigPath);
			
			// Configurações de proxy
			DTOConfigProxy configProxy = (DTOConfigProxy) SystemConfigMng.getConfigInfo("configProxy");
			if(configProxy != null && configProxy.isEnabled()) {
				logger.debug("Configurando proxy");
				ProxyConfiguration proxy = ProxyConfiguration.getInstance();
				proxy.setProxyConfiguration(configProxy.getServer(), configProxy.getPort(), configProxy.getUserName(), configProxy.getPassword());
			}
			
			dbmanager = PersistenceServiceFactory.getInstance().getManager();
			
			// busca a lista de pedidos passíveis de mudança automática de status.
			ImsCertificateRequestImplDao imsCertificateRequestImplDao = new ImsCertificateRequestImplDao(dbmanager);
			List<ImsCertificateRequestImpl> listOfCertReq = (List<ImsCertificateRequestImpl>) imsCertificateRequestImplDao.getListOfCertReqWithStatusPending();//OfCertReqWithStatusPending(CertificateRequestState.IMS_ADMIN_PENDING || CertificateRequestState.IMS_ADMIN_APPROVED || CertificateRequestState.RA_VALIDATED);
			
			// Converte para uma lista de DTOs
			List<DTOImsCertificateRequest> requestsDto = new ArrayList<DTOImsCertificateRequest>(listOfCertReq.size());			Iterator<ImsCertificateRequestImpl> iListOfCertReq = listOfCertReq.iterator();
			while(iListOfCertReq.hasNext()) {
				requestsDto.add(CertificateRequestConverter.convertCertificateRequestToDto(iListOfCertReq.next()));
			}
			
			if (requestsDto.size()!=0){
			   		Iterator<DTOImsCertificateRequest> iRequestsDto = requestsDto.iterator();
					while(iRequestsDto.hasNext()) {
						
						DTOImsCertificateRequest certReq = iRequestsDto.next();
						DTOConfigCertProvider dtoConfigCertProvider = certReq.getCertProvider();
						try {

							@SuppressWarnings("deprecation")
							ImsCertificateRequestService reqService = new ImsCertificateRequestServiceImpl(dbmanager);
							@SuppressWarnings("deprecation")
							ImsDigitalCertificateServiceImpl certService = new ImsDigitalCertificateServiceImpl(dbmanager);
							DeviceServiceImpl deviceService = new DeviceServiceImpl(dbmanager);
							
							CertificateProviderService certificateProviderService = new CertisignCertificateProvider("", dtoConfigCertProvider.getUrlextern());
							
							// verifica se External ID está nulo
							if (certReq.getExternalId() != null && certReq.getExternalId().length()>0) {
								// Retorna o status do pedido no Serviço Provedor de Certificados.
								caRequestState = certificateProviderService.getCertificateRequestStatus(certReq.getExternalId());
							}   
							
							// Se retornar 7 do GAR, não faz nada. Está aguardando autorização no GAR
							if(caRequestState == CertificateRequestState.IMS_ADMIN_APPROVAL_PENDING)
								continue;
							
							// Se o pedido foi aprovado pelo Admin do IMS
							if(certReq.getStatus() == CertificateRequestState.IMS_ADMIN_APPROVED) {
								// É preciso dispachar o mesmo para o Serviço Provedor de Certificados.
								logger.debug("running method if IMS_ADMIN_APPROVED  to user id ="+certReq.getUser().getId()+" email ="+certReq.getUser().getEmail() +" Id Certificate Req ="+certReq.getId());								
							    
								try {
									
									reqService.dispatchRequest(certReq);									
									logger.debug("request " + certReq.getExternalId() + " dispatched to certificate provider.");
									continue;
									
								} catch (ImsServiceException ex) {
									
									// GAR recusou por alguma razão. Não dispacha mais.
									dbmanager.getTransaction().begin();
									
									DTOStateChange stateChange = new DTOStateChange();								
									stateChange.setComment("Pedido recusado externamente.");
									stateChange.setDate(new GregorianCalendar());
									stateChange.setNewState(CertificateRequestState.AUTOMATICALLY_REJECTED.number());
									stateChange.setOldState(CertificateRequestState.RA_APPROVED.number());
									stateChange.setType(StateType.REQUEST);
									stateChange.setUser(certReq.getUser());
									stateChange.setObjectId(certReq.getId());
									
									certReq.setStatus(CertificateRequestState.AUTOMATICALLY_REJECTED);
									
								    reqService.changeRequestState(certReq, stateChange);
								    
								    dbmanager.getTransaction().commit();
								    
									try {
										
									    // Manda email
	    								ret = checkLocation(context);
	    								if (ret.equals("error")){
	    									logger.error("error method execute the class ImsCheckCertReqStatusAtRAJob not found location the HTML. "+new Date());
	    								}else{
	    									
	    									// Ler o template de email respectivo à mudança de status								
	    									String emailTemplate = readFileHTML(CertificateRequestState.AUTOMATICALLY_REJECTED.number(), locationHTML);
	    	
	    									if (emailTemplate==null){
	    										logger.error("html template not found");
	    									}else{
	    										
	    										RequestTemplateService templateService = new RequestTemplateServiceImpl(dbmanager);
	    										RequestTemplateVO template = templateService.findById(certReq.getType().getRequestTemplateId());
	
	    										RequestTemplateItemVO[] emailItems = template.getItems(TemplateItemType.EMAIL);
	    										RequestTemplateItemVO[] firstNameItems = template.getItems(TemplateItemType.FIRSTNAME);
	    										RequestTemplateItemVO[] lastNameItems = template.getItems(TemplateItemType.LASTNAME);
	    																				
	    										StringBuilder name = new StringBuilder();
	    										name.append(certReq.getData().getItems().get(firstNameItems[0].getKey()));
	    										if(lastNameItems != null) {
	    											name.append(" " + certReq.getData().getItems().get(lastNameItems[0].getKey()));
	    										}
	    										String destEmail = certReq.getData().getItems().get(emailItems[0].getKey());
	    										
	    										String emailToSend = parseEmailTemplate(emailTemplate, certReq, name.toString());
	    										// Envia o email
	    										DTOConfigEMail dtoConfigEMail = (DTOConfigEMail) SystemConfigMng.getConfigInfo("configEMail");
	    										EmailServiceImpl emailSender = new EmailServiceImpl(dtoConfigEMail);
	    										
	    										emailSender.sendEmail(emailToSend, destEmail, "IMS::Aviso de mudan\u00E7a do status do Pedido de Certificado: " + caRequestState.getStateName(new Locale("pt", "BR")),locationHTML);
	    									}
	    								}
	    								
									} catch(Exception e) {
										// Erro de envio de email não interrompe o job.
										logger.error("Erro ao enviar email.", ex);
									}
									
									logger.error("erro running method to user id ="+certReq.getUser().getId()+" email ="+certReq.getUser().getEmail() +" Id Certificate Req ="+certReq.getId(), ex);
									
									continue;
								}								
							}				
							
							// Se foi emitido fora do IMS, atualiza.
							if(caRequestState == CertificateRequestState.CA_ISSUED) {

								dbmanager.getTransaction().begin();
								
								DTOStateChange stateChange = new DTOStateChange();								
								stateChange.setComment("Certificado emitido fora do IMS.");
								stateChange.setDate(new GregorianCalendar());
								stateChange.setNewState(CertificateRequestState.INSTALLED_EXT.number());
								stateChange.setOldState(CertificateRequestState.RA_APPROVED.number());
								stateChange.setType(StateType.REQUEST);
								stateChange.setUser(certReq.getUser());
								
								certReq.setStatus(CertificateRequestState.INSTALLED_EXT);
								
							    reqService.changeRequestState(certReq, stateChange);
							    
							    // Pega o certificado emitido
							    String certBase64 = ((CertisignCertificateProvider)certificateProviderService).retrieveCertificate(
							    		certReq.getExternalId(),
							    		certReq.getCertProvider().getLicense(),
							    		certReq.getCertProvider().getPassword());
							    
							    // Importa o certificado
							    DTOImsCertificate certDto = (DTOImsCertificate)certService.decodeCertificate(certReq, certBase64);
							    certDto = certService.addRequestedCertificateIT(certDto, null);
								deviceService.addFakeDeviceToImportedCert(certDto);
							    
							    dbmanager.getTransaction().commit();
							    
								logger.info("request " + certReq.getExternalId() + " issued outside IMS control.");
								
								continue;
							}							
						
							// Se o status do pedido é UNDER_SUBMISSION_TO_RA e o status externo é superior a isso
							// ou
							// Se o status do pedido é RA_VALIDATED e o status externo é superior a isso.
							if((caRequestState.number() > CertificateRequestState.UNDER_SUBMISSION_TO_RA.number() && 
												certReq.getStatus() == CertificateRequestState.UNDER_SUBMISSION_TO_RA) ||
												(caRequestState.number() > CertificateRequestState.RA_VALIDATED.number() && 
														certReq.getStatus() == CertificateRequestState.RA_VALIDATED)) {									

							    logger.debug("running method if IMS_STATE with CA_STATE to user id ="+certReq.getUser().getId()+" email ="+certReq.getUser().getEmail()+" Id Certificate Req ="+certReq.getId());
								
								// Atualiza o status do pedido com aquele enviado pelo provedor externo.
								updateState(dbmanager, certReq, caRequestState);								
							}							
							
						} catch (CertificateProviderException e) {
							logger.error("erro running method  to user id ="+certReq.getUser().getId()+" email ="+certReq.getUser().getEmail() +" Id Certificate Req ="+certReq.getId(), e);
						} finally {
					}						
				}
					
		 } // if (listOfCertReq.size()!=0){
			logger.info("end running method execute the class "+this.getClass().getSimpleName());
			logger.info("____________________________________________________________________");
		} catch(Exception ex) {
			logger.error("error running job '"+jobName+"': " + ex.getMessage(), ex);
			throw new JobExecutionException(ex.getLocalizedMessage());

		} finally {
			if(dbmanager != null && dbmanager.isOpen()){
				dbmanager.close();
			}
		}
	}
	
	/**
	 * Tem a finalidade de receber um texto e substituir os campos em destaque pelos dados do objeto.
	 * @param   template Texto a ser substituido
	 * @param   certReq Dados do objeto ImsCertificateRequestImpl
	 */
	public String parseEmailTemplate(String template, DTOCertificateRequest certReq, String toName) {
		String texto = "";
		try{

			texto = template.replace("{nome}", toName);
			texto = texto.replace("{id}",  certReq.getId()+"");
			texto = texto.replace("{externalId}", (certReq.getExternalId()== null || "".equals(certReq.getExternalId()) ||certReq.getExternalId().equals("")?"S/N":certReq.getExternalId())    );
			
		}catch(Exception e){
			logger.error("error running method parseEmailTemplate", e);
			return template;
		}
		return texto;
	}
	
	/**
	 * Tem a finalidade de receber um texto e substituir os campos em destaque pelos dados do objeto.
	 * @param   template Texto a ser substituido
	 * @param   certReq Dados do objeto ImsCertificateRequestImpl
	 */
	public String parseEmailTemplateForIMSApproved(String template, ImsCertificateRequestImpl certReq, String toName) {
		
		try{

			template = template.replace("{nome}", toName);
			template = template.replace("{id}", certReq.getId()+"");
			
		}catch(Exception e){
			logger.error("error running method parseEmailTemplateForIMSApproved", e);
			return template;
		}
		return template;
	}
	
	/**
	 * Tem a finalidade de verificar o status que veio WebService e converter para Enum do IMS.
	 * E conectar no banco de dados e atualizar o status do CertificateRequest com o novo status.
	 * @param   dbmanager Conexão do banco
	 * @param   certificateRequest Dados do objeto ImsCertificateRequestImpl
	 * @throws Exception 
	 */
	private void updateState(EntityManager dbmanager, DTOCertificateRequest certificateRequestDto, CertificateRequestState state){

		try {
			
		    ImsCertificateRequestImplDao imsCertificateRequestImplDao = new ImsCertificateRequestImplDao(dbmanager);
		    dbmanager.getTransaction().begin();
		    
		    ImsCertificateRequestImpl certificateRequest_ = imsCertificateRequestImplDao.find(certificateRequestDto.getId());
		    certificateRequest_.setCertificateRequestState(state);
		    imsCertificateRequestImplDao.merge(certificateRequest_);
		    dbmanager.getTransaction().commit();
		    
		} catch (Exception ex){
			dbmanager.getTransaction().rollback();
			logger.error("error method updateState with user  ="+certificateRequestDto.getUser().getId() +" - "+certificateRequestDto.getUser().getEmail(), ex);			
		}		
	}
	
	/**
	 * Tem a finalidade de ler arquivo HTML conforme status do email
	 * @param   statusHTML  1 - onde IMS_ADMIN_APPROVED/IMS_ADMIN_APPROVAL_PENDING
     * @return  String retorna o arquivo html em uma String
	 * @throws Exception 
	 */
	public String  readFileHTML(int statusHTML , String in_path_){
		final StringBuffer textoHTML  = new StringBuffer("");
		File file               = null;
		String location         = null;
		Scanner input = null;

		try{
			if (statusHTML == CertificateRequestState.IMS_ADMIN_APPROVED.number()){
			    location =in_path_+"//Aprovado_Adm_IMS.html";
				file = new File(location);
		    }else if(statusHTML == CertificateRequestState.RA_VALIDATED.number()){
		    	location =in_path_+"//Validado_por_AR.html";
				file = new File(location);
		    }else if(statusHTML == CertificateRequestState.RA_APPROVED.number()){
		    	location =in_path_+"//Aprovado_por_AR.html";
				file = new File(location);
		    }else if(statusHTML == CertificateRequestState.AUTOMATICALLY_REJECTED.number()){
		    	location =in_path_+"//Rejeitado_pelo_GAR.html";
				file = new File(location);
		    }
		    
	        input = new Scanner(file);
	        
	        while (input.hasNextLine()) {
	        	final String text = input.nextLine();

	        	if (text != null) {
		        	textoHTML.append(text.trim());
	        	}
	        }
		} catch (FileNotFoundException e) {
			logger.error("running method readFileHTML não encontrou o arquivo ="+location, e);
			return null;
		}catch (Exception ex){
			logger.error("error method readFileHTML status "+statusHTML+  " -"+location, ex);
			return null;
		} finally {
			if (input != null) input.close();
		}
		return textoHTML.toString();
	}	
	/**
	 * Tem a finalidade de ler arquivo HTML conforme status do email
	 * @param   statusHTML  1 - onde IMS_ADMIN_APPROVED/IMS_ADMIN_APPROVAL_PENDING
     * @return  String retorna o arquivo html em uma String
	 * @throws Exception 
	 */
	private String checkLocation(JobExecutionContext job_ ){
		String ret = "success";
		try{
			JobDataMap databMap    = job_.getJobDetail().getJobDataMap();
			locationHTML = databMap.getString("location");
		}catch(Exception e){
			ret = "error";
		}
		return ret;
		
	}
}
