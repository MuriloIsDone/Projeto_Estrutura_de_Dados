package service;

import estruturas.FilaCircular;
import estruturas.Pilha;
import model.Operacao;
import model.Solicitacao;

public class CentralAtendimento {

    private static final int TAM_FILA = 50;
    private static final int TAM_PILHA = 100;

    private FilaCircular<Solicitacao> filaEspera;
    private Pilha<Operacao> historico;

    private int proximoCodigo;
    private int totalCadastradas;
    private int totalAtendidas;

    public CentralAtendimento() {
        this.filaEspera = new FilaCircular<Solicitacao>(TAM_FILA);
        this.historico = new Pilha<Operacao>(TAM_PILHA);
        this.proximoCodigo = 101;   // codigos comecam em 101, como no enunciado
        this.totalCadastradas = 0;
        this.totalAtendidas = 0;
    }

    // 1- cadastrar nova solicitacao
    public Solicitacao cadastrar(String solicitante, String descricao,
                                 String categoria, int prioridade) throws Exception {
        if (filaEspera.qIsFull())
            throw new Exception("A fila de espera esta cheia (capacidade "
                    + TAM_FILA + "). Atenda solicitacoes antes de cadastrar novas.");
        verificaEspacoHistorico();

        Solicitacao s = new Solicitacao(proximoCodigo, solicitante, descricao,
                                        categoria, prioridade);
        filaEspera.enqueue(s);                              // entra no FIM da fila
        registrarOperacao(new Operacao(Operacao.CADASTRO, s));
        proximoCodigo++;
        totalCadastradas++;
        return s;
    }

    // 2- consultar proxima solicitacao (sem remover neh)
    public Solicitacao consultarProxima() throws Exception {
        if (filaEspera.qIsEmpty())
            throw new Exception("Nao ha solicitacoes aguardando atendimento.");
        return filaEspera.front();
    }

    // 3- atender proxima solicitacao (remove do inicio da fila - fifo)
    public Solicitacao atenderProxima(String responsavel) throws Exception {
        if (filaEspera.qIsEmpty())
            throw new Exception("Nao ha solicitacoes na fila para atender.");
        verificaEspacoHistorico();

        Solicitacao s = filaEspera.dequeue();
        s.setResponsavel(responsavel);
        s.setStatus(Solicitacao.CONCLUIDA);
        registrarOperacao(new Operacao(Operacao.ATENDIMENTO, s));
        totalAtendidas++;
        return s;
    }

    // 4 - Exibir fila de solicitacoes
    // a fila e percorrida girando os elementos... retira do inicio e devolve
    // ao fim. apos qtde rotacoes a fila volta exatamente a ordem original.
    public String filaFormatada() throws Exception {
        if (filaEspera.qIsEmpty())
            return "  (fila vazia)";

        StringBuilder sb = new StringBuilder();
        int qtde = filaEspera.totalElementos();
        for (int i = 0; i < qtde; i++) {
            Solicitacao s = filaEspera.dequeue();
            sb.append("  ").append(i + 1).append("o da fila -> ").append(s).append("\n");
            filaEspera.enqueue(s);
        }
        return sb.toString();
    }

    // 5- quantidade de solicitacoes aguardando
    public int quantidadeAguardando() {
        return filaEspera.totalElementos();
    }

    public boolean filaVazia() {
        return filaEspera.qIsEmpty();
    }

    // 6- consultar ultima operacao realizada (topo da pilha, sem remover)
    public Operacao ultimaOperacao() throws Exception {
        if (historico.isEmpty())
            throw new Exception("Nenhuma operacao foi realizada ainda.");
        return historico.topo();
    }

    // 7- exibir historico em ordem inversa lifo
    // desempilha tudo para uma pilha auxiliar (printa no caminho) e
    // depois devolve, preservando a ordem original.
    public String historicoFormatado() throws Exception {
        if (historico.isEmpty())
            return "  (nenhuma operacao registrada)";

        Pilha<Operacao> aux = new Pilha<Operacao>(TAM_PILHA);
        StringBuilder sb = new StringBuilder();

        while (!historico.isEmpty()) {
            Operacao op = historico.pop();
            sb.append("  ").append(op).append("\n");
            aux.push(op);
        }
        while (!aux.isEmpty())          // devolve na ordem correta
            historico.push(aux.pop());

        return sb.toString();
    }

    // ------------------------------------------------------------------
    // 8 - desfaz a ultima operacao
    // APENAASSS operacoes de atendimento podem ser desfeitas, tipo, a solicitacao
    // volta para a posicao que estava antes, ou sejaaaa, o inicio da fila.
    // Para isso vai usar uma fila auxiliar da mesma classe fornecida pelo
    // professor (proibido mexer no vetor interno).
    // ------------------------------------------------------------------
    public Solicitacao desfazerUltimaOperacao() throws Exception {
        if (historico.isEmpty())
            throw new Exception("Nao ha operacoes para desfazer.");

        Operacao op = historico.topo();
        if (!op.getTipo().equals(Operacao.ATENDIMENTO))
            throw new Exception("A ultima operacao foi um " + op.getTipo()
                    + ". Somente operacoes de ATENDIMENTO podem ser desfeitas.");

        if (filaEspera.qIsFull())
            throw new Exception("A fila esta cheia: nao ha espaco para devolver a solicitacao.");

        historico.pop();                        // agora sim a operacao sai da pilha

        Solicitacao s = op.getSolicitacao();
        s.setStatus(Solicitacao.AGUARDANDO);
        s.setResponsavel("-");

        // coloca dnv no inicio: monta uma fila auxiliar com ela na frente
        FilaCircular<Solicitacao> aux = new FilaCircular<Solicitacao>(TAM_FILA);
        aux.enqueue(s);
        while (!filaEspera.qIsEmpty())
            aux.enqueue(filaEspera.dequeue());
        while (!aux.qIsEmpty())
            filaEspera.enqueue(aux.dequeue());

        totalAtendidas--;
        return s;
    }

    // resumo do sistema
    public String resumo() {
        StringBuilder sb = new StringBuilder();
        sb.append("  Solicitacoes cadastradas ..: ").append(totalCadastradas).append("\n");
        sb.append("  Solicitacoes atendidas ....: ").append(totalAtendidas).append("\n");
        sb.append("  Aguardando na fila ........: ").append(filaEspera.totalElementos())
          .append(" de ").append(TAM_FILA).append("\n");
        sb.append("  Operacoes no historico ....: ").append(historico.sizeElements())
          .append(" de ").append(TAM_PILHA);
        return sb.toString();
    }

    // pfv verifica, antes de alterar a fila, se ainda cabe uma operacao no
    // historico. Assim o sistema nunca fica em estado inconsistente...
    private void verificaEspacoHistorico() throws Exception {
        if (historico.isFull())
            throw new Exception("O historico de operacoes esta cheio (capacidade "
                    + TAM_PILHA + "). Desfaca ou reinicie o sistema.");
    }

    private void registrarOperacao(Operacao op) throws Exception {
        historico.push(op);
    }
}
