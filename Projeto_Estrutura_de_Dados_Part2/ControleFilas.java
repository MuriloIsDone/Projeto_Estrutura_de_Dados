package service;

import estruturas.Fila;
import excecoes.CentralException.EstruturaInconsistenteException;
import excecoes.CentralException.PrioridadeInvalidaException;
import excecoes.CentralException.SemSolicitacaoException;
import model.Solicitacao;

/**
 * Controle das duas filas de espera, uma para cada nível de prioridade.
 *
 * Conforme o enunciado, esta lógica pertence à aplicação: a classe Fila<T>
 * fornecida pelo professor é usada sem nenhuma alteração, e nenhuma coleção
 * pronta da linguagem é utilizada.
 *
 * Regra de seleção da próxima solicitação:
 *  1. Dentro de cada fila vale rigorosamente a ordem FIFO.
 *  2. Havendo solicitações nas duas filas, atende-se a fila de prioridade 1
 *     (alta), exceto quando ela já atendeu LIMITE_ATENDIMENTOS_ALTA_SEGUIDOS
 *     solicitações seguidas enquanto a fila de prioridade 2 (baixa) esperava.
 *     Nesse caso, atende-se uma da fila baixa. Isso impede a inanição
 *     (starvation) das solicitações de baixa prioridade.
 *  3. Se apenas uma fila possui solicitações, atende-se essa fila.
 */
public class ControleFilas {

    // ASSUNÇÃO: o enunciado pede "após algumas chamadas" da fila alta; o grupo
    // definiu 3. Basta alterar esta constante para mudar a regra.
    public static final int LIMITE_ATENDIMENTOS_ALTA_SEGUIDOS = 3;

    private final Fila<Solicitacao> filaAlta = new Fila<Solicitacao>();
    private final Fila<Solicitacao> filaBaixa = new Fila<Solicitacao>();

    // Quantos atendimentos seguidos da fila alta ocorreram com a fila baixa
    // esperando. É zerado quando a fila baixa é atendida ou quando não há
    // ninguém esperando nela.
    private int altaSeguidas = 0;

    // ------------------------------------------------------------------
    // Inserção e seleção
    // ------------------------------------------------------------------

    /** Insere a solicitação no final da fila correspondente à sua prioridade. */
    public void inserir(Solicitacao s) throws PrioridadeInvalidaException {
        Fila<Solicitacao> fila = filaDaPrioridade(s.getPrioridade());
        if (fila == null) {
            throw prioridadeInvalida(s.getPrioridade());
        }
        enfileirar(fila, s);
    }

    /** Consulta a próxima solicitação global, sem removê-la. */
    public Solicitacao proxima() throws SemSolicitacaoException {
        return primeiro(filaSelecionada());
    }

    /** Remove e devolve a próxima solicitação global, atualizando o contador. */
    public Solicitacao atender() throws SemSolicitacaoException {
        Fila<Solicitacao> fila = filaSelecionada();
        boolean veioDaAlta = (fila == filaAlta);
        boolean baixaEsperando = !filaBaixa.qIsEmpty();

        Solicitacao s = desenfileirar(fila);

        if (veioDaAlta && baixaEsperando) {
            altaSeguidas++;
        } else {
            altaSeguidas = 0;
        }
        return s;
    }

    // Escolhe a fila que será atendida agora, aplicando a regra de seleção.
    private Fila<Solicitacao> filaSelecionada() throws SemSolicitacaoException {
        boolean altaVazia = filaAlta.qIsEmpty();
        boolean baixaVazia = filaBaixa.qIsEmpty();

        if (altaVazia && baixaVazia) {
            throw new SemSolicitacaoException(
                    "Não há solicitações aguardando atendimento.");
        }
        if (altaVazia) {
            return filaBaixa;
        }
        if (baixaVazia) {
            return filaAlta;
        }
        if (altaSeguidas >= LIMITE_ATENDIMENTOS_ALTA_SEGUIDOS) {
            return filaBaixa;
        }
        return filaAlta;
    }

    // ------------------------------------------------------------------
    // Cancelamento, remoção e desfazimento
    // ------------------------------------------------------------------

    /**
     * Remove uma solicitação específica de sua fila, preservando a ordem das
     * demais. Devolve false se ela não estava na fila.
     */
    public boolean remover(Solicitacao alvo) {
        Fila<Solicitacao> fila = filaDaPrioridade(alvo.getPrioridade());
        if (fila == null) {
            return false;
        }
        boolean achou = false;
        int quantidade = fila.totalElementos();
        for (int i = 0; i < quantidade; i++) {
            Solicitacao atual = desenfileirar(fila);
            if (!achou && atual.equals(alvo)) {
                achou = true;               // não devolve: foi removida
            } else {
                enfileirar(fila, atual);    // devolve ao final, mantendo a ordem
            }
        }
        return achou;
    }

