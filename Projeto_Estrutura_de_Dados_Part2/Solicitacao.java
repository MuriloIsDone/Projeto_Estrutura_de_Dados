package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Solicitação de atendimento registrada na Central.
 * Todos os atributos são privados (encapsulamento); o acesso é feito por
 * métodos públicos. Duas solicitações são consideradas iguais quando possuem o
 * mesmo código, o que permite usar a busca da LinkedList do professor.
 */
public class Solicitacao {

    // Estados possíveis (conforme o enunciado do projeto).
    public static final String AGUARDANDO = "AGUARDANDO";
    public static final String EM_ATENDIMENTO = "EM_ATENDIMENTO";
    public static final String CONCLUIDA = "CONCLUIDA";
    public static final String CANCELADA = "CANCELADA";

    // Níveis de prioridade: 1 é a mais alta e 2 é a mais baixa.
    public static final int PRIORIDADE_ALTA = 1;
    public static final int PRIORIDADE_BAIXA = 2;

    private static final DateTimeFormatter FORMATO =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final int codigo;
    private final String solicitante;
    private final String descricao;
    private final String categoria;
    private final int prioridade;
    private final LocalDateTime dataHoraAbertura;
    private String status;
    private String responsavel;

    public Solicitacao(int codigo, String solicitante, String descricao,
                       String categoria, int prioridade) {
        this.codigo = codigo;
        this.solicitante = solicitante;
        this.descricao = descricao;
        this.categoria = categoria;
        this.prioridade = prioridade;
        this.dataHoraAbertura = LocalDateTime.now();
        this.status = AGUARDANDO;
        this.responsavel = "-";
    }

    /** Indica se o texto informado corresponde a um dos quatro estados válidos. */
    public static boolean statusValido(String status) {
        return AGUARDANDO.equals(status) || EM_ATENDIMENTO.equals(status)
                || CONCLUIDA.equals(status) || CANCELADA.equals(status);
    }

    public int getCodigo() {
        return codigo;
    }

    public String getSolicitante() {
        return solicitante;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getCategoria() {
        return categoria;
    }

    public int getPrioridade() {
        return prioridade;
    }

    public LocalDateTime getDataHoraAbertura() {
        return dataHoraAbertura;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getResponsavel() {
        return responsavel;
    }

    public void setResponsavel(String responsavel) {
        this.responsavel = responsavel;
    }

    @Override
    public boolean equals(Object outro) {
        if (this == outro) {
            return true;
        }
        if (!(outro instanceof Solicitacao)) {
            return false;
        }
        return this.codigo == ((Solicitacao) outro).codigo;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(codigo);
    }

    @Override
    public String toString() {
        return "#" + codigo
                + " | " + solicitante
                + " | " + categoria
                + " | prioridade " + prioridade
                + " | " + status
                + " | aberta em " + dataHoraAbertura.format(FORMATO)
                + " | responsável: " + responsavel
                + "\n      Descrição: " + descricao;
    }
}
