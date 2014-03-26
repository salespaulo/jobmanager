/**
 * jobmanager-core
 * 
 * Criada em 11/03/2011 17:24:44
 * 
 * Direito de copia reservado a Z Sistemas S.A.
 * Todos os direitos sao reservador em propriedade da empresa
 * ------------- Z Sistemas S.A. --------------
 * O uso deste produto esta sujeito aos termos de licenca
 */
package org.zsis.jobmanager.core;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * Classe que representa as configurações de todos os jobs 
 * agendados.
 * 
 * @author psales
 * @since 11/03/2011
 */
public class SchedulerContexts implements Serializable {

	/** Ao alterar esta classe alterar o serial number */
	private static final long serialVersionUID = 6861887299387282091L;

	/**
	 * Retorna uma instância desta classe contendo uma lista vazia
	 * de jobs agendados.
	 *
	 * @return Uma instância desta classe contendo uma lista vazia
	 */
	public static SchedulerContexts getNullInstance() {
		final List<SchedulerContext> contexts = Collections.emptyList();
		return new SchedulerContexts(contexts);
	}
	
	/**
	 * Classe que representa as informações de um job. 
	 * 
	 * @author psales
	 * @since 11/03/2011
	 */
	public static class JobContext {
		private final String name;
		private final String group;
		private final Class<?> type;
		private final Properties dataMap;
		
		/**
		 * Construtor publish utilizado somente neste pacote.
		 *
		 * @param name Nome do job.
		 * @param group Grupo do job.
		 * @param type Tipo do job.
		 * @param dataMap Parâmetros do job.
		 */
		JobContext(String name, String group, Class<?> type, Properties dataMap) {
			this.name = name;
			this.group = group;
			this.type = type;
			this.dataMap = dataMap;
		}

		/**
		 * Retorna o nome deste job.
		 *
		 * @return Nome deste job.
		 */
		public String getName() {
			return name;
		}

		/**
		 * Retorna o grupo que este job pertence.
		 *
		 * @return O grupo deste job.
		 */
		public String getGroup() {
			return group;
		}

		/**
		 * Retorna a {@link Class} deste job. 
		 *
		 * @return A {@link Class} deste job.
		 */
		public Class<?> getType() {
			return type;
		}

