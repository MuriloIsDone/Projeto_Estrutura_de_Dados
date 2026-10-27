package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

//Registro de um atendimento concluído, guardado na Lista Duplamente Encadeada.
// A DLL do professor mantém os elementos em ordem crescente e exige que o tipo
// implemente Comparable; por isso a chave de ordenação é um número de sequência
// único e crescente, que preserva a ordem cronológica de conclusão.

public class Atendimento implements Comparable<Atendimento> {

    private static final DateTimeFormatter FORMATO =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private final int sequencia;
    private final Solicitacao solicitacao;
    private final String responsavel;
    private final LocalDateTime dataHoraConclusao;

    public Atendimento(int sequencia, Solicitacao solicitacao, String responsavel) {
        this.sequencia = sequencia;
        this.solicitacao = solicitacao;
        this.responsavel = responsavel;
        this.dataHoraConclusao = LocalDateTime.now();
    }

    public int getSequencia() {
        return sequencia;
    }

    public Solicitacao getSolicitacao() {
        return solicitacao;
    }

    public String getResponsavel() {
        return responsavel;
    }

    public LocalDateTime getDataHoraConclusao() {
        return dataHoraConclusao;
    }

    @Override
    public int compareTo(Atendimento outro) {
        return Integer.compare(this.sequencia, outro.sequencia);
    }

    @Override
    public boolean equals(Object outro) {
        if (this == outro) {
            return true;
        }
        if (!(outro instanceof Atendimento)) {
            return false;
        }
        return this.sequencia == ((Atendimento) outro).sequencia;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(sequencia);
    }

    @Override
    public String toString() {
        return "Atendimento nº " + sequencia
                + " | solicitação #" + solicitacao.getCodigo()
                + " | " + solicitacao.getSolicitante()
                + " | " + solicitacao.getCategoria()
                + " | prioridade " + solicitacao.getPrioridade()
                + " | responsável: " + responsavel
                + " | concluído em " + dataHoraConclusao.format(FORMATO);
    }
}
