package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Registro de uma operação realizada no sistema, empilhado na Pilha (LIFO).
 * Quando a operação é um ATENDIMENTO, guarda também o registro criado no
 * histórico e o valor do contador de anti-inanição antes do atendimento, para
 * que o desfazimento restaure o sistema exatamente ao estado anterior.
 */
public class Operacao {

    public static final String CADASTRO = "CADASTRO";
    public static final String ATENDIMENTO = "ATENDIMENTO";
    public static final String CANCELAMENTO = "CANCELAMENTO";
    public static final String REMOCAO = "REMOCAO";

    private static final DateTimeFormatter FORMATO =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private final String tipo;
    private final Solicitacao solicitacao;
    private final Atendimento atendimento;
    private final int contadorAnterior;
    private final LocalDateTime momento;

    public Operacao(String tipo, Solicitacao solicitacao) {
        this(tipo, solicitacao, null, 0);
    }

    public Operacao(String tipo, Solicitacao solicitacao,
                    Atendimento atendimento, int contadorAnterior) {
        this.tipo = tipo;
        this.solicitacao = solicitacao;
        this.atendimento = atendimento;
        this.contadorAnterior = contadorAnterior;
        this.momento = LocalDateTime.now();
    }

    public String getTipo() {
        return tipo;
    }

    public Solicitacao getSolicitacao() {
        return solicitacao;
    }

    public Atendimento getAtendimento() {
        return atendimento;
    }

    public int getContadorAnterior() {
        return contadorAnterior;
    }

    public LocalDateTime getMomento() {
        return momento;
    }

    @Override
    public String toString() {
        return "[" + momento.format(FORMATO) + "] " + tipo
                + " - solicitação #" + solicitacao.getCodigo()
                + " (" + solicitacao.getSolicitante() + ")";
    }
}