		/**
		 * Retorna os parâmetros usados para este job.
		 *
		 * @return the dataMap Os parâmetros deste job.
		 * @see org.quartz.JobDataMap 
		 */
		public Properties getDataMap() {
			return dataMap;
		}
		
		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return this.getClass().getSimpleName() + "=[name=" + name
					+ "; group=" + group + "; type=" + type + "; dataMap=" + dataMap + "]" ;
		}
		
		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object o) {
			if (o == this) return true;
			if (!(o instanceof SchedulerContexts.JobContext)) return false;
			final JobContext other = (JobContext) o;
			return other.name.equals(this.name)
					&& other.group.equals(this.group)
					&& other.type.equals(this.type);
		}
		
		/* (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			int primeNumber = 83;
			int result = 54;
			int hashCode = this.name.hashCode();
			hashCode += this.group.hashCode();
			result += primeNumber * result + hashCode - this.name.codePointAt(0);
			result += primeNumber * result + hashCode - this.group.codePointAt(0);
			return result;
		}
		
	}
	
	/**
	 * Classe que representa as informações do agendamento de
	 * um job.
	 * 
	 * @author psales
	 * @since 11/03/2011
	 */
	public static class TriggerContext {
		private final String name;
		private final String group;
		private final String expression;
		
		/**
		 * Construtor publish utilizado somente neste pacote.
		 *
		 * @param name Nome do agendamento.
		 * @param group Grupo do agendamento.
		 * @param expression Expressão do agendamento.
		 */
		TriggerContext(String name, String group, String expression) {
			this.name = name;
			this.group = group;
			this.expression = expression;
		}

		/**
		 * Retorna o nome deste agendamento.
		 *
		 * @return Nome deste agendamento.
		 */
		public String getName() {
			return name;
		}

		/**
		 * Retorna o grupo que este agendamento pertence.
		 *
		 * @return O grupo deste agendamento.
		 */
		public String getGroup() {
			return group;
		}

		/**
		 * Retorna a expressão usada por este agendamento para
		 * determinar a periodicidade que será executado.
		 *
		 * @return A expressão de periodicidade de execução.
		 * @see http://www.quartz-scheduler.org/docs/tutorial/TutorialLesson06.html
		 */
		public String getExpression() {
			return expression;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return this.getClass().getSimpleName() + "=[name=" + name
					+ "; group=" + group + "; expression=" + expression + "]";
		}
		
		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object o) {
			if (o == this) return true;
			if (!(o instanceof SchedulerContexts.TriggerContext)) return false;
			final TriggerContext other = (TriggerContext) o;
			return other.name.equals(this.name) && other.group.equals(this.group);
		}
		
		/* (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			int primeNumber = 83;
			int result = 54;
			int hashCode = this.name.hashCode();
			hashCode += this.group.hashCode();
			result += primeNumber * result + hashCode - this.name.codePointAt(0);
			result += primeNumber * result + hashCode - this.group.codePointAt(0);
			return result;
		}
		
	}

	/**
	 * Classe que representa as configurações de um job e
	 * seu agendamento.
	 * 
	 * @author psales
	 * @since 14/03/2011
	 */
	public static class SchedulerContext {
		
		/** As configurações do job */
		private final JobContext job;
		
		/** As configurações do agendamento */
		private final TriggerContext trigger;
		
		/**
		 * Construtor publish utilizado somente neste pacote.
		 *
		 * @param job Configurações do job.
		 * @param trigger Configurações do agendamento.
		 */
		SchedulerContext(JobContext job, TriggerContext trigger) {
			this.job = job;
			this.trigger = trigger;
		}
		
		/**
		 * Retorna o {@link SchedulerContexts.JobContext}.
		 *
		 * @return Instância de {@link SchedulerContexts.JobContext}.
		 */
		public JobContext getJob() {
			return job;
		}
		
		/**
		 * Retorna o agendamento {@link SchedulerContexts.TriggerContext}.
		 *
		 * @return Instância de {@link SchedulerContexts.TriggerContext}.
		 */
		public TriggerContext getTrigger() {
			return trigger;
		}
		
		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return this.getClass().getSimpleName() + "=[job="+job + "; trigger=" + trigger + "]";
		}
		
		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object o) {
			if (o == this) return true;
			if (!(o instanceof SchedulerContexts.SchedulerContext)) return false;
			final SchedulerContext other = (SchedulerContext) o;
			return other.job.equals(this.job) && other.trigger.equals(this.trigger);
		}
		
		/* (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			int primeNumber = 487;
			int result = 47;
			int hashCode = this.job.hashCode();
			hashCode += this.trigger.hashCode();
			result += primeNumber * result + hashCode - this.job.name.codePointAt(0);
			result += primeNumber * result + hashCode - this.trigger.name.codePointAt(0);
			return result;
		}
		
	}
	
	/** Lista das configurações dos jobs e seus agendamentos */
	private final List<SchedulerContext> schedulers;

	/**
	 * Construtor publish utilizado somente neste pacote.
	 *
	 * @param schedulers Lista de configuração dos jobs e agendamentos
	 */
	SchedulerContexts(List<SchedulerContext> schedulers) {
		this.schedulers = schedulers;
	}
	
	/**
	 * Retorna a lista de configuração dos jobs e seus agendamentos.
	 *
	 * @return Lista de configuração dos jobs.
	 * @see SchedulerContext
	 */
	public List<SchedulerContext> getSchedulers() {
		return schedulers;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		
		for (SchedulerContext c: schedulers) {
			sb.append(c);
			sb.append("\n");
		}
		
		return sb.toString();
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {
		if (o == this) return true;
		if (!(o instanceof SchedulerContexts)) return false;
		final SchedulerContexts other = (SchedulerContexts) o;
		return other.schedulers.equals(this.schedulers);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		int primeNumber = 17;
		int result = 678;
		int hashCode = this.schedulers.hashCode();
		return primeNumber * result + hashCode;
	}
	
}