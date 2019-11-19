package springboot.dto.input;

import springboot.enums.AvaliacaoPublicacao;
import springboot.enums.CompetenciaType;

import java.util.Set;

public class AvaliacaoInput {
    private String observacaoAvaliacao;

    private String observacaoQuestao;

    private String questao;

    private Set<CompetenciaType> competencias;

    private Integer confianca;

    private AvaliacaoPublicacao avaliacaoPublicacao;

    public AvaliacaoInput(String observacaoAvaliacao, String observacaoQuestao, String questao, Set<CompetenciaType> competencias, Integer confianca, AvaliacaoPublicacao avaliacaoPublicacao) {
        this.observacaoAvaliacao = observacaoAvaliacao;
        this.observacaoQuestao = observacaoQuestao;
        this.questao = questao;
        this.competencias = competencias;
        this.confianca = confianca;
        this.avaliacaoPublicacao = avaliacaoPublicacao;
    }

    public String getObservacaoAvaliacao() {
        return observacaoAvaliacao;
    }

    public void setObservacaoAvaliacao(String observacaoAvaliacao) {
        this.observacaoAvaliacao = observacaoAvaliacao;
    }

    public String getObservacaoQuestao() {
        return observacaoQuestao;
    }

    public void setObservacaoQuestao(String observacaoQuestao) {
        this.observacaoQuestao = observacaoQuestao;
    }

    public String getQuestao() {
        return questao;
    }

    public void setQuestao(String questao) {
        this.questao = questao;
    }

    public Set<CompetenciaType> getCompetencias() {
        return competencias;
    }

    public void setCompetencias(Set<CompetenciaType> competencias) {
        this.competencias = competencias;
    }

    public Integer getConfianca() {
        return confianca;
    }

    public void setConfianca(Integer confianca) {
        this.confianca = confianca;
    }

    public AvaliacaoPublicacao getAvaliacaoPublicacao() {
        return avaliacaoPublicacao;
    }

    public void setAvaliacaoPublicacao(AvaliacaoPublicacao avaliacaoPublicacao) {
        this.avaliacaoPublicacao = avaliacaoPublicacao;
    }
}