    /**
     * Devolve uma solicitação ao INÍCIO da fila de sua prioridade e restaura o
     * contador de anti-inanição. Como a Fila do professor só insere no final,
     * usa-se uma fila auxiliar da mesma classe (sem tocar na estrutura interna).
     */
    public void devolverAoInicio(Solicitacao s, int contadorAnterior) {
        Fila<Solicitacao> destino = filaDaPrioridade(s.getPrioridade());
        if (destino == null) {
            throw new EstruturaInconsistenteException(
                    "Prioridade inválida em solicitação já cadastrada: " + s.getPrioridade());
        }
        Fila<Solicitacao> auxiliar = new Fila<Solicitacao>();
        enfileirar(auxiliar, s);                          // a devolvida fica na frente
        while (!destino.qIsEmpty()) {
            enfileirar(auxiliar, desenfileirar(destino)); // depois vêm as que esperavam
        }
        while (!auxiliar.qIsEmpty()) {
            enfileirar(destino, desenfileirar(auxiliar)); // devolve tudo à fila original
        }
        altaSeguidas = contadorAnterior;
    }

    // ------------------------------------------------------------------
    // Consultas
    // ------------------------------------------------------------------

    public int getAltaSeguidas() {
        return altaSeguidas;
    }

    public int total() {
        return filaAlta.totalElementos() + filaBaixa.totalElementos();
    }

    public int quantidade(int prioridade) throws PrioridadeInvalidaException {
        Fila<Solicitacao> fila = filaDaPrioridade(prioridade);
        if (fila == null) {
            throw prioridadeInvalida(prioridade);
        }
        return fila.totalElementos();
    }

    /**
     * Texto com o conteúdo de uma fila, do início para o fim. A fila é girada
     * (retira do início e devolve ao final) e volta exatamente à ordem original.
     */
    public String formatar(int prioridade) throws PrioridadeInvalidaException {
        Fila<Solicitacao> fila = filaDaPrioridade(prioridade);
        if (fila == null) {
            throw prioridadeInvalida(prioridade);
        }
        if (fila.qIsEmpty()) {
            return "  (fila vazia)\n";
        }
        StringBuilder sb = new StringBuilder();
        int quantidade = fila.totalElementos();
        for (int i = 0; i < quantidade; i++) {
            Solicitacao s = desenfileirar(fila);
            sb.append("  ").append(i + 1).append("º -> ").append(s).append("\n");
            enfileirar(fila, s);
        }
        return sb.toString();
    }

    /** Explica qual fila será atendida e por quê (demonstração da seleção). */
    public String descreverSelecao() {
        StringBuilder sb = new StringBuilder();
        sb.append("  Regra: a fila de prioridade 1 (alta) é atendida primeiro; após ")
          .append(LIMITE_ATENDIMENTOS_ALTA_SEGUIDOS)
          .append(" atendimentos seguidos dela, com a fila 2 (baixa) esperando,\n")
          .append("  uma solicitação da fila de prioridade 2 é atendida.\n");
        sb.append("  Fila alta (1): ").append(filaAlta.totalElementos())
          .append(" aguardando | Fila baixa (2): ").append(filaBaixa.totalElementos())
          .append(" aguardando\n");
        sb.append("  Atendimentos seguidos da fila alta: ").append(altaSeguidas)
          .append(" de ").append(LIMITE_ATENDIMENTOS_ALTA_SEGUIDOS).append("\n");
        try {
            Fila<Solicitacao> escolhida = filaSelecionada();
            String nome = (escolhida == filaAlta) ? "ALTA (prioridade 1)" : "BAIXA (prioridade 2)";
            sb.append("  Próxima a ser atendida: fila ").append(nome).append("\n  ")
              .append(primeiro(escolhida)).append("\n");
        } catch (SemSolicitacaoException e) {
            sb.append("  Próxima a ser atendida: nenhuma (as duas filas estão vazias)\n");
        }
        return sb.toString();
    }

    // ------------------------------------------------------------------
    // Métodos auxiliares
    // ------------------------------------------------------------------

    // Devolve a fila da prioridade informada ou null se a prioridade for inválida.
    private Fila<Solicitacao> filaDaPrioridade(int prioridade) {
        if (prioridade == Solicitacao.PRIORIDADE_ALTA) {
            return filaAlta;
        }
        if (prioridade == Solicitacao.PRIORIDADE_BAIXA) {
            return filaBaixa;
        }
        return null;
    }

    private PrioridadeInvalidaException prioridadeInvalida(int prioridade) {
        return new PrioridadeInvalidaException("Prioridade inválida: " + prioridade
                + ". Use 1 (alta) ou 2 (baixa).");
    }

    // As classes do professor declaram "throws Exception". Como todas as chamadas
    // abaixo são precedidas de verificação, os métodos a seguir convertem uma
    // falha inesperada em EstruturaInconsistenteException.

    private void enfileirar(Fila<Solicitacao> fila, Solicitacao s) {
        try {
            fila.enqueue(s);
        } catch (Exception e) {
            throw new EstruturaInconsistenteException("Falha ao enfileirar a solicitação.", e);
        }
    }

    private Solicitacao desenfileirar(Fila<Solicitacao> fila) {
        try {
            return fila.dequeue();
        } catch (Exception e) {
            throw new EstruturaInconsistenteException("Falha ao desenfileirar a solicitação.", e);
        }
    }

    private Solicitacao primeiro(Fila<Solicitacao> fila) {
        try {
            return fila.front();
        } catch (Exception e) {
            throw new EstruturaInconsistenteException("Falha ao consultar o início da fila.", e);
        }
    }
}
