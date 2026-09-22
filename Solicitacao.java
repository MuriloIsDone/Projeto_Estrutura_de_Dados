package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Classe de dominio que representa uma solicitacao de atendimento.
 * Encapsulamento: todos os atributos sao privados e o acesso e feito por
 * metodos publicos (getters/setters).
 */
public class Solicitacao {

    //estados possiveis da solicitacao (constantes evitam erro de digitacao)
    public static final String AGUARDANDO = "AGUARDANDO";
    public static final String EM_ATENDIMENTO = "EM_ATENDIMENTO";
    public static final String CONCLUIDA = "CONCLUIDA";
    public static final String CANCELADA = "CANCELADA";

    private int codigo;
    private String solicitante;
    private String descricao;
    private String categoria;
    private int prioridade;
    private LocalDateTime dataHoraAbertura;
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

    // Representacao textual da solicitacao, usada quando o objeto e impresso.

    @Override
    public String toString() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        StringBuilder sb = new StringBuilder();
        sb.append("#").append(codigo)
          .append(" | ").append(solicitante)
          .append(" | ").append(categoria)
          .append(" | prio ").append(prioridade)
          .append(" | ").append(status)
          .append(" | aberta em ").append(dataHoraAbertura.format(fmt))
          .append(" | resp.: ").append(responsavel)
          .append("\n    Descricao: ").append(descricao);
        return sb.toString();
    }
}
