package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// Registro de uma operacao realizada no sistema.
// Cada operacao e empilhada na Pilha (LIFO) para formar o historico recente
//e permitir o "desfazer".

public class Operacao {

    public static final String CADASTRO = "CADASTRO";
    public static final String ATENDIMENTO = "ATENDIMENTO";
    public static final String CANCELAMENTO = "CANCELAMENTO";

    private String tipo;
    private Solicitacao solicitacao;
    private LocalDateTime momento;

    public Operacao(String tipo, Solicitacao solicitacao) {
        this.tipo = tipo;
        this.solicitacao = solicitacao;
        this.momento = LocalDateTime.now();
    }

    public String getTipo() {
        return tipo;
    }

    public Solicitacao getSolicitacao() {
        return solicitacao;
    }

    public LocalDateTime getMomento() {
        return momento;
    }

    @Override
    public String toString() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        return "[" + momento.format(fmt) + "] " + tipo
                + " - solicitacao #" + solicitacao.getCodigo()
                + " (" + solicitacao.getSolicitante() + ")";
    }
}
