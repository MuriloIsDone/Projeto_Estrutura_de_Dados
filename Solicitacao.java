package model;

import java.util.Date;
import java.time.format.DateTimeFormatter;

// representa uma solicitacao de atendimento, tudo privado + get/set (encapsulamento)
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
    private Date dataHoraAbertura;
    private String status;
    private String responsavel;

    public Solicitacao(int codigo, String solicitante, String descricao,
                       String categoria, int prioridade) {
        this.codigo = codigo;
        this.solicitante = solicitante;
        this.descricao = descricao;
        this.categoria = categoria;
        this.prioridade = prioridade;
        this.dataHoraAbertura = new Date();
        this.status = AGUARDANDO; // nasce aguardando, sem responsavel ainda
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

    public Date getDataHoraAbertura() {
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

    //epresentacao textual da solicitacao, usada quando o objeto e impresso.
    // é o que aparece quando da print na solicitacao (fila/historico usam isso)    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("#").append(codigo)
          .append(" | ").append(solicitante)
          .append(" | ").append(categoria)
          .append(" | prio ").append(prioridade)
          .append(" | ").append(status)
          .append(" | aberta em ").append(dataHoraAbertura)
          .append(" | resp.: ").append(responsavel)
          .append("\n    Descricao: ").append(descricao);
        return sb.toString();
    }
}
