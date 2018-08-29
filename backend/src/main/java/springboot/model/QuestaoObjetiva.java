package springboot.model;

/**
 * Classe concreta que estende atributos comuns da classe Questão e adiciona coleção de alternativas.
 * 
 * Ambiente de Estudo ao Pensamento Computacional
 * 
 * @author Marcelo Gabriel dos Santos Queiroz Vitorino 
 */

import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;


@Entity(name = "Quest_Obj")
public class QuestaoObjetiva extends Questao {
	
	@ManyToOne   // Revisar essa relação
	@Column(nullable = false)
	private List<Alternativa> alternativas;
	
	public QuestaoObjetiva(Questao tipo, String enunciado, String fonte, String autor, byte[] image, List<Alternativa> alternativas) {
		super(tipo,enunciado,fonte,autor,image);
		this.alternativas = alternativas;
	}
	
	public QuestaoObjetiva() {
	}

	@JoinColumn(name = "QuestAlternativa")
	public List<Alternativa> getAlternativas() {
		return alternativas;
	}

	public void setAlternativas(List<Alternativa> alternativas) {
		this.alternativas = alternativas;
	}

}

