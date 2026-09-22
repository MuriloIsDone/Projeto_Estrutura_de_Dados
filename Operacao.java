package model;

import java.util.Date;

// envelopa uma solicitacao com o tipo de acao + o momento, pra empilhar
// no historico e dar pra desfazer dps

public class Operacao {

    public static final String CADASTRO = "CADASTRO";
    public static final String ATENDIMENTO = "ATENDIMENTO";
    public static final String CANCELAMENTO = "CANCELAMENTO"; //reservado, n uso ainda

    private String tipo;
    private Solicitacao solicitacao;
    private Date momento;

    public Operacao(String tipo, Solicitacao solicitacao) { 
        this.tipo = tipo;
        this.solicitacao = solicitacao;
        this.momento = new Date(); // pega a hora exata que a operacao rolou
    }

    public String getTipo() {
        return tipo;
    }

    public Solicitacao getSolicitacao() {
        return solicitacao;
    }

    public Date getMomento() {
        return momento;
    }

    @Override
    public String toString() {
        return "[" + momento + "] " + tipo
                + " - solicitacao #" + solicitacao.getCodigo()
                + " (" + solicitacao.getSolicitante() + ")";
    }
}